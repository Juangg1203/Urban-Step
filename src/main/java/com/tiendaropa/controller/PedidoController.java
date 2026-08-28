package com.tiendaropa.controller;

import java.util.List;
import java.util.Optional;

import com.tiendaropa.model.Cliente;
import com.tiendaropa.model.Pedido;
import com.tiendaropa.model.Rol;
import com.tiendaropa.service.CarritoService;
import com.tiendaropa.service.ClienteService;
import com.tiendaropa.service.ImagenService;
import com.tiendaropa.service.InventarioService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Checkout y seguimiento de pedidos del cliente.
 *
 * "Continuar compra" pasa DIRECTO a pendiente de pago: no hay aprobacion
 * previa. Lo unico que el sistema controla antes de eso es que no compre mas
 * de lo que hay en inventario. Despues del pago, el vendedor y el jefe
 * revisan en cascada; el cliente no ve esa parte, solo el resultado.
 */
@Controller
public class PedidoController {

    private final CarritoService carrito;
    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final ReportePdfService pdfService;
    private final InventarioService inventario;
    private final ImagenService imagenService;
    private final UsuarioService usuarioService;

    public PedidoController(CarritoService carrito, PedidoService pedidoService,
                            ClienteService clienteService, ReportePdfService pdfService,
                            InventarioService inventario, ImagenService imagenService,
                            UsuarioService usuarioService) {
        this.carrito = carrito;
        this.pedidoService = pedidoService;
        this.clienteService = clienteService;
        this.pdfService = pdfService;
        this.inventario = inventario;
        this.imagenService = imagenService;
        this.usuarioService = usuarioService;
    }

    private Optional<Cliente> cliente(Authentication auth) {
        return auth == null ? Optional.empty() : clienteService.porNombreUsuario(auth.getName());
    }

    /**
     * Junta calle + ciudad + pais en una sola direccion. Ciudad y pais se
     * eligen de una lista para no recibir "bogota", "Bogotá D.C." y "BTA"
     * como tres valores distintos del mismo lugar; la calle si es texto
     * libre porque ahi no hay como estandarizar.
     */
    private String armarDireccion(String calle, String ciudad, String pais) {
        StringBuilder texto = new StringBuilder();
        if (calle != null && !calle.isBlank()) texto.append(calle.trim());
        if (ciudad != null && !ciudad.isBlank()) {
            if (texto.length() > 0) texto.append(", ");
            texto.append(ciudad.trim());
        }
        if (pais != null && !pais.isBlank()) {
            if (texto.length() > 0) texto.append(", ");
            texto.append(pais.trim());
        }
        return texto.toString();
    }

    // ==================================================================
    //  Checkout
    // ==================================================================

    @GetMapping("/checkout")
    public String checkout(Authentication auth, Model modelo, RedirectAttributes flash) {
        if (carrito.isVacio()) {
            flash.addFlashAttribute("mensaje", "Tu carrito esta vacio.");
            return "redirect:/catalogo";
        }
        Optional<Cliente> posible = cliente(auth);
        if (posible.isEmpty()) return "redirect:/login";

        modelo.addAttribute("titulo", "Confirmar compra");
        modelo.addAttribute("carrito", carrito);
        modelo.addAttribute("cliente", posible.get());
        modelo.addAttribute("faltantes", inventario.verificar(carrito.getItems()));
        // Lista de vendedores activos, para que el cliente elija quien lo
        // atendio. Es opcional: la comision no es obligatoria en la compra.
        modelo.addAttribute("vendedores", usuarioService.porRol(Rol.EMPLEADO).stream()
                .filter(u -> u.isVendedor() && u.isActivo())
                .toList());
        return "checkout";
    }

