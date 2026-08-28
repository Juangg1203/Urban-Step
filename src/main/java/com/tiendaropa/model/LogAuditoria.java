package com.tiendaropa.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

/** Rastro de quien consulto o modifico datos personales y de que nivel. */
@Entity
@Table(name = "log_auditoria")
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(length = 60)
    private String usuario;

    @Column(length = 20)
    private String rol;

    @Column(nullable = false, length = 40)
    private String accion;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_dato", length = 15)
    private NivelDato nivelDato;

    @Column(length = 40)
    private String entidad;

    @Column(name = "registro_id")
    private Long registroId;

    @Column(length = 400)
    private String detalle;

    @Column(length = 45)
    private String ip;

    public LogAuditoria() { }

    public String getFechaTexto() {
        return fecha == null ? "-"
                : fecha.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public NivelDato getNivelDato() { return nivelDato; }
    public void setNivelDato(NivelDato nivelDato) { this.nivelDato = nivelDato; }
    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }
    public Long getRegistroId() { return registroId; }
    public void setRegistroId(Long registroId) { this.registroId = registroId; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
}
