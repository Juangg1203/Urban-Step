package com.tiendaropa.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "conversacion")
public class Conversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String sesion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "atencion_id")
    private Atencion atencion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio = LocalDateTime.now();

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(nullable = false)
    private boolean escalada = false;

    @OneToMany(mappedBy = "conversacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha ASC")
    private List<MensajeChat> mensajes = new ArrayList<>();

    public Conversacion() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSesion() { return sesion; }
    public void setSesion(String sesion) { this.sesion = sesion; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Atencion getAtencion() { return atencion; }
    public void setAtencion(Atencion atencion) { this.atencion = atencion; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public boolean isEscalada() { return escalada; }
    public void setEscalada(boolean escalada) { this.escalada = escalada; }
    public List<MensajeChat> getMensajes() { return mensajes; }
    public void setMensajes(List<MensajeChat> mensajes) { this.mensajes = mensajes; }
}
