package com.tiendaropa.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jakarta.persistence.*;

/**
 * Resena que el cliente deja sobre un producto, despues de confirmar que
 * recibio el pedido. No se pueden calificar productos que nunca se compraron
 * ni pedidos que no llegaron: la resena nace atada a un ItemPedido concreto.
 */
@Entity
@Table(name = "resena")
public class Resena {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @Column(nullable = false)
    private int calificacion;   // 1 a 5

    @Column(length = 600)
    private String comentario;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public Resena() { }

    public Resena(Producto producto, Cliente cliente, Pedido pedido, int calificacion, String comentario) {
        this.producto = producto;
        this.cliente = cliente;
        this.pedido = pedido;
        this.calificacion = calificacion;
        this.comentario = comentario;
    }

    public String getFechaTexto() { return fecha == null ? "" : fecha.format(FORMATO); }
    public String getNombreCliente() { return cliente == null ? "" : cliente.getNombres(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public int getCalificacion() { return calificacion; }
    public void setCalificacion(int calificacion) { this.calificacion = calificacion; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
