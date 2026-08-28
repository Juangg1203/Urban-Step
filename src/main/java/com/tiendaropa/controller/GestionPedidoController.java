package com.tiendaropa.controller;

import java.util.Optional;

import com.tiendaropa.model.EstadoPedido;
import com.tiendaropa.model.Pedido;
import com.tiendaropa.model.Rol;
import com.tiendaropa.model.SubtipoEmpleado;
import com.tiendaropa.model.Usuario;
import com.tiendaropa.service.ComisionService;
import com.tiendaropa.service.NotificacionService;
import com.tiendaropa.service.PedidoService;
import com.tiendaropa.service.ReportePdfService;
import com.tiendaropa.service.UsuarioService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Bandeja interna de pedidos.
 *
 * Cada rol ve solo lo que le corresponde, y no solo por seguridad de rutas:
 * la consulta misma trae bandejas distintas segun quien pregunta.
 *
 *  - /panel/aprobaciones : JEFE. Pedidos con el pago ya verificado, en
 *    espera del visto bueno final antes de pasar a bodega.
 *  - /panel/pedidos      : EMPLEADO (vendedor o bodeguero) y ADMIN.
 *      · Vendedor ve solo pagos por confirmar.
 *      · Bodeguero ve solo despachos, y el respaldo por si el cliente no
 *        confirma la entrega.
 *      · Administrador ve las dos bandejas, para su funcion de supervision,
 *        pero no puede confirmar pagos ni despachar: eso es operativo, no
 *        administrativo.
 *
 * Quien ejecuta cada accion queda guardado en el pedido y en la auditoria.
 * Un pedido nunca cambia de estado solo.
 */
@Controller
@RequestMapping("/panel")
public class GestionPedidoController {

    private final PedidoService pedidoService;
    private final NotificacionService notificaciones;
    private final UsuarioService usuarioService;
    private final ReportePdfService pdfService;
    private final ComisionService comisionService;

    public GestionPedidoController(PedidoService pedidoService, NotificacionService notificaciones,
                                   UsuarioService usuarioService, ReportePdfService pdfService,
                                   ComisionService comisionService) {
        this.pedidoService = pedidoService;
        this.notificaciones = notificaciones;
        this.usuarioService = usuarioService;
        this.pdfService = pdfService;
        this.comisionService = comisionService;
    }

    private Usuario actual(Authentication auth) {
        return usuarioService.actual(auth).orElse(null);
    }

    // ==================================================================
    //  Jefe: visto bueno final (despues del pago verificado, no antes)
    // ==================================================================

    @GetMapping("/aprobaciones")
    public String aprobaciones(Model modelo) {
        modelo.addAttribute("titulo", "Compras por aceptar");
        modelo.addAttribute("pendientes", pedidoService.pendientesDeAceptacion());
        modelo.addAttribute("avisos", notificaciones.paraRol(Rol.JEFE));
        notificaciones.marcarTodasLeidas(Rol.JEFE);
        return "panel/aprobaciones";
    }

    @PostMapping("/aprobaciones/{id}/aprobar")
    public String aceptar(@PathVariable Long id, @RequestParam(required = false) String nota,
                          Authentication auth, RedirectAttributes flash) {
        boolean ok = pedidoService.aceptar(id, actual(auth), nota);
        flash.addFlashAttribute("mensaje", ok
                ? "Pedido aceptado. Bodega ya lo puede alistar."
                : "Ese pedido ya no estaba esperando tu aceptacion.");
        return "redirect:/panel/aprobaciones";
    }

    @PostMapping("/aprobaciones/{id}/rechazar")
    public String rechazar(@PathVariable Long id, @RequestParam String motivo,
                           Authentication auth, RedirectAttributes flash) {
        if (motivo == null || motivo.isBlank()) {
            flash.addFlashAttribute("mensaje", "Escribe el motivo del rechazo: el cliente merece saberlo.");
            return "redirect:/panel/aprobaciones";
        }
        boolean ok = pedidoService.rechazar(id, actual(auth), motivo);
        flash.addFlashAttribute("mensaje", ok
                ? "Pedido rechazado. Las unidades vuelven al inventario."
                : "Ese pedido ya no estaba esperando tu aceptacion.");
        return "redirect:/panel/aprobaciones";
    }

