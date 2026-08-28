package com.tiendaropa.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 60)
    private String nombreUsuario;

    @Column(nullable = false, unique = true, length = 120)
    private String correo;

    @Column(nullable = false, length = 120)
    private String clave;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    /** Solo aplica cuando el rol es EMPLEADO: vendedor o bodeguero. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SubtipoEmpleado subtipo;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @OneToOne(mappedBy = "usuario")
    private Cliente cliente;

    public Usuario() { }

    public Usuario(String nombreUsuario, String correo, String clave, Rol rol) {
        this.nombreUsuario = nombreUsuario;
        this.correo = correo;
        this.clave = clave;
        this.rol = rol;
    }

    public Long getId() { return id; }
    public SubtipoEmpleado getSubtipo() { return subtipo; }
    public void setSubtipo(SubtipoEmpleado subtipo) { this.subtipo = subtipo; }

    /** Etiqueta lista para mostrar: "Empleado (Bodeguero)" o solo el rol. */
    public String getRolTexto() {
        if (rol == Rol.EMPLEADO && subtipo != null) {
            return rol.getEtiqueta() + " (" + subtipo.getEtiqueta() + ")";
        }
        return rol == null ? "" : rol.getEtiqueta();
    }

    public boolean isBodeguero() { return subtipo == SubtipoEmpleado.BODEGUERO; }
    public boolean isVendedor()  { return subtipo == SubtipoEmpleado.VENDEDOR; }

    public void setId(Long id) { this.id = id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
}
