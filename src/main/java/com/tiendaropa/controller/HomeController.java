package com.tiendaropa.controller;

import com.tiendaropa.service.ProductoService;
import com.tiendaropa.service.ResenaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {

    private final ProductoService productoService;
    private final ResenaService resenaService;

    public HomeController(ProductoService productoService, ResenaService resenaService) {
        this.productoService = productoService;
        this.resenaService = resenaService;
    }

    @GetMapping("/")
    public String inicio(Model modelo) {
        modelo.addAttribute("destacados", productoService.destacados());
        modelo.addAttribute("titulo", "Ropa y calzado con talla que si queda");
        return "inicio";
    }

    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(required = false) String linea,
                           @RequestParam(required = false) Long categoria,
                           @RequestParam(required = false) String q,
                           Model modelo) {
        modelo.addAttribute("productos", productoService.catalogo(linea, categoria, q));
        modelo.addAttribute("categorias", productoService.categorias());
        modelo.addAttribute("lineaActiva", linea);
        modelo.addAttribute("categoriaActiva", categoria);
        modelo.addAttribute("busqueda", q);
        modelo.addAttribute("titulo", "Catalogo");
        return "catalogo";
    }

    @GetMapping("/producto/{id}")
    public String producto(@PathVariable Long id, Model modelo) {
        return productoService.porId(id)
                .map(p -> {
                    modelo.addAttribute("producto", p);
                    modelo.addAttribute("titulo", p.getNombre());
                    modelo.addAttribute("resenas", resenaService.deProducto(id));
                    modelo.addAttribute("promedioResenas", resenaService.promedioDe(id));
                    modelo.addAttribute("totalResenas", resenaService.cuantasTiene(id));
                    return "producto";
                })
                .orElse("redirect:/catalogo");
    }

    @GetMapping("/politica-datos")
    public String politica(Model modelo) {
        modelo.addAttribute("titulo", "Politica de tratamiento de datos");
        return "politica";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado(Model modelo) {
        modelo.addAttribute("titulo", "Sin permiso");
        return "error/403";
    }
}
