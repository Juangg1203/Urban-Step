package com.tiendaropa.dto;

/** Resultado de evaluar la politica de acceso sobre un nivel de dato. */
public enum Acceso {
    COMPLETO("Visible"),
    ENMASCARADO("Parcial"),
    DENEGADO("Sin acceso");

    private final String etiqueta;
    Acceso(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }

    public boolean isCompleto() { return this == COMPLETO; }
    public boolean isEnmascarado() { return this == ENMASCARADO; }
    public boolean isDenegado() { return this == DENEGADO; }
}
