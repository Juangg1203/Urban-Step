package com.tiendaropa.controller;

import java.math.BigDecimal;
import java.util.Optional;

import com.tiendaropa.model.Categoria;
import com.tiendaropa.model.NivelDato;
import com.tiendaropa.model.Producto;
import com.tiendaropa.service.AuditoriaService;
import com.tiendaropa.service.ImagenService;
import com.tiendaropa.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CRUD de productos. Restringido a ADMIN y JEFE en SecurityConfig.
 *
 * El catalogo publico lee siempre de la base; no hay ni un producto escrito
 * en el HTML. Lo que se cree aqui aparece de inmediato en la tienda.
 */
@Controller
@RequestMapping("/panel/productos")
public class ProductoAdminController {

    private static final int STOCK_BAJO = 5;

    private final ProductoService productoService;
    private final ImagenService imagenService;
    private final AuditoriaService auditoria;

    public ProductoAdminController(ProductoService productoService, ImagenService imagenService,
                                   AuditoriaService auditoria) {
        this.productoService = productoService;
        this.imagenService = imagenService;
        this.auditoria = auditoria;
    }

    // ==================================================================
    //  Consultar
    // ==================================================================

    @GetMapping
    public String listar(@RequestParam(required = false) String q, Model modelo) {
        modelo.addAttribute("titulo", "Productos");
        modelo.addAttribute("productos", productoService.listarTodos(q));
        modelo.addAttribute("busqueda", q);
        modelo.addAttribute("totalProductos", productoService.total());
        modelo.addAttribute("activos", productoService.activos());
        modelo.addAttribute("stockBajo", productoService.conStockBajo(STOCK_BAJO));
        modelo.addAttribute("limiteStockBajo", STOCK_BAJO);
        return "panel/productos";
    }

    // ==================================================================
    //  Crear y editar
    // ==================================================================

    @GetMapping("/nuevo")
    public String formularioNuevo(Model modelo) {
        modelo.addAttribute("titulo", "Nuevo producto");
        modelo.addAttribute("producto", new Producto());
        modelo.addAttribute("categorias", productoService.categorias());
        modelo.addAttribute("esNuevo", true);
        return "panel/producto-form";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model modelo, RedirectAttributes flash) {
        Optional<Producto> producto = productoService.porId(id);
        if (producto.isEmpty()) {
            flash.addFlashAttribute("mensaje", "Ese producto no existe.");
            return "redirect:/panel/productos";
        }
        modelo.addAttribute("titulo", "Editar " + producto.get().getNombre());
        modelo.addAttribute("producto", producto.get());
        modelo.addAttribute("categorias", productoService.categorias());
        modelo.addAttribute("esNuevo", false);
        modelo.addAttribute("tieneMovimientos", productoService.tieneMovimientos(id));
        return "panel/producto-form";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam String nombre,
                          @RequestParam(required = false) String descripcion,
                          @RequestParam Long categoriaId,
                          @RequestParam BigDecimal precio,
                          @RequestParam(required = false) String tallas,
                          @RequestParam(required = false) String color,
                          @RequestParam(required = false) String material,
                          @RequestParam(defaultValue = "0") int stock,
                          @RequestParam(defaultValue = "5") int stockMinimo,
                          @RequestParam(defaultValue = "0") java.math.BigDecimal comisionPct,
                          @RequestParam(defaultValue = "false") boolean activo,
                          @RequestParam(required = false) String imagenUrl,
                          @RequestParam(required = false) MultipartFile archivoImagen,
                          RedirectAttributes flash) {

        // ---- validaciones que el navegador no alcanza a cubrir ----
        if (precio == null || precio.signum() < 0) {
            flash.addFlashAttribute("mensaje", "El precio no puede ser negativo.");
            return volverAlFormulario(id);
        }
        if (stock < 0) {
            flash.addFlashAttribute("mensaje", "La cantidad disponible no puede ser negativa.");
            return volverAlFormulario(id);
        }
        Optional<Categoria> categoria = productoService.categoriaPorId(categoriaId);
        if (categoria.isEmpty()) {
            flash.addFlashAttribute("mensaje", "La categoria seleccionada ya no existe.");
            return volverAlFormulario(id);
        }

        Producto producto = (id == null)
                ? new Producto()
                : productoService.porId(id).orElse(new Producto());

        String imagenAnterior = producto.getImagen();

        // La referencia es automatica, atada a la categoria: PREFIJO-NNN.
        // En un producto nuevo siempre se genera. En uno existente, solo se
        // vuelve a generar si la categoria cambio; si no, conserva la que
        // ya tenia, para no reescribir un SKU que ya esta impreso o citado.
        Long categoriaAnteriorId = producto.getCategoria() == null ? null : producto.getCategoria().getId();
        boolean necesitaNuevoSku = id == null || !categoriaId.equals(categoriaAnteriorId);
        if (necesitaNuevoSku) {
            producto.setSku(productoService.generarSku(categoria.get()));
        }

        producto.setNombre(nombre.trim());
        producto.setDescripcion(descripcion);
        producto.setCategoria(categoria.get());
        producto.setPrecio(precio);
        producto.setTallas(tallas);
        producto.setColor(color);
        producto.setMaterial(material);
        producto.setStock(stock);
        producto.setStockMinimo(Math.max(0, stockMinimo));
        // El porcentaje se limita entre 0 y 100: un valor fuera de rango no tiene sentido de negocio.
        java.math.BigDecimal comisionValida = comisionPct == null ? java.math.BigDecimal.ZERO
                : comisionPct.max(java.math.BigDecimal.ZERO).min(new java.math.BigDecimal("100"));
        producto.setComisionPct(comisionValida);
        producto.setActivo(activo);

        // ---- imagen: archivo subido o URL externa ----
        try {
            String subida = imagenService.guardar(archivoImagen);
            if (subida != null) {
                producto.setImagen(subida);
                // La anterior ya no la usa nadie: se borra para no dejar basura.
                if (imagenAnterior != null && !imagenAnterior.equals(subida)) {
                    imagenService.borrar(imagenAnterior);
                }
            } else if (imagenUrl != null && !imagenUrl.isBlank()) {
                producto.setImagen(imagenUrl.trim());
            }
        } catch (ImagenService.ImagenInvalida e) {
            flash.addFlashAttribute("mensaje", e.getMessage());
            return volverAlFormulario(id);
        }

        productoService.guardar(producto);

        auditoria.registrar(id == null ? "PRODUCTO_CREADO" : "PRODUCTO_EDITADO",
                NivelDato.PUBLICO, "Producto", producto.getId(),
                producto.getSku() + " - " + producto.getNombre());

        flash.addFlashAttribute("mensaje", id == null
                ? "Producto \"" + producto.getNombre() + "\" creado. Ya aparece en el catalogo."
                : "Producto \"" + producto.getNombre() + "\" actualizado.");
        return "redirect:/panel/productos";
    }

