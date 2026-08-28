package com.tiendaropa.model;

/**
 * Clasificacion de la informacion (Ley 1581 de 2012 / Ley 1266 de 2008).
 */
public enum NivelDato {
    PUBLICO("Publico", "No requiere autorizacion previa"),
    SEMIPRIVADO("Semiprivado", "Interesa al titular y a un grupo determinado"),
    PRIVADO("Privado", "Solo interesa al titular"),
    SENSIBLE("Sensible", "Afecta la intimidad; exige autorizacion expresa");

    private final String etiqueta;
    private final String descripcion;

    NivelDato(String etiqueta, String descripcion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }
    public String getEtiqueta() { return etiqueta; }
    public String getDescripcion() { return descripcion; }
}
