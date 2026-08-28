package com.tiendaropa.dto;

import jakarta.validation.constraints.*;

/** Formulario de registro. Separa lo publico de lo privado desde la entrada. */
public class RegistroForm {

    @NotBlank(message = "Escribe un nombre de usuario")
    @Size(min = 4, max = 60)
    private String nombreUsuario;

    @NotBlank(message = "Escribe un correo")
    @Email(message = "El correo no tiene un formato valido")
    private String correo;

    @NotBlank(message = "Escribe una clave")
    @Size(min = 8, message = "La clave debe tener al menos 8 caracteres")
    private String clave;

    @NotBlank(message = "Escribe tus nombres")
    private String nombres;

    @NotBlank(message = "Escribe tus apellidos")
    private String apellidos;

    private String ciudad;
    private String departamento;
    private String ocupacion;

    private String tipoDocumento;
    private String numeroDocumento;
    private String direccion;
    private String telefono;
    private String fechaNacimiento;

    @AssertTrue(message = "Debes autorizar el tratamiento de datos para crear la cuenta")
    private boolean aceptaTratamiento;

    private boolean autorizaMarketing;

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
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
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public boolean isAceptaTratamiento() { return aceptaTratamiento; }
    public void setAceptaTratamiento(boolean aceptaTratamiento) { this.aceptaTratamiento = aceptaTratamiento; }
    public boolean isAutorizaMarketing() { return autorizaMarketing; }
    public void setAutorizaMarketing(boolean autorizaMarketing) { this.autorizaMarketing = autorizaMarketing; }
}
