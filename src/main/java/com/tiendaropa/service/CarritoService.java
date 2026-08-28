package com.tiendaropa.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.tiendaropa.dto.ItemCarrito;
import com.tiendaropa.model.Producto;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

/**
 * Carrito de compras. Vive en la sesion del navegador, no en la base de
 * datos: mientras el cliente no confirme nada, no hay razon para guardar
 * lo que esta mirando.
 *
 * Cuando decide seguir, el carrito se convierte en un Pedido (cotizacion o
 * solicitud de compra) y ahi si queda registrado.
 */
@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CarritoService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final BigDecimal UMBRAL_ENVIO_GRATIS = new BigDecimal("180000");
    private static final BigDecimal COSTO_ENVIO = new BigDecimal("12000");
    private static final int MAX_POR_LINEA = 20;

    private final List<ItemCarrito> items = new ArrayList<>();

    public List<ItemCarrito> getItems() { return items; }

    public boolean isVacio() { return items.isEmpty(); }

    public int getTotalUnidades() {
        return items.stream().mapToInt(ItemCarrito::getCantidad).sum();
    }

    public int getTotalLineas() { return items.size(); }

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(ItemCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getCostoEnvio() {
        BigDecimal sub = getSubtotal();
        if (sub.signum() == 0) return BigDecimal.ZERO;
        return sub.compareTo(UMBRAL_ENVIO_GRATIS) >= 0 ? BigDecimal.ZERO : COSTO_ENVIO;
    }

    public BigDecimal getTotal() { return getSubtotal().add(getCostoEnvio()); }

    public boolean isEnvioGratis() {
        return getSubtotal().signum() > 0 && getCostoEnvio().signum() == 0;
    }

    /** Cuanto falta para que el envio salga gratis. Cero si ya lo es. */
    public BigDecimal getFaltaParaEnvioGratis() {
        BigDecimal falta = UMBRAL_ENVIO_GRATIS.subtract(getSubtotal());
        return falta.signum() > 0 ? falta : BigDecimal.ZERO;
    }

    // ------------------------------------------------------------------
    /** Agrega o suma cantidad. Devuelve el mensaje para mostrarle al cliente. */
    public String agregar(Producto producto, String talla, int cantidad) {
        if (producto == null) return "El producto ya no esta disponible.";
        if (cantidad < 1) cantidad = 1;

        if (producto.getStock() <= 0) {
            return "\"" + producto.getNombre() + "\" esta agotado por ahora.";
        }

        for (ItemCarrito item : items) {
            if (item.mismaLinea(producto.getId(), talla)) {
                int nueva = Math.min(item.getCantidad() + cantidad,
                                     Math.min(MAX_POR_LINEA, producto.getStock()));
                if (nueva == item.getCantidad()) {
                    return "No hay mas existencias de \"" + producto.getNombre() + "\" en esa talla.";
                }
                item.setCantidad(nueva);
                return "Actualizamos la cantidad de \"" + producto.getNombre() + "\" en tu carrito.";
            }
        }

        ItemCarrito item = new ItemCarrito();
        item.setProductoId(producto.getId());
        item.setSku(producto.getSku());
        item.setNombre(producto.getNombre());
        item.setTalla(talla);
        item.setColor(producto.getColor());
        item.setPrecioUnitario(producto.getPrecio());
        item.setStockDisponible(producto.getStock());
        item.setCantidad(Math.min(cantidad, Math.min(MAX_POR_LINEA, producto.getStock())));
        items.add(item);
        return "\"" + producto.getNombre() + "\" se agrego a tu carrito.";
    }

    public void cambiarCantidad(Long productoId, String talla, int cantidad) {
        for (ItemCarrito item : items) {
            if (item.mismaLinea(productoId, talla)) {
                if (cantidad < 1) { items.remove(item); return; }
                int tope = item.getStockDisponible() > 0
                        ? Math.min(MAX_POR_LINEA, item.getStockDisponible()) : MAX_POR_LINEA;
                item.setCantidad(Math.min(cantidad, tope));
                return;
            }
        }
    }

    public void quitar(Long productoId, String talla) {
        items.removeIf(item -> item.mismaLinea(productoId, talla));
    }

    public void vaciar() { items.clear(); }
}
