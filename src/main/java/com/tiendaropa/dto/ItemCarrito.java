package com.tiendaropa.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Linea del carrito en sesion. No es una entidad: el carrito vive en la
 * sesion del navegador y solo se convierte en Pedido cuando el cliente
 * guarda la cotizacion o confirma la compra.
 */
public class ItemCarrito implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productoId;
    private String sku;
    private String nombre;
    private String talla;
    private String color;
    private int cantidad;
    private BigDecimal precioUnitario;
    private int stockDisponible;

    public ItemCarrito() { }

    public BigDecimal getSubtotal() {
        if (precioUnitario == null) return BigDecimal.ZERO;
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    /** Dos lineas son la misma si coinciden producto y talla. */
    public boolean mismaLinea(Long otroProducto, String otraTalla) {
        boolean mismoProducto = productoId != null && productoId.equals(otroProducto);
        boolean mismaTalla = (talla == null && otraTalla == null)
                || (talla != null && talla.equalsIgnoreCase(otraTalla == null ? "" : otraTalla));
        return mismoProducto && mismaTalla;
    }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal p) { this.precioUnitario = p; }
    public int getStockDisponible() { return stockDisponible; }
    public void setStockDisponible(int stockDisponible) { this.stockDisponible = stockDisponible; }
}
