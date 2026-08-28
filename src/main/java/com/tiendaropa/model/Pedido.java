package com.tiendaropa.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

import com.tiendaropa.util.ConvertidorCifrado;

/**
 * Pedido o cotizacion. Nace como COTIZACION cuando el cliente guarda el
 * carrito, y pasa a PENDIENTE_PAGO cuando confirma la compra.
 *
 * La direccion de entrega es un dato PRIVADO, asi que se guarda cifrada igual
 * que el resto: que viaje dentro de un pedido no la vuelve menos sensible.
 */
@Entity
@Table(name = "pedido")
public class Pedido {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPedido estado = EstadoPedido.COTIZACION;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "costo_envio", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoEnvio = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "medio_pago", length = 30)
    private String medioPago;

    @Convert(converter = ConvertidorCifrado.class)
    @Column(name = "direccion_entrega", length = 400)
    private String direccionEntrega;

    @Column(length = 400)
    private String observaciones;

    // --- visto bueno final del jefe (despues del pago, no antes) ---
    @ManyToOne
    @JoinColumn(name = "aprobado_por_id")
    private Usuario aprobadoPor;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "motivo_decision", length = 400)
    private String motivoDecision;

    // --- pago y despacho ---
    @ManyToOne
    @JoinColumn(name = "pago_verificado_por_id")
    private Usuario pagoVerificadoPor;

    @Column(name = "referencia_pago", length = 60)
    private String referenciaPago;

    /**
     * Nombre del archivo con el comprobante que sube el cliente (transferencia,
     * consignacion...). Es opcional y sirve para agilizar la verificacion del
     * vendedor; se guarda igual que las imagenes de producto, fuera del war.
     */
    @Column(name = "comprobante_pago", length = 300)
    private String comprobantePago;

    // --- pasarela de pagos (Wompi) ---
    /** Referencia unica que se le envia a la pasarela. Nunca se reutiliza. */
    @Column(name = "referencia_pasarela", length = 60)
    private String referenciaPasarela;

    /** Id que devuelve la pasarela para esa transaccion. */
    @Column(name = "transaccion_pasarela", length = 60)
    private String transaccionPasarela;

    /** APPROVED, DECLINED, VOIDED, ERROR o PENDING, tal como lo reporta la pasarela. */
    @Column(name = "estado_pasarela", length = 20)
    private String estadoPasarela;

    /** CARD, NEQUI, PSE, BANCOLOMBIA_TRANSFER... */
    @Column(name = "metodo_pasarela", length = 30)
    private String metodoPasarela;

    @ManyToOne
    @JoinColumn(name = "despachado_por_id")
    private Usuario despachadoPor;

    @Column(name = "numero_guia", length = 60)
    private String numeroGuia;

    @Column(name = "fecha_despacho")
    private LocalDateTime fechaDespacho;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    /**
     * Foto que sube el CLIENTE al confirmar que recibio el pedido. Es la
     * prueba de entrega vista desde su lado, distinta de la guia que registra
     * bodega. Que la suba el cliente es lo que dispara el estado ENTREGADO y
     * habilita la resena.
     */
    @Column(name = "foto_entrega", length = 300)
    private String fotoEntrega;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> items = new ArrayList<>();

    public Pedido() { }

    public Pedido(Cliente cliente) {
        this.cliente = cliente;
    }

    // ------------------------------------------------------------------
    public void agregar(ItemPedido item) {
        item.setPedido(this);
        items.add(item);
    }

    /** Recalcula subtotal, envio y total. Envio gratis desde $180.000. */
    public void recalcular() {
        BigDecimal suma = BigDecimal.ZERO;
        for (ItemPedido item : items) {
            suma = suma.add(item.getSubtotal());
        }
        this.subtotal = suma;
        this.costoEnvio = suma.compareTo(new BigDecimal("180000")) >= 0 || suma.signum() == 0
                ? BigDecimal.ZERO : new BigDecimal("12000");
        this.total = this.subtotal.add(this.costoEnvio);
    }

    public int getTotalUnidades() {
        return items.stream().mapToInt(ItemPedido::getCantidad).sum();
    }

    public boolean isEnvioGratis() {
        return costoEnvio != null && costoEnvio.signum() == 0 && subtotal.signum() > 0;
    }

    // JSTL 3.0 no formatea LocalDateTime: se expone ya como texto.
    public String getFechaTexto() { return fecha == null ? "" : fecha.format(FORMATO); }
    public String getFechaAprobacionTexto() {
        return fechaAprobacion == null ? "" : fechaAprobacion.format(FORMATO);
    }
    public String getFechaDespachoTexto() {
        return fechaDespacho == null ? "" : fechaDespacho.format(FORMATO);
    }
    public String getFechaEntregaTexto() {
        return fechaEntrega == null ? "" : fechaEntrega.format(FORMATO);
    }

    // ------------------------------------------------------------------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
        this.fechaActualizacion = LocalDateTime.now();
    }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime f) { this.fechaActualizacion = f; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getCostoEnvio() { return costoEnvio; }
    public void setCostoEnvio(BigDecimal costoEnvio) { this.costoEnvio = costoEnvio; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String medioPago) { this.medioPago = medioPago; }
    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Usuario getAprobadoPor() { return aprobadoPor; }
    public void setAprobadoPor(Usuario aprobadoPor) { this.aprobadoPor = aprobadoPor; }
    public LocalDateTime getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(LocalDateTime f) { this.fechaAprobacion = f; }
    public String getMotivoDecision() { return motivoDecision; }
    public void setMotivoDecision(String motivoDecision) { this.motivoDecision = motivoDecision; }
    public Usuario getPagoVerificadoPor() { return pagoVerificadoPor; }
    public void setPagoVerificadoPor(Usuario u) { this.pagoVerificadoPor = u; }
    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }
    public String getComprobantePago() { return comprobantePago; }
    public void setComprobantePago(String comprobantePago) { this.comprobantePago = comprobantePago; }
    public boolean isTieneComprobante() { return comprobantePago != null && !comprobantePago.isBlank(); }
    public String getReferenciaPasarela() { return referenciaPasarela; }
    public void setReferenciaPasarela(String r) { this.referenciaPasarela = r; }
    public String getTransaccionPasarela() { return transaccionPasarela; }
    public void setTransaccionPasarela(String t) { this.transaccionPasarela = t; }
    public String getEstadoPasarela() { return estadoPasarela; }
    public void setEstadoPasarela(String e) { this.estadoPasarela = e; }
    public String getMetodoPasarela() { return metodoPasarela; }
    public void setMetodoPasarela(String m) { this.metodoPasarela = m; }

    public boolean isPagadoConPasarela() { return "APPROVED".equals(estadoPasarela); }

    /**
     * El total en centavos, que es como Wompi maneja los montos.
     * Trabajar con enteros evita los errores de redondeo de los decimales:
     * un centavo perdido en una pasarela es una transaccion rechazada.
     */
    public long getTotalEnCentavos() {
        return total == null ? 0L
                : total.movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).longValueExact();
    }
    public Usuario getDespachadoPor() { return despachadoPor; }
    public void setDespachadoPor(Usuario despachadoPor) { this.despachadoPor = despachadoPor; }
    public String getNumeroGuia() { return numeroGuia; }
    public void setNumeroGuia(String numeroGuia) { this.numeroGuia = numeroGuia; }
    public LocalDateTime getFechaDespacho() { return fechaDespacho; }
    public void setFechaDespacho(LocalDateTime f) { this.fechaDespacho = f; }
    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime f) { this.fechaEntrega = f; }
    public String getFotoEntrega() { return fotoEntrega; }
    public void setFotoEntrega(String fotoEntrega) { this.fotoEntrega = fotoEntrega; }
    public boolean isTieneFotoEntrega() { return fotoEntrega != null && !fotoEntrega.isBlank(); }
    public List<ItemPedido> getItems() { return items; }
    public void setItems(List<ItemPedido> items) { this.items = items; }
}
