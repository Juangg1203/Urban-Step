package com.tiendaropa.controller;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiendaropa.model.Cliente;
import com.tiendaropa.model.EstadoPedido;
import com.tiendaropa.model.Pedido;
import com.tiendaropa.service.ClienteService;
import com.tiendaropa.service.PedidoService;
import com.tiendaropa.service.WompiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Flujo de pago con la pasarela.
 *
 *   GET  /pagos/wompi/{id}        arma el formulario firmado y lo manda al checkout
 *   GET  /pagos/wompi/retorno     el cliente vuelve; se consulta el estado real
 *   POST /api/pagos/wompi/eventos webhook que envia Wompi (sin sesion, sin CSRF)
 *
 * Los datos de la tarjeta nunca tocan este servidor. Lo unico que viaja hacia
 * aca es un identificador de transaccion, y su estado se verifica contra la
 * API de Wompi antes de mover el pedido.
 */
@Controller
public class PagoController {

    private static final Logger log = LoggerFactory.getLogger(PagoController.class);

    private final WompiService wompi;
    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final ObjectMapper mapper = new ObjectMapper();

    public PagoController(WompiService wompi, PedidoService pedidoService,
                          ClienteService clienteService) {
        this.wompi = wompi;
        this.pedidoService = pedidoService;
        this.clienteService = clienteService;
    }

    private Optional<Cliente> cliente(Authentication auth) {
        return auth == null ? Optional.empty() : clienteService.porNombreUsuario(auth.getName());
    }

    // ==================================================================
    //  1. Salida hacia el checkout
    // ==================================================================

    @GetMapping("/pagos/wompi/{id}")
    public String irAlCheckout(@PathVariable Long id, Authentication auth,
                               Model modelo, RedirectAttributes flash) {

        Optional<Cliente> posible = cliente(auth);
        if (posible.isEmpty()) return "redirect:/login";

        Optional<Pedido> encontrado = pedidoService.porId(id);
        if (encontrado.isEmpty() || !encontrado.get().getCliente().getId().equals(posible.get().getId())) {
            flash.addFlashAttribute("mensaje", "No encontramos ese pedido.");
            return "redirect:/pedidos";
        }
        Pedido pedido = encontrado.get();

        if (!wompi.estaHabilitado()) {
            flash.addFlashAttribute("mensaje",
                    "El pago en linea no esta configurado. Reporta tu pago manualmente.");
            return "redirect:/pedidos/" + id;
        }
        if (pedido.getEstado() != EstadoPedido.PENDIENTE_PAGO) {
            flash.addFlashAttribute("mensaje",
                    "Este pedido ya no esta pendiente de pago.");
            return "redirect:/pedidos/" + id;
        }

        // Referencia nueva en cada intento: Wompi rechaza las repetidas.
        String referencia = wompi.nuevaReferencia(pedido);
        long centavos = pedido.getTotalEnCentavos();
        pedidoService.registrarIntentoPasarela(pedido.getId(), referencia);

        modelo.addAttribute("titulo", "Pagar el pedido " + pedido.getNumero());
        modelo.addAttribute("pedido", pedido);
        modelo.addAttribute("referencia", referencia);
        modelo.addAttribute("centavos", centavos);
        modelo.addAttribute("firma", wompi.firmaIntegridad(referencia, centavos));
        modelo.addAttribute("llavePublica", wompi.getLlavePublica());
        modelo.addAttribute("urlCheckout", wompi.getUrlCheckout());
        modelo.addAttribute("urlRetorno", wompi.getUrlRetorno());
        modelo.addAttribute("moneda", wompi.getMoneda());
        modelo.addAttribute("sandbox", wompi.isSandbox());
        modelo.addAttribute("simulado", wompi.isModoSimulado());
        return wompi.isModoSimulado() ? "pago-simulado" : "pago";
    }

    // ==================================================================
    //  2. Retorno del cliente
    // ==================================================================

    @GetMapping("/pagos/wompi/retorno")
    public String retorno(@RequestParam(name = "id", required = false) String transaccionId,
                          Authentication auth, RedirectAttributes flash) {

        Optional<Cliente> posible = cliente(auth);
        if (posible.isEmpty()) return "redirect:/login";

        if (transaccionId == null || transaccionId.isBlank()) {
            flash.addFlashAttribute("mensaje", "No recibimos el identificador de la transaccion.");
            return "redirect:/pedidos";
        }

        // La URL de retorno NO es fuente de verdad: se le pregunta a Wompi.
        Optional<WompiService.ResultadoPago> resultado = wompi.consultar(transaccionId);
        if (resultado.isEmpty()) {
            flash.addFlashAttribute("mensaje",
                    "No pudimos confirmar el pago con la pasarela. Si el dinero salio de tu cuenta, "
                  + "escribenos y lo verificamos manualmente.");
            return "redirect:/pedidos";
        }

        WompiService.ResultadoPago pago = resultado.get();
        Optional<Pedido> pedido = pedidoService.porReferenciaPasarela(pago.getReferencia());
        if (pedido.isEmpty()) {
            log.warn("Transaccion {} con referencia {} sin pedido asociado",
                    transaccionId, pago.getReferencia());
            flash.addFlashAttribute("mensaje", "No encontramos el pedido de esa transaccion.");
            return "redirect:/pedidos";
        }

        pedidoService.aplicarResultadoPasarela(pedido.get().getId(), transaccionId,
                pago.getEstado(), pago.getMetodo(), pago.getCentavos());

        flash.addFlashAttribute("mensaje", mensajeSegunEstado(pago));
        return "redirect:/pedidos/" + pedido.get().getId();
    }

