package com.tiendaropa.model;

/**
 * Especializacion del rol EMPLEADO. Cambia que puede HACER, no que puede VER:
 * el acceso a datos personales es el mismo para los dos.
 */
public enum SubtipoEmpleado {
    VENDEDOR("Vendedor"),
    BODEGUERO("Bodeguero");

    private final String etiqueta;
    SubtipoEmpleado(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
}