    // ==================================================================
    //  Bandeja de pedidos: cada uno ve solo lo suyo
    // ==================================================================

    @GetMapping("/pedidos")
    public String pedidos(Authentication auth, Model modelo) {
        Usuario yo = actual(auth);
        modelo.addAttribute("titulo", "Pedidos");
        modelo.addAttribute("esVendedor", esVendedor(yo));
        modelo.addAttribute("esBodeguero", esBodeguero(yo));
        modelo.addAttribute("esAdmin", yo != null && yo.getRol() == Rol.ADMIN);

        // La consulta misma trae bandejas distintas: no es solo un filtro
        // visual, es lo que impide que un vendedor vea el trabajo de bodega.
        if (esVendedor(yo) || (yo != null && yo.getRol() == Rol.ADMIN)) {
            modelo.addAttribute("porVerificar", pedidoService.porVerificarPago());
        }
        if (esBodeguero(yo) || (yo != null && yo.getRol() == Rol.ADMIN)) {
            modelo.addAttribute("porDespachar", pedidoService.porDespachar());
            modelo.addAttribute("porConfirmarEntrega", pedidoService.porConfirmarEntrega());
        }
        if (yo != null && yo.getRol() == Rol.ADMIN) {
            modelo.addAttribute("recientes", pedidoService.recientes());
        }
        return "panel/pedidos";
    }

    @GetMapping("/pedidos/{id}")
    public String detalle(@PathVariable Long id, Model modelo, RedirectAttributes flash) {
        Optional<Pedido> pedido = pedidoService.porId(id);
        if (pedido.isEmpty()) {
            flash.addFlashAttribute("mensaje", "Ese pedido no existe.");
            return "redirect:/panel/pedidos";
        }
        modelo.addAttribute("titulo", "Pedido " + pedido.get().getNumero());
        modelo.addAttribute("pedido", pedido.get());
        modelo.addAttribute("estadosPosibles", EstadoPedido.values());
        return "panel/pedido-detalle";
    }