    // ==================================================================
    //  Eliminar y dar de baja
    // ==================================================================

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id, RedirectAttributes flash) {
        productoService.desactivar(id);
        auditoria.registrar("PRODUCTO_DESACTIVADO", NivelDato.PUBLICO, "Producto", id,
                "Retirado del catalogo");
        flash.addFlashAttribute("mensaje", "Producto retirado del catalogo. Sigue en el historial.");
        return "redirect:/panel/productos";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id, RedirectAttributes flash) {
        productoService.activar(id);
        auditoria.registrar("PRODUCTO_ACTIVADO", NivelDato.PUBLICO, "Producto", id,
                "Publicado en el catalogo");
        flash.addFlashAttribute("mensaje", "Producto publicado de nuevo en el catalogo.");
        return "redirect:/panel/productos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        Optional<Producto> producto = productoService.porId(id);
        if (producto.isEmpty()) {
            flash.addFlashAttribute("mensaje", "Ese producto ya no existe.");
            return "redirect:/panel/productos";
        }
        String nombre = producto.get().getNombre();
        String imagen = producto.get().getImagen();

        if (!productoService.eliminar(id)) {
            // Borrarlo dejaria pedidos apuntando a la nada: se ofrece la baja logica.
            flash.addFlashAttribute("mensaje", "\"" + nombre + "\" ya tiene ventas registradas, "
                    + "asi que no se puede borrar sin romper el historial. Usa \"Retirar\" en su lugar.");
            return "redirect:/panel/productos";
        }
        imagenService.borrar(imagen);
        auditoria.registrar("PRODUCTO_ELIMINADO", NivelDato.PUBLICO, "Producto", id, nombre);
        flash.addFlashAttribute("mensaje", "Producto \"" + nombre + "\" eliminado definitivamente.");
        return "redirect:/panel/productos";
    }

    // ------------------------------------------------------------------
    private String volverAlFormulario(Long id) {
        return id == null ? "redirect:/panel/productos/nuevo"
                          : "redirect:/panel/productos/" + id + "/editar";
    }
}
