package com.tiendaropa.dto;

public class ConteoDTO {
    private String etiqueta;
    private long cantidad;
    private double porcentaje;

    public ConteoDTO(String etiqueta, long cantidad, double porcentaje) {
        this.etiqueta = etiqueta;
        this.cantidad = cantidad;
        this.porcentaje = porcentaje;
    }
    public String getEtiqueta() { return etiqueta; }
    public long getCantidad() { return cantidad; }
    public double getPorcentaje() { return porcentaje; }
}