    // ==================================================================
    //  Pasarela simulada (solo cuando modo=simulado)
    // ==================================================================

    /**
     * Hace de "banco". Recibe el formulario firmado igual que lo recibiria
     * Wompi, valida la firma, y aplica el resultado que elija quien prueba.
     *
     * Se valida la firma aunque sea una simulacion: si no, el ejercicio no
     * demuestra nada. El punto de la firma es justamente que el monto no se
     * pueda alterar en el camino, y eso hay que poder mostrarlo.
     */
    @PostMapping("/pagos/simulado/procesar")
    public String procesarSimulado(@RequestParam String referencia,
                                   @RequestParam long centavos,
                                   @RequestParam String firma,
                                   @RequestParam String resultado,
                                   @RequestParam(required = false) String metodo,
                                   Authentication auth, RedirectAttributes flash) {

        Optional<Cliente> posible = cliente(auth);
        if (posible.isEmpty()) return "redirect:/login";

        if (!wompi.isModoSimulado()) {
            flash.addFlashAttribute("mensaje", "La pasarela simulada no esta activa.");
            return "redirect:/pedidos";
        }

        // Misma comprobacion que haria la pasarela real.
        if (!wompi.firmaValida(referencia, centavos, firma)) {
            log.warn("Pago simulado con firma invalida para la referencia {}", referencia);
            flash.addFlashAttribute("mensaje",
                    "La firma no coincide con el monto. La pasarela rechazo la transaccion, "
                  + "que es exactamente lo que deberia pasar si alguien altera el total.");
            return "redirect:/pedidos";
        }

        Optional<Pedido> pedido = pedidoService.porReferenciaPasarela(referencia);
        if (pedido.isEmpty() || !pedido.get().getCliente().getId().equals(posible.get().getId())) {
            flash.addFlashAttribute("mensaje", "No encontramos el pedido de esa transaccion.");
            return "redirect:/pedidos";
        }

        String transaccionId = "SIM-" + referencia;
        String metodoPago = (metodo == null || metodo.isBlank()) ? "CARD" : metodo;

        pedidoService.aplicarResultadoPasarela(pedido.get().getId(), transaccionId,
                resultado, metodoPago, centavos);

        flash.addFlashAttribute("mensaje", switch (resultado) {
            case "APPROVED" -> "Pago aprobado (simulacion). El equipo lo verifica antes de despachar.";
            case "DECLINED" -> "El banco rechazo el pago (simulacion). Puedes intentar de nuevo.";
            case "PENDING"  -> "El pago quedo en proceso (simulacion).";
            default          -> "La pasarela reporto: " + resultado;
        });
        return "redirect:/pedidos/" + pedido.get().getId();
    }

    // ==================================================================
    //  3. Webhook
    // ==================================================================

    /**
     * Wompi llama a esta URL cuando cambia el estado de una transaccion.
     * Es la via confiable: llega aunque el cliente cierre el navegador antes
     * de volver. Se valida la firma del evento antes de hacer nada.
     */
    @PostMapping("/api/pagos/wompi/eventos")
    @ResponseBody
    public ResponseEntity<String> eventos(@RequestBody String cuerpo) {
        try {
            JsonNode evento = mapper.readTree(cuerpo);

            if (!wompi.firmaEventoValida(evento)) {
                // 401 y no 400: el evento se entiende, pero no se puede confiar en el.
                return ResponseEntity.status(401).body("firma invalida");
            }

            JsonNode transaccion = evento.path("data").path("transaction");
            String referencia = transaccion.path("reference").asText("");
            String estado = transaccion.path("status").asText("");
            String transaccionId = transaccion.path("id").asText("");
            String metodo = transaccion.path("payment_method_type").asText("");
            long centavos = transaccion.path("amount_in_cents").asLong(0);

            Optional<Pedido> pedido = pedidoService.porReferenciaPasarela(referencia);
            if (pedido.isEmpty()) {
                log.warn("Evento de Wompi con referencia desconocida: {}", referencia);
                // 200 igual: si respondemos error, Wompi reintenta en vano.
                return ResponseEntity.ok("referencia desconocida");
            }

            pedidoService.aplicarResultadoPasarela(pedido.get().getId(), transaccionId,
                    estado, metodo, centavos);
            log.info("Evento de Wompi aplicado: {} -> {}", referencia, estado);
            return ResponseEntity.ok("ok");

        } catch (Exception e) {
            log.error("Error procesando el evento de Wompi: {}", e.getMessage());
            return ResponseEntity.status(400).body("evento ilegible");
        }
    }

    // ------------------------------------------------------------------
    private String mensajeSegunEstado(WompiService.ResultadoPago pago) {
        switch (pago.getEstado()) {
            case "APPROVED":
                return "Pago aprobado. El equipo lo verifica antes de despachar.";
            case "DECLINED":
                return "El banco rechazo el pago. Puedes intentar con otro medio.";
            case "VOIDED":
                return "La transaccion fue anulada. No se hizo ningun cobro.";
            case "PENDING":
                return "Tu pago quedo en proceso. Te avisamos apenas la pasarela lo confirme.";
            default:
                return "La pasarela reporto: " + pago.getEstado()
                     + (pago.getMensaje().isBlank() ? "" : " (" + pago.getMensaje() + ")");
        }
    }
}
