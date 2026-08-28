package com.tiendaropa.dto;

/** Comentario textual dejado por un cliente al calificar la atencion. */
public class RecomendacionDTO {
    private String cliente;
    private String fecha;
    private int estrellas;
    private String tema;
    private String canal;
    private String texto;

    public RecomendacionDTO(String cliente, String fecha, int estrellas,
                            String tema, String canal, String texto) {
        this.cliente = cliente;
        this.fecha = fecha;
        this.estrellas = estrellas;
        this.tema = tema;
        this.canal = canal;
        this.texto = texto;
    }
    public String getCliente() { return cliente; }
    public String getFecha() { return fecha; }
    public int getEstrellas() { return estrellas; }
    public String getTema() { return tema; }
    public String getCanal() { return canal; }
    public String getTexto() { return texto; }
    public boolean isNegativa() { return estrellas <= 2; }
}