    @GetMapping("/pedidos/{id}/pdf")
    public ResponseEntity<byte[]> pdfPedido(@PathVariable Long id) {
        Optional<Pedido> pedido = pedidoService.porId(id);
        if (pedido.isEmpty()) return ResponseEntity.notFound().build();

        byte[] contenido = pdfService.ordenPedido(pedido.get());
        String archivo = "orden-" + pedido.get().getNumero() + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenido);
    }

    // ==================================================================
    //  Historial por rol
    // ==================================================================

    @GetMapping("/historial")
    public String historial(Model modelo, Authentication auth) {
        Usuario yo = actual(auth);
        modelo.addAttribute("titulo", "Historial");
        modelo.addAttribute("pedidos", pedidoService.recientes());

        if (yo != null && (yo.getRol() == Rol.ADMIN || yo.getRol() == Rol.JEFE)) {
            modelo.addAttribute("usuarios", usuarioService.todos());
        }
        return "panel/historial";
    }

    // ==================================================================
    //  Vendedor: verifica el pago. El bodeguero no llega a este metodo
    //  aunque adivine la URL, porque se comprueba el subtipo aqui.
    // ==================================================================

    @PostMapping("/pedidos/{id}/confirmar-pago")
    public String confirmarPago(@PathVariable Long id, Authentication auth, RedirectAttributes flash) {
        Usuario yo = actual(auth);
        if (!esVendedor(yo)) {
            flash.addFlashAttribute("mensaje", "Confirmar pagos es tarea del vendedor.");
            return "redirect:/panel/pedidos";
        }
        boolean ok = pedidoService.confirmarPago(id, yo);
        flash.addFlashAttribute("mensaje", ok
                ? "Pago confirmado. Pasa al jefe para el visto bueno final."
                : "Ese pedido no tenia un pago por verificar.");
        return "redirect:/panel/pedidos";
    }

    // ==================================================================
    //  Bodeguero: alistar, despachar y el respaldo de entrega
    // ==================================================================

    @PostMapping("/pedidos/{id}/alistar")
    public String alistar(@PathVariable Long id, Authentication auth, RedirectAttributes flash) {
        Usuario yo = actual(auth);
        if (!esBodeguero(yo)) {
            flash.addFlashAttribute("mensaje", "Alistar pedidos es tarea de bodega.");
            return "redirect:/panel/pedidos";
        }
        boolean ok = pedidoService.alistar(id, yo);
        flash.addFlashAttribute("mensaje", ok
                ? "Pedido en preparacion."
                : "Ese pedido no esta listo para alistar.");
        return "redirect:/panel/pedidos";
    }

    @PostMapping("/pedidos/{id}/despachar")
    public String despachar(@PathVariable Long id, @RequestParam String guia,
                            Authentication auth, RedirectAttributes flash) {
        Usuario yo = actual(auth);
        if (!esBodeguero(yo)) {
            flash.addFlashAttribute("mensaje", "Despachar pedidos es tarea de bodega.");
            return "redirect:/panel/pedidos";
        }
        boolean ok = pedidoService.despachar(id, yo, guia);
        flash.addFlashAttribute("mensaje", ok
                ? "Pedido despachado con guia " + guia + "."
                : "Ese pedido no se puede despachar todavia.");
        return "redirect:/panel/pedidos";
    }

    /** Respaldo: si el cliente no confirma la recepcion, bodega puede cerrarlo. */
    @PostMapping("/pedidos/{id}/entregado")
    public String entregado(@PathVariable Long id, Authentication auth, RedirectAttributes flash) {
        Usuario yo = actual(auth);
        if (!esBodeguero(yo)) {
            flash.addFlashAttribute("mensaje", "Cerrar la entrega es tarea de bodega.");
            return "redirect:/panel/pedidos";
        }
        boolean ok = pedidoService.marcarEntregadoPorStaff(id, yo);
        flash.addFlashAttribute("mensaje", ok
                ? "Entrega cerrada. El cliente no la habia confirmado."
                : "Ese pedido todavia no ha sido despachado.");
        return "redirect:/panel/pedidos";
    }

    // ==================================================================
    //  Administrador: forzar un estado, con motivo y auditoria
    // ==================================================================

    @PostMapping("/pedidos/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam EstadoPedido estado,
                                @RequestParam(required = false) String motivo,
                                Authentication auth, RedirectAttributes flash) {
        Usuario yo = actual(auth);
        if (yo == null || yo.getRol() != Rol.ADMIN) {
            flash.addFlashAttribute("mensaje", "Solo el administrador puede forzar un cambio de estado.");
            return "redirect:/panel/pedidos/" + id;
        }
        boolean ok = pedidoService.cambiarEstado(id, estado, yo, motivo);
        flash.addFlashAttribute("mensaje", ok
                ? "Estado cambiado a \"" + estado.getEtiqueta() + "\". Queda registrado en la auditoria."
                : "No se pudo cambiar el estado: ya estaba en ese estado o el pedido no existe.");
        return "redirect:/panel/pedidos/" + id;
    }

    // ==================================================================
    //  Comisiones del vendedor
    // ==================================================================

    @GetMapping("/mis-comisiones")
    public String misComisiones(Authentication auth, Model modelo, RedirectAttributes flash) {
        Usuario yo = actual(auth);
        if (!esVendedor(yo)) {
            flash.addFlashAttribute("mensaje", "Las comisiones son del vendedor.");
            return "redirect:/panel";
        }
        modelo.addAttribute("titulo", "Mis comisiones");
        modelo.addAttribute("pedidos", comisionService.historialDe(yo.getId()));
        modelo.addAttribute("confirmada", comisionService.confirmadaDe(yo.getId()));
        modelo.addAttribute("pendiente", comisionService.pendienteDe(yo.getId()));
        modelo.addAttribute("total", comisionService.totalDe(yo.getId()));
        return "panel/mis-comisiones";
    }

    // ------------------------------------------------------------------
    private boolean esVendedor(Usuario u) {
        return u != null && u.getRol() == Rol.EMPLEADO && u.getSubtipo() == SubtipoEmpleado.VENDEDOR;
    }

    private boolean esBodeguero(Usuario u) {
        return u != null && u.getRol() == Rol.EMPLEADO && u.getSubtipo() == SubtipoEmpleado.BODEGUERO;
    }
}
