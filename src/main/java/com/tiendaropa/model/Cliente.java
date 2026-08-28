package com.tiendaropa.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

/**
 * NIVEL PUBLICO. Estos campos los puede consultar cualquier rol interno
 * (queda registrado en la auditoria). Los demas niveles viven en tablas
 * aparte para poder aplicarles reglas de acceso distintas.
 */
@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 80)
    private String nombres;

    @Column(nullable = false, length = 80)
    private String apellidos;

    @Column(length = 60)
    private String ciudad;

    @Column(length = 60)
    private String departamento;

    @Column(length = 80)
    private String ocupacion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "acepta_tratamiento", nullable = false)
    private boolean aceptaTratamiento = false;

    @Column(name = "autoriza_sensibles", nullable = false)
    private boolean autorizaSensibles = false;

    @Column(name = "autoriza_marketing", nullable = false)
    private boolean autorizaMarketing = false;

    @Column(name = "version_politica", length = 20)
    private String versionPolitica;

    @Column(name = "fecha_autorizacion")
    private LocalDateTime fechaAutorizacion;

    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private DatoPrivadoCliente datosPrivados;

    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private DatoSensibleCliente datosSensibles;

    public Cliente() { }

    public String getNombreCompleto() { return nombres + " " + apellidos; }

    public String getFechaRegistroTexto() {
        return fechaRegistro == null ? "-"
                : fechaRegistro.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public String getOcupacion() { return ocupacion; }
    public void setOcupacion(String ocupacion) { this.ocupacion = ocupacion; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public boolean isAceptaTratamiento() { return aceptaTratamiento; }
    public void setAceptaTratamiento(boolean aceptaTratamiento) { this.aceptaTratamiento = aceptaTratamiento; }
    public boolean isAutorizaSensibles() { return autorizaSensibles; }
    public void setAutorizaSensibles(boolean autorizaSensibles) { this.autorizaSensibles = autorizaSensibles; }
    public boolean isAutorizaMarketing() { return autorizaMarketing; }
    public void setAutorizaMarketing(boolean autorizaMarketing) { this.autorizaMarketing = autorizaMarketing; }
    public String getVersionPolitica() { return versionPolitica; }
    public void setVersionPolitica(String versionPolitica) { this.versionPolitica = versionPolitica; }
    public LocalDateTime getFechaAutorizacion() { return fechaAutorizacion; }
    public void setFechaAutorizacion(LocalDateTime fechaAutorizacion) { this.fechaAutorizacion = fechaAutorizacion; }
    public DatoPrivadoCliente getDatosPrivados() { return datosPrivados; }
    public void setDatosPrivados(DatoPrivadoCliente datosPrivados) { this.datosPrivados = datosPrivados; }
    public DatoSensibleCliente getDatosSensibles() { return datosSensibles; }
    public void setDatosSensibles(DatoSensibleCliente datosSensibles) { this.datosSensibles = datosSensibles; }
}
