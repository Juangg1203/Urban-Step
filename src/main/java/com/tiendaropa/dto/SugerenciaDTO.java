package com.tiendaropa.dto;

/** Sugerencia dirigida a la administracion, derivada de los indicadores del mes. */
public class SugerenciaDTO {
    private String prioridad;   // ALTA | MEDIA | BAJA
    private String titulo;
    private String detalle;
    private String indicador;   // el dato que la origina

    public SugerenciaDTO(String prioridad, String titulo, String detalle, String indicador) {
        this.prioridad = prioridad;
        this.titulo = titulo;
        this.detalle = detalle;
        this.indicador = indicador;
    }
    public String getPrioridad() { return prioridad; }
    public String getTitulo() { return titulo; }
    public String getDetalle() { return detalle; }
    public String getIndicador() { return indicador; }
}