    @PostMapping("/checkout")
    public String confirmar(@RequestParam(required = false) String direccion,
                            @RequestParam(required = false) String ciudad,
                            @RequestParam(required = false) String pais,
                            @RequestParam(required = false) String medioPago,
                            @RequestParam(required = false) String observaciones,
                            @RequestParam(defaultValue = "false") boolean soloCotizar,
                            @RequestParam(required = false) Long vendedorId,
                            Authentication auth, RedirectAttributes flash) {

        Optional<Cliente> posible = cliente(auth);
        if (posible.isEmpty()) return "redirect:/login";
        if (carrito.isVacio()) {
            flash.addFlashAttribute("mensaje", "Tu carrito esta vacio.");
            return "redirect:/catalogo";
        }

        // Ciudad y pais se eligen de una lista; la calle se escribe libre.
        // Se arman en una sola direccion aqui, en el servidor, para que la
        // cifren y la guarden igual que siempre, sin tocar el resto del flujo.
        String direccionCompleta = armarDireccion(direccion, ciudad, pais);

        // Segunda verificacion, la que decide: es lo unico que puede frenar
        // la compra. Una cotizacion no compromete inventario, no se valida.
        if (!soloCotizar) {
            List<InventarioService.Faltante> faltantes = inventario.verificar(carrito.getItems());
            if (!faltantes.isEmpty()) {
                StringBuilder aviso = new StringBuilder("No pudimos confirmar tu compra. ");
                for (InventarioService.Faltante f : faltantes) {
                    aviso.append(f.getMensaje()).append(" ");
                }
                aviso.append("Ajusta las cantidades en el carrito e intenta de nuevo.");
                flash.addFlashAttribute("mensaje", aviso.toString().trim());
                return "redirect:/carrito";
            }
        }

        Pedido pedido = pedidoService.crearDesdeCarrito(posible.get(), carrito.getItems(),
                soloCotizar, direccionCompleta, medioPago, observaciones,
                soloCotizar ? null : vendedorId);
        carrito.vaciar();

        flash.addFlashAttribute("mensaje", soloCotizar
                ? "Guardamos tu cotizacion " + pedido.getNumero() + ". Los precios quedan congelados."
                : "Tu pedido " + pedido.getNumero() + " esta listo. Continua para pagarlo.");
        return "redirect:/pedidos/" + pedido.getId();
    }

    // ==================================================================
    //  Seguimiento
    // ==================================================================

    @GetMapping("/pedidos")
    public String misPedidos(Authentication auth, Model modelo) {
        Optional<Cliente> posible = cliente(auth);
        if (posible.isEmpty()) return "redirect:/login";

        modelo.addAttribute("titulo", "Mis pedidos");
        modelo.addAttribute("pedidos", pedidoService.deCliente(posible.get().getId()));
        modelo.addAttribute("cotizaciones", pedidoService.cotizacionesDe(posible.get().getId()));
        return "cliente/pedidos";
    }

    @GetMapping("/pedidos/{id}")
    public String detalle(@PathVariable Long id, Authentication auth,
                          Model modelo, RedirectAttributes flash) {
        Optional<Cliente> posible = cliente(auth);
        if (posible.isEmpty()) return "redirect:/login";

        Optional<Pedido> pedido = pedidoService.porId(id);
        if (pedido.isEmpty() || !pedido.get().getCliente().getId().equals(posible.get().getId())) {
            flash.addFlashAttribute("mensaje", "No encontramos ese pedido.");
            return "redirect:/pedidos";
        }

        modelo.addAttribute("titulo", "Pedido " + pedido.get().getNumero());
        modelo.addAttribute("pedido", pedido.get());
        return "cliente/pedido-detalle";
    }

