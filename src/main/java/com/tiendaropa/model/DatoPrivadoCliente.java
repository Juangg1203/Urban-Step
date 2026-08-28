package com.tiendaropa.model;

import java.time.LocalDateTime;
import com.tiendaropa.util.ConvertidorCifrado;
import jakarta.persistence.*;

/** NIVEL PRIVADO. Todos los campos se guardan cifrados en MySQL. */
@Entity
@Table(name = "dato_privado_cliente")
public class DatoPrivadoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false, unique = true)
    private Cliente cliente;

    @Column(name = "tipo_documento", length = 10)
    private String tipoDocumento;

    @Convert(converter = ConvertidorCifrado.class)
    @Column(name = "numero_documento", length = 400)
    private String numeroDocumento;

    @Convert(converter = ConvertidorCifrado.class)
    @Column(length = 400)
    private String direccion;

    @Convert(converter = ConvertidorCifrado.class)
    @Column(length = 400)
    private String telefono;

    @Convert(converter = ConvertidorCifrado.class)
    @Column(name = "correo_personal", length = 400)
    private String correoPersonal;

    @Convert(converter = ConvertidorCifrado.class)
    @Column(name = "fecha_nacimiento", length = 400)
    private String fechaNacimiento;

    private LocalDateTime actualizado = LocalDateTime.now();

    public DatoPrivadoCliente() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreoPersonal() { return correoPersonal; }
    public void setCorreoPersonal(String correoPersonal) { this.correoPersonal = correoPersonal; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public LocalDateTime getActualizado() { return actualizado; }
    public void setActualizado(LocalDateTime actualizado) { this.actualizado = actualizado; }
}
