package com.tiendaropa.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;

/** NIVEL SEMIPRIVADO: historial y comportamiento de pago del cliente. */
@Entity
@Table(name = "compra_cliente")
public class CompraCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false)
    private int cantidad = 1;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "medio_pago", length = 30)
    private String medioPago;

    @Column(name = "estado_pago", length = 20)
    private String estadoPago;

    public CompraCliente() { }

    /** JSTL no formatea LocalDateTime, asi que la vista usa este texto. */
    public String getFechaTexto() {
        return fecha == null ? "-"
                : fecha.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String medioPago) { this.medioPago = medioPago; }
    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }
}