    /** El cliente descarga su propia orden. Solo la suya. */
    @GetMapping("/pedidos/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id, Authentication auth) {
        Optional<Cliente> posible = cliente(auth);
        if (posible.isEmpty()) return ResponseEntity.status(403).build();

        Optional<Pedido> pedido = pedidoService.porId(id);
        if (pedido.isEmpty() || !pedido.get().getCliente().getId().equals(posible.get().getId())) {
            return ResponseEntity.notFound().build();
        }
        byte[] contenido = pdfService.ordenPedido(pedido.get());
        String archivo = "pedido-" + pedido.get().getNumero() + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenido);
    }

    @PostMapping("/pedidos/{id}/enviar")
    public String enviarCotizacion(@PathVariable Long id, Authentication auth,
                                   RedirectAttributes flash) {
        Optional<Cliente> posible = cliente(auth);
        if (posible.isEmpty()) return "redirect:/login";

        boolean ok = pedidoService.enviarCotizacion(id, posible.get().getId());
        flash.addFlashAttribute("mensaje", ok
                ? "Tu cotizacion esta lista para pagar."
                : "Esa cotizacion ya no se puede enviar.");
        return "redirect:/pedidos/" + id;
    }

    // ==================================================================
    //  Pago manual y comprobante
    // ==================================================================

    @PostMapping("/pedidos/{id}/pagar")
    public String reportarPago(@PathVariable Long id,
                               @RequestParam String referenciaPago,
                               @RequestParam(required = false) String medioPago,
                               @RequestParam(required = false) MultipartFile comprobante,
                               Authentication auth, RedirectAttributes flash) {
        Optional<Cliente> posible = cliente(auth);
        if (posible.isEmpty()) return "redirect:/login";

        String nombreArchivo;
        try {
            nombreArchivo = imagenService.guardar(comprobante);
        } catch (ImagenService.ImagenInvalida e) {
            flash.addFlashAttribute("mensaje", e.getMessage());
            return "redirect:/pedidos/" + id;
        }

        boolean ok = pedidoService.reportarPago(id, posible.get().getId(), referenciaPago,
                medioPago, nombreArchivo);
        flash.addFlashAttribute("mensaje", ok
                ? "Recibimos tu pago" + (nombreArchivo != null ? " y el comprobante" : "")
                  + ". El equipo lo verifica y te avisamos."
                : "Ese pedido no esta en estado de pago.");
        return "redirect:/pedidos/" + id;
    }

    // ==================================================================
    //  Confirmar recepcion (dispara ENTREGADO y habilita la resena)
    // ==================================================================

    @PostMapping("/pedidos/{id}/recibido")
    public String confirmarRecepcion(@PathVariable Long id,
                                     @RequestParam(required = false) MultipartFile foto,
                                     Authentication auth, RedirectAttributes flash) {
        Optional<Cliente> posible = cliente(auth);
        if (posible.isEmpty()) return "redirect:/login";

        String nombreFoto;
        try {
            nombreFoto = imagenService.guardar(foto);
        } catch (ImagenService.ImagenInvalida e) {
            flash.addFlashAttribute("mensaje", e.getMessage());
            return "redirect:/pedidos/" + id;
        }

        boolean ok = pedidoService.confirmarRecepcion(id, posible.get().getId(), nombreFoto);
        flash.addFlashAttribute("mensaje", ok
                ? "Gracias por confirmar. Ya puedes dejar tu resena de los productos."
                : "Ese pedido no esta en estado de despachado.");
        return "redirect:/pedidos/" + id;
    }

    @PostMapping("/pedidos/{id}/resena")
    public String dejarResena(@PathVariable Long id,
                              @RequestParam Long productoId,
                              @RequestParam int calificacion,
                              @RequestParam(required = false) String comentario,
                              Authentication auth, RedirectAttributes flash) {
        Optional<Cliente> posible = cliente(auth);
        if (posible.isEmpty()) return "redirect:/login";

        boolean ok = pedidoService.dejarResena(id, posible.get().getId(), productoId,
                calificacion, comentario);
        flash.addFlashAttribute("mensaje", ok
                ? "Gracias por tu resena."
                : "No fue posible guardar la resena. Revisa que el pedido este entregado "
                + "y que no la hayas dejado antes.");
        return "redirect:/pedidos/" + id;
    }

    @PostMapping("/pedidos/{id}/cancelar")
    public String cancelar(@PathVariable Long id,
                           @RequestParam(required = false) String motivo,
                           Authentication auth, RedirectAttributes flash) {
        Optional<Cliente> posible = cliente(auth);
        if (posible.isEmpty()) return "redirect:/login";

        boolean ok = pedidoService.cancelar(id, posible.get().getId(), motivo);
        flash.addFlashAttribute("mensaje", ok
                ? "Cancelamos tu pedido."
                : "Ese pedido ya avanzo demasiado para cancelarlo. Escribenos por el chat.");
        return "redirect:/pedidos/" + id;
    }
}
