package com.tiendaropa.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

/** Cada contacto de atencion al cliente. Es la fuente del reporte mensual. */
@Entity
@Table(name = "atencion")
public class Atencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agente_id")
    private Usuario agente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Canal canal;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Tema tema = Tema.OTRO;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio = LocalDateTime.now();

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAtencion estado = EstadoAtencion.ABIERTA;

    @Column(nullable = false)
    private boolean resuelta = false;

    private Integer calificacion;

    @Column(length = 600)
    private String recomendacion;

    public Atencion() { }

    public Atencion(Cliente cliente, Canal canal, Tema tema) {
        this.cliente = cliente;
        this.canal = canal;
        this.tema = tema;
    }

    /** Nombre visible del cliente o "Visitante" si fue una sesion anonima. */
    public String getNombreCliente() {
        return cliente != null ? cliente.getNombreCompleto() : "Visitante sin registro";
    }

    public String getFechaInicioTexto() { return formatear(fechaInicio); }
    public String getFechaCierreTexto() { return formatear(fechaCierre); }

    private static String formatear(java.time.LocalDateTime f) {
        return f == null ? "-"
                : f.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Usuario getAgente() { return agente; }
    public void setAgente(Usuario agente) { this.agente = agente; }
    public Canal getCanal() { return canal; }
    public void setCanal(Canal canal) { this.canal = canal; }
    public Tema getTema() { return tema; }
    public void setTema(Tema tema) { this.tema = tema; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
    public EstadoAtencion getEstado() { return estado; }
    public void setEstado(EstadoAtencion estado) { this.estado = estado; }
    public boolean isResuelta() { return resuelta; }
    public void setResuelta(boolean resuelta) { this.resuelta = resuelta; }
    public Integer getCalificacion() { return calificacion; }
    public void setCalificacion(Integer calificacion) { this.calificacion = calificacion; }
    public String getRecomendacion() { return recomendacion; }
    public void setRecomendacion(String recomendacion) { this.recomendacion = recomendacion; }
}
