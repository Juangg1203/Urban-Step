package com.tiendaropa.controller;

import java.util.List;
import java.util.Optional;

import com.tiendaropa.model.Cliente;
import com.tiendaropa.model.Pedido;
import com.tiendaropa.model.Rol;
import com.tiendaropa.model.SubtipoEmpleado;
import com.tiendaropa.model.Usuario;
import com.tiendaropa.service.CarritoAsistidoService;
import com.tiendaropa.service.ClienteService;
import com.tiendaropa.service.InventarioService;
import com.tiendaropa.service.PedidoService;
import com.tiendaropa.service.ProductoService;
import com.tiendaropa.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Venta asistida: el VENDEDOR arma la compra por un cliente presencial y la
 * confirma en su nombre. El pedido queda asociado al vendedor sin que se lo
 * pregunten (el ya sabe que es el, porque fue quien la hizo), y por eso gana
 * la comision igual que si el cliente lo hubiera elegido en el sitio.
 *
 * Usa un carrito de sesion APARTE (CarritoAsistidoService), distinto del
 * carrito personal del vendedor como comprador: para que armar una venta por
 * otra persona nunca se mezcle con sus propias compras.
 */
@Controller
@RequestMapping("/panel/venta-asistida")
public class VentaAsistidaController {

    private final CarritoAsistidoService carrito;
    private final ClienteService clienteService;
    private final ProductoService productoService;
    private final PedidoService pedidoService;
    private final InventarioService inventario;
    private final UsuarioService usuarioService;

    public VentaAsistidaController(CarritoAsistidoService carrito, ClienteService clienteService,
                                   ProductoService productoService, PedidoService pedidoService,
                                   InventarioService inventario, UsuarioService usuarioService) {
        this.carrito = carrito;
        this.clienteService = clienteService;
        this.productoService = productoService;
        this.pedidoService = pedidoService;
        this.inventario = inventario;
        this.usuarioService = usuarioService;
    }

    private Usuario vendedorActual(Authentication auth) {
        Usuario u = usuarioService.actual(auth).orElse(null);
        return (u != null && u.getRol() == Rol.EMPLEADO && u.getSubtipo() == SubtipoEmpleado.VENDEDOR) ? u : null;
    }

    // ==================================================================
    //  Paso 1: elegir el cliente
    // ==================================================================

    @GetMapping
    public String inicio(@RequestParam(required = false) String q, Authentication auth,
                         Model modelo, RedirectAttributes flash) {
        if (vendedorActual(auth) == null) {
            flash.addFlashAttribute("mensaje", "La venta asistida es una herramienta del vendedor.");
            return "redirect:/panel";
        }
        modelo.addAttribute("titulo", "Venta asistida");
        modelo.addAttribute("busqueda", q);
        modelo.addAttribute("resultados", clienteService.buscarParaVentaAsistida(q));
        modelo.addAttribute("carrito", carrito);
        return "panel/venta-asistida-buscar";
    }

    @PostMapping("/elegir-cliente")
    public String elegirCliente(@RequestParam Long clienteId, Authentication auth,
                                RedirectAttributes flash) {
        if (vendedorActual(auth) == null) return "redirect:/panel";
        Optional<Cliente> cliente = clienteService.porId(clienteId);
        if (cliente.isEmpty()) {
            flash.addFlashAttribute("mensaje", "No encontramos ese cliente.");
            return "redirect:/panel/venta-asistida";
        }
        carrito.elegirCliente(cliente.get().getId(), cliente.get().getNombreCompleto());
        flash.addFlashAttribute("mensaje", "Armando la compra de " + cliente.get().getNombreCompleto() + ".");
        return "redirect:/panel/venta-asistida/armar";
    }

    // ==================================================================
    //  Paso 2: armar el carrito por el cliente
    // ==================================================================

    @GetMapping("/armar")
    public String armar(Authentication auth, Model modelo, RedirectAttributes flash) {
        if (vendedorActual(auth) == null) return "redirect:/panel";
        if (!carrito.isTieneClienteElegido()) {
            flash.addFlashAttribute("mensaje", "Primero busca al cliente.");
            return "redirect:/panel/venta-asistida";
        }
        modelo.addAttribute("titulo", "Venta asistida para " + carrito.getClienteObjetivoNombre());
        modelo.addAttribute("carrito", carrito);
        modelo.addAttribute("productos", productoService.catalogo(null, null, null));
        modelo.addAttribute("faltantes", inventario.verificar(carrito.getItems()));
        return "panel/venta-asistida-armar";
    }

    @PostMapping("/carrito/agregar")
    public String agregar(@RequestParam Long productoId, @RequestParam(required = false) String talla,
                          @RequestParam(defaultValue = "1") int cantidad, Authentication auth,
                          RedirectAttributes flash) {
        if (vendedorActual(auth) == null) return "redirect:/panel";
        productoService.porId(productoId).ifPresent(p ->
                flash.addFlashAttribute("mensaje", carrito.agregar(p, talla, cantidad)));
        return "redirect:/panel/venta-asistida/armar";
    }

    @PostMapping("/carrito/quitar")
    public String quitar(@RequestParam Long productoId, @RequestParam(required = false) String talla,
                         Authentication auth) {
        if (vendedorActual(auth) == null) return "redirect:/panel";
        carrito.quitar(productoId, talla);
        return "redirect:/panel/venta-asistida/armar";
    }

    @PostMapping("/cancelar")
    public String cancelar(Authentication auth, RedirectAttributes flash) {
        if (vendedorActual(auth) == null) return "redirect:/panel";
        carrito.reiniciar();
        flash.addFlashAttribute("mensaje", "Venta asistida cancelada.");
        return "redirect:/panel/venta-asistida";
    }

    // ==================================================================
    //  Paso 3: confirmar
    // ==================================================================

    @PostMapping("/confirmar")
    public String confirmar(@RequestParam(required = false) String direccion,
                            @RequestParam(required = false) String medioPago,
                            @RequestParam(required = false) String observaciones,
                            Authentication auth, RedirectAttributes flash) {
        Usuario vendedor = vendedorActual(auth);
        if (vendedor == null) return "redirect:/panel";
        if (!carrito.isTieneClienteElegido() || carrito.isVacio()) {
            flash.addFlashAttribute("mensaje", "Elige un cliente y agrega productos antes de confirmar.");
            return "redirect:/panel/venta-asistida";
        }

        List<InventarioService.Faltante> faltantes = inventario.verificar(carrito.getItems());
        if (!faltantes.isEmpty()) {
            StringBuilder aviso = new StringBuilder("No se pudo confirmar. ");
            for (InventarioService.Faltante f : faltantes) aviso.append(f.getMensaje()).append(" ");
            flash.addFlashAttribute("mensaje", aviso.toString().trim());
            return "redirect:/panel/venta-asistida/armar";
        }

        Cliente cliente = clienteService.porId(carrito.getClienteObjetivoId()).orElse(null);
        if (cliente == null) {
            flash.addFlashAttribute("mensaje", "Ese cliente ya no existe.");
            carrito.reiniciar();
            return "redirect:/panel/venta-asistida";
        }

        Pedido pedido = pedidoService.crearVentaAsistida(cliente, carrito.getItems(), vendedor,
                direccion, medioPago, observaciones);
        carrito.reiniciar();

        flash.addFlashAttribute("mensaje", "Venta " + pedido.getNumero() + " creada a nombre de "
                + cliente.getNombreCompleto() + ". Va a ganar comision cuando el pago quede confirmado.");
        return "redirect:/panel/pedidos/" + pedido.getId();
    }
}
