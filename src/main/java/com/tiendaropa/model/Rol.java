package com.tiendaropa.model;

/**
 * Roles del sistema. El acceso a cada nivel de dato depende de este rol.
 *
 * El JEFE absorbe la funcion de oficial de proteccion de datos: es quien ve
 * los datos privados completos, audita los accesos y aprueba las compras.
 * El EMPLEADO se especializa con SubtipoEmpleado (vendedor o bodeguero),
 * pero ambos comparten el mismo nivel de acceso a datos personales.
 */
public enum Rol {
    CLIENTE("Cliente"),
    EMPLEADO("Empleado"),
    JEFE("Jefe"),
    ADMIN("Administrador");

    private final String etiqueta;
    Rol(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
}
