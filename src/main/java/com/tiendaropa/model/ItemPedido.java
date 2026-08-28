package com.tiendaropa.model;

import java.math.BigDecimal;
import jakarta.persistence.*;

/**
 * Linea de un pedido. Guarda el precio al momento de agregar, no el actual:
 * si manana sube el precio, la cotizacion que el cliente vio sigue valiendo.
 */
@Entity
@Table(name = "item_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    /** Copia del nombre por si el producto se retira del catalogo. */
    @Column(name = "nombre_producto", nullable = false, length = 120)
    private String nombreProducto;

    @Column(length = 10)
    private String talla;

    @Column(nullable = false)
    private int cantidad = 1;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    public ItemPedido() { }

    public ItemPedido(Producto producto, String talla, int cantidad) {
        this.producto = producto;
        this.nombreProducto = producto.getNombre();
        this.precioUnitario = producto.getPrecio();
        this.talla = talla;
        this.cantidad = cantidad;
    }

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal p) { this.precioUnitario = p; }
}
