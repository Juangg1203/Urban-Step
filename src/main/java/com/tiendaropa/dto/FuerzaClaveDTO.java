package com.tiendaropa.dto;

import java.util.ArrayList;
import java.util.List;

/** Resultado de evaluar la seguridad de una clave. */
public class FuerzaClaveDTO {

    private int puntaje;            // 0 a 100
    private String nivel;           // MUY_DEBIL, DEBIL, ACEPTABLE, BUENA, EXCELENTE
    private String etiqueta;        // texto que se le muestra al usuario
    private boolean aceptable;      // si alcanza para crear la cuenta
    private List<String> avisos = new ArrayList<>();

    public int getPuntaje() { return puntaje; }
    public void setPuntaje(int puntaje) { this.puntaje = puntaje; }
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }
    public boolean isAceptable() { return aceptable; }
    public void setAceptable(boolean aceptable) { this.aceptable = aceptable; }
    public List<String> getAvisos() { return avisos; }
    public void setAvisos(List<String> avisos) { this.avisos = avisos; }
}
