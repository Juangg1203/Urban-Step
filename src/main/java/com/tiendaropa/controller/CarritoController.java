package com.tiendaropa.controller;

import com.tiendaropa.model.Producto;
import com.tiendaropa.service.CarritoService;
import com.tiendaropa.service.InventarioService;
import com.tiendaropa.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Carrito de compras. Es publico a proposito: cualquiera puede armarlo sin
 * cuenta, y el login se pide solo al momento de confirmar. Obligar a
 * registrarse antes de ver el total es la forma mas rapida de perder la venta.
 */
@Controller
@RequestMapping("/carrito")
public class CarritoController {

    private final CarritoService carrito;
    private final ProductoService productoService;
    private final InventarioService inventario;

    public CarritoController(CarritoService carrito, ProductoService productoService,
                             InventarioService inventario) {
        this.carrito = carrito;
        this.productoService = productoService;
        this.inventario = inventario;
    }

    @GetMapping
    public String ver(Model modelo) {
        modelo.addAttribute("titulo", "Tu carrito");
        modelo.addAttribute("carrito", carrito);
        modelo.addAttribute("faltantes", inventario.verificar(carrito.getItems()));
        return "carrito";
    }

    @PostMapping("/agregar")
    public String agregar(@RequestParam Long productoId,
                          @RequestParam(required = false) String talla,
                          @RequestParam(defaultValue = "1") int cantidad,
                          @RequestParam(required = false) String volverA,
                          RedirectAttributes flash) {

        Producto producto = productoService.porId(productoId).orElse(null);
        String mensaje = carrito.agregar(producto, talla, cantidad);
        flash.addFlashAttribute("mensaje", mensaje);

        if (volverA != null && volverA.equals("catalogo")) return "redirect:/catalogo";
        if (producto != null && volverA != null && volverA.equals("producto")) {
            return "redirect:/producto/" + producto.getId();
        }
        return "redirect:/carrito";
    }

    @PostMapping("/cantidad")
    public String cambiarCantidad(@RequestParam Long productoId,
                                  @RequestParam(required = false) String talla,
                                  @RequestParam int cantidad,
                                  RedirectAttributes flash) {
        // Se consulta el inventario real, no la copia que el carrito guardo
        // al agregar: entre una cosa y otra pudo venderse todo.
        int disponible = inventario.disponible(productoId);
        if (cantidad > disponible) {
            flash.addFlashAttribute("mensaje", disponible <= 0
                    ? "Ese producto se agoto. Lo dejamos fuera del carrito."
                    : "Solo quedan " + disponible + " unidades disponibles. Ajustamos la cantidad.");
            cantidad = disponible;
        }
        carrito.cambiarCantidad(productoId, talla, cantidad);
        return "redirect:/carrito";
    }

    @PostMapping("/quitar")
    public String quitar(@RequestParam Long productoId,
                         @RequestParam(required = false) String talla,
                         RedirectAttributes flash) {
        carrito.quitar(productoId, talla);
        flash.addFlashAttribute("mensaje", "Producto retirado del carrito.");
        return "redirect:/carrito";
    }

    @PostMapping("/vaciar")
    public String vaciar(RedirectAttributes flash) {
        carrito.vaciar();
        flash.addFlashAttribute("mensaje", "Tu carrito quedo vacio.");
        return "redirect:/carrito";
    }
}
