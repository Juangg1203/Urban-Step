package com.tiendaropa.dto;

import java.util.ArrayList;
import java.util.List;
import com.tiendaropa.model.Cliente;
import com.tiendaropa.model.CompraCliente;

/**
 * Lo que un usuario concreto puede ver de un cliente. La ficha se arma
 * despues de aplicar la politica de acceso, asi que la vista JSP nunca
 * recibe un dato que el rol no tenga permitido leer.
 */
public class VistaClienteDTO {

    private Cliente cliente;                 // nivel publico
    private Acceso accesoSemiprivado = Acceso.DENEGADO;
    private Acceso accesoPrivado = Acceso.DENEGADO;
    private Acceso accesoSensible = Acceso.DENEGADO;

    private String tipoDocumento;
    private String numeroDocumento;
    private String direccion;
    private String telefono;
    private String correoPersonal;
    private String fechaNacimiento;

    private List<CompraCliente> compras = new ArrayList<>();

    // Metadatos de los datos sensibles: se muestran sin revelar el contenido
    private boolean sensiblesRegistrados;
    private boolean sensiblesAutorizados;
    private String fechaAutorizacionSensibles;

    // Contenido sensible: solo se llena cuando quien mira es el titular
    private String medidasCorporales;
    private String alergiasMateriales;
    private String condicionMovilidad;
    private String restriccionVestimenta;

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Acceso getAccesoSemiprivado() { return accesoSemiprivado; }
    public void setAccesoSemiprivado(Acceso accesoSemiprivado) { this.accesoSemiprivado = accesoSemiprivado; }
    public Acceso getAccesoPrivado() { return accesoPrivado; }
    public void setAccesoPrivado(Acceso accesoPrivado) { this.accesoPrivado = accesoPrivado; }
    public Acceso getAccesoSensible() { return accesoSensible; }
    public void setAccesoSensible(Acceso accesoSensible) { this.accesoSensible = accesoSensible; }
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
    public List<CompraCliente> getCompras() { return compras; }
    public void setCompras(List<CompraCliente> compras) { this.compras = compras; }
    public boolean isSensiblesRegistrados() { return sensiblesRegistrados; }
    public void setSensiblesRegistrados(boolean sensiblesRegistrados) { this.sensiblesRegistrados = sensiblesRegistrados; }
    public boolean isSensiblesAutorizados() { return sensiblesAutorizados; }
    public void setSensiblesAutorizados(boolean sensiblesAutorizados) { this.sensiblesAutorizados = sensiblesAutorizados; }
    public String getFechaAutorizacionSensibles() { return fechaAutorizacionSensibles; }
    public void setFechaAutorizacionSensibles(String f) { this.fechaAutorizacionSensibles = f; }
    public String getMedidasCorporales() { return medidasCorporales; }
    public void setMedidasCorporales(String medidasCorporales) { this.medidasCorporales = medidasCorporales; }
    public String getAlergiasMateriales() { return alergiasMateriales; }
    public void setAlergiasMateriales(String alergiasMateriales) { this.alergiasMateriales = alergiasMateriales; }
    public String getCondicionMovilidad() { return condicionMovilidad; }
    public void setCondicionMovilidad(String condicionMovilidad) { this.condicionMovilidad = condicionMovilidad; }
    public String getRestriccionVestimenta() { return restriccionVestimenta; }
    public void setRestriccionVestimenta(String restriccionVestimenta) { this.restriccionVestimenta = restriccionVestimenta; }
}
