package com.tiendaropa.service;

import java.util.List;
import java.util.Optional;
import com.tiendaropa.model.Categoria;
import com.tiendaropa.model.Producto;
import com.tiendaropa.repository.CategoriaRepository;
import com.tiendaropa.repository.ProductoRepository;
import com.tiendaropa.repository.CompraClienteRepository;
import com.tiendaropa.repository.ItemPedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepo;
    private final CategoriaRepository categoriaRepo;
    private final ItemPedidoRepository itemPedidoRepo;
    private final CompraClienteRepository compraRepo;

    public ProductoService(ProductoRepository productoRepo, CategoriaRepository categoriaRepo,
                           ItemPedidoRepository itemPedidoRepo, CompraClienteRepository compraRepo) {
        this.productoRepo = productoRepo;
        this.categoriaRepo = categoriaRepo;
        this.itemPedidoRepo = itemPedidoRepo;
        this.compraRepo = compraRepo;
    }

    public List<Producto> catalogo(String linea, Long categoriaId, String texto) {
        if (texto != null && !texto.isBlank()) return productoRepo.buscar(texto);
        if (categoriaId != null) return productoRepo.findByActivoTrueAndCategoriaId(categoriaId);
        if (linea != null && !linea.isBlank()) return productoRepo.findByActivoTrueAndCategoriaLinea(linea);
        return productoRepo.findByActivoTrue();
    }

    public List<Producto> destacados() {
        List<Producto> todos = productoRepo.findByActivoTrue();
        return todos.size() > 6 ? todos.subList(0, 6) : todos;
    }

    public Optional<Producto> porId(Long id) { return productoRepo.findById(id); }
    public List<Categoria> categorias() { return categoriaRepo.findAll(); }
    public long total() { return productoRepo.count(); }

    // ==================================================================
    //  CRUD del panel
    // ==================================================================

    /** Listado del panel: incluye los inactivos, que el catalogo no muestra. */
    public List<Producto> listarTodos(String texto) {
        return (texto == null || texto.isBlank())
                ? productoRepo.findAllByOrderByNombreAsc()
                : productoRepo.buscarTodos(texto);
    }

    public Optional<Categoria> categoriaPorId(Long id) { return categoriaRepo.findById(id); }

    public boolean skuOcupado(String sku, Long idActual) {
        return productoRepo.findBySku(sku)
                .filter(p -> idActual == null || !p.getId().equals(idActual))
                .isPresent();
    }

    /**
     * Genera la referencia a partir de la categoria: PREFIJO-NNN, por ejemplo
     * CAM-001 para la primera camiseta o PANT-011 para el pantalon numero 11.
     *
     * Si se borro un producto y quedo un hueco (por ejemplo existen CAM-001 y
     * CAM-003, pero no CAM-002), el siguiente producto de esa categoria ocupa
     * ese hueco en vez de saltar a CAM-004. Es lo mismo que hacer una fila:
     * el que llega toma el primer puesto libre, no el ultimo.
     */
    public String generarSku(Categoria categoria) {
        String prefijo = prefijoDe(categoria.getNombre());
        List<Producto> existentes = productoRepo.findBySkuStartingWithOrderBySkuAsc(prefijo + "-");

        java.util.SortedSet<Integer> numerosUsados = new java.util.TreeSet<>();
        for (Producto p : existentes) {
            String resto = p.getSku().substring(prefijo.length() + 1);
            try {
                numerosUsados.add(Integer.parseInt(resto));
            } catch (NumberFormatException ignorado) {
                // Un SKU que no sigue el patron (dato antiguo o manual) no cuenta.
            }
        }

        int candidato = 1;
        while (numerosUsados.contains(candidato)) candidato++;

        return prefijo + "-" + String.format("%03d", candidato);
    }

    /**
     * Prefijo a partir del nombre de la categoria. Hay un diccionario para
     * las categorias conocidas (para que "Pantalones" de PANT y no PAN), y
     * un respaldo generico por si se agrega una categoria nueva.
     */
    private String prefijoDe(String nombreCategoria) {
        String limpio = java.text.Normalizer.normalize(nombreCategoria.toLowerCase().trim(),
                java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");

        java.util.Map<String, String> conocidas = java.util.Map.of(
                "camisetas", "CAM",
                "pantalones", "PANT",
                "chaquetas", "CHA",
                "tenis", "TEN",
                "botas", "BOT",
                "sandalias", "SAN");
        if (conocidas.containsKey(limpio)) return conocidas.get(limpio);

        String soloLetras = limpio.replaceAll("[^a-z]", "").toUpperCase();
        return soloLetras.length() >= 3 ? soloLetras.substring(0, 3) : "PRD";
    }

    @Transactional
    public Producto guardar(Producto producto) {
        return productoRepo.save(producto);
    }

    /**
     * Baja logica: el producto deja de verse en el catalogo pero sigue en la
     * base. Si se borrara de verdad, los pedidos que lo incluyen quedarian
     * apuntando a la nada y el historial dejaria de cuadrar.
     */
    @Transactional
    public void desactivar(Long id) {
        productoRepo.findById(id).ifPresent(p -> {
            p.setActivo(false);
            productoRepo.save(p);
        });
    }

    @Transactional
    public void activar(Long id) {
        productoRepo.findById(id).ifPresent(p -> {
            p.setActivo(true);
            productoRepo.save(p);
        });
    }

    /**
     * Borrado definitivo. Solo se permite si el producto nunca se vendio;
     * en caso contrario se avisa y se sugiere desactivarlo.
     */
    @Transactional
    public boolean eliminar(Long id) {
        Optional<Producto> posible = productoRepo.findById(id);
        if (posible.isEmpty()) return false;
        if (tieneMovimientos(id)) return false;
        productoRepo.deleteById(id);
        return true;
    }

    public boolean tieneMovimientos(Long productoId) {
        return itemPedidoRepo.existsByProductoId(productoId)
            || compraRepo.existsByProductoId(productoId);
    }

    public long activos() { return productoRepo.countByActivoTrue(); }

    public long conStockBajo(int limite) {
        return productoRepo.countByActivoTrueAndStockLessThanEqual(limite);
    }
}
