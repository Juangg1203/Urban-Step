package com.tiendaropa.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jakarta.persistence.*;

/**
 * Aviso interno dirigido a un rol. Se usa para que al jefe le llegue la
 * solicitud de aprobacion cuando un cliente continua la compra, y para
 * avisar a bodega cuando hay algo que despachar.
 */
@Entity
@Table(name = "notificacion")
public class Notificacion {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Rol al que va dirigida (JEFE, EMPLEADO, ADMIN). */
    @Enumerated(EnumType.STRING)
    @Column(name = "rol_destino", nullable = false, length = 20)
    private Rol rolDestino;

    /** Si aplica, restringe a un subtipo: por ejemplo solo bodegueros. */
    @Enumerated(EnumType.STRING)
    @Column(name = "subtipo_destino", length = 20)
    private SubtipoEmpleado subtipoDestino;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, length = 400)
    private String mensaje;

    @Column(length = 200)
    private String enlace;

    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @Column(nullable = false)
    private boolean leida = false;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public Notificacion() { }

    public Notificacion(Rol rolDestino, String titulo, String mensaje, String enlace) {
        this.rolDestino = rolDestino;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.enlace = enlace;
    }

    public String getFechaTexto() { return fecha == null ? "" : fecha.format(FORMATO); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Rol getRolDestino() { return rolDestino; }
    public void setRolDestino(Rol rolDestino) { this.rolDestino = rolDestino; }
    public SubtipoEmpleado getSubtipoDestino() { return subtipoDestino; }
    public void setSubtipoDestino(SubtipoEmpleado s) { this.subtipoDestino = s; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getEnlace() { return enlace; }
    public void setEnlace(String enlace) { this.enlace = enlace; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
