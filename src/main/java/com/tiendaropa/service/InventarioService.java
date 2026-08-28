package com.tiendaropa.service;

import java.util.ArrayList;
import java.util.List;

import com.tiendaropa.dto.ItemCarrito;
import com.tiendaropa.model.ItemPedido;
import com.tiendaropa.model.Pedido;
import com.tiendaropa.model.Producto;
import com.tiendaropa.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Control de inventario.
 *
 * Tres reglas que conviene tener claras:
 *
 * 1. La cantidad que el carrito guardo al agregar es una foto de ese momento.
 *    Entre que el cliente agrego el producto y confirma la compra pudo pasar
 *    media hora, asi que la disponibilidad SE VUELVE A VERIFICAR contra la base
 *    justo antes de crear el pedido. Confiar en la copia del carrito es como
 *    vender la misma silla dos veces.
 *
 * 2. El stock se descuenta cuando el pedido se genera, y se devuelve si el
 *    jefe lo rechaza o el cliente lo cancela. Mientras el pedido vive, esas
 *    unidades estan comprometidas y no se le pueden prometer a nadie mas.
 *
 * 3. Nunca se deja el stock en negativo: si algo se descuadra, se corta en
 *    cero y queda en el log, porque un inventario negativo es un error de
 *    datos que despues nadie sabe explicar.
 */
@Service
public class InventarioService {

    private static final Logger log = LoggerFactory.getLogger(InventarioService.class);

    private final ProductoRepository productoRepo;

    public InventarioService(ProductoRepository productoRepo) {
        this.productoRepo = productoRepo;
    }

    /** Un problema concreto de disponibilidad, con el texto que vera el cliente. */
    public static class Faltante {
        private final String producto;
        private final int pedida;
        private final int disponible;

        public Faltante(String producto, int pedida, int disponible) {
            this.producto = producto;
            this.pedida = pedida;
            this.disponible = disponible;
        }
        public String getProducto() { return producto; }
        public int getPedida() { return pedida; }
        public int getDisponible() { return disponible; }

        public String getMensaje() {
            if (disponible <= 0) {
                return "\"" + producto + "\" se agoto mientras estaba en tu carrito.";
            }
            return "De \"" + producto + "\" pediste " + pedida
                 + " y solo quedan " + disponible + ".";
        }
    }

    // ==================================================================
    //  Verificacion
    // ==================================================================

    /**
     * Contrasta el carrito contra el inventario real. Lista vacia = todo bien.
     */
    public List<Faltante> verificar(List<ItemCarrito> lineas) {
        List<Faltante> faltantes = new ArrayList<>();
        for (ItemCarrito linea : lineas) {
            Producto producto = productoRepo.findById(linea.getProductoId()).orElse(null);
            if (producto == null || !producto.isActivo()) {
                faltantes.add(new Faltante(linea.getNombre(), linea.getCantidad(), 0));
                continue;
            }
            if (producto.getStock() < linea.getCantidad()) {
                faltantes.add(new Faltante(producto.getNombre(),
                        linea.getCantidad(), producto.getStock()));
            }
        }
        return faltantes;
    }

    /** Cuantas unidades se pueden vender de un producto ahora mismo. */
    public int disponible(Long productoId) {
        return productoRepo.findById(productoId)
                .filter(Producto::isActivo)
                .map(Producto::getStock)
                .orElse(0);
    }

    // ==================================================================
    //  Movimientos
    // ==================================================================

    /** Descuenta las unidades del pedido. Devuelve los productos que quedaron bajo minimo. */
    @Transactional
    public List<Producto> descontar(Pedido pedido) {
        List<Producto> quedaronBajos = new ArrayList<>();
        for (ItemPedido item : pedido.getItems()) {
            Producto producto = item.getProducto();
            if (producto == null) continue;

            int nuevo = producto.getStock() - item.getCantidad();
            if (nuevo < 0) {
                log.warn("Inventario negativo evitado en {} (habia {}, se pidieron {})",
                        producto.getSku(), producto.getStock(), item.getCantidad());
                nuevo = 0;
            }
            producto.setStock(nuevo);
            productoRepo.save(producto);

            if (producto.isStockBajo()) quedaronBajos.add(producto);
        }
        return quedaronBajos;
    }

    /** Devuelve las unidades al inventario cuando el pedido se cae. */
    @Transactional
    public void reponer(Pedido pedido) {
        for (ItemPedido item : pedido.getItems()) {
            Producto producto = item.getProducto();
            if (producto == null) continue;
            producto.setStock(producto.getStock() + item.getCantidad());
            productoRepo.save(producto);
        }
        log.info("Inventario repuesto por el pedido {}", pedido.getNumero());
    }

    // ==================================================================
    //  Alertas
    // ==================================================================

    /** Productos publicados a los que les queda poco. */
    public List<Producto> bajoMinimo() {
        return productoRepo.findAllByOrderByNombreAsc().stream()
                .filter(Producto::isStockBajo)
                .toList();
    }

    /** Productos publicados que ya se agotaron. */
    public List<Producto> agotados() {
        return productoRepo.findAllByOrderByNombreAsc().stream()
                .filter(p -> p.isActivo() && p.getStock() <= 0)
                .toList();
    }

    public long cuantosBajoMinimo() { return bajoMinimo().size(); }
    public long cuantosAgotados() { return agotados().size(); }
}
