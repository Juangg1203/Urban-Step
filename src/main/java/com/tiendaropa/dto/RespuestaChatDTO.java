package com.tiendaropa.dto;

import java.util.ArrayList;
import java.util.List;

/** Lo que el chatbot devuelve al navegador en cada turno. */
public class RespuestaChatDTO {

    private String sesion;
    private String respuesta;
    private String intencion;
    private boolean escalar;
    private boolean pedirCalificacion;
    private boolean generadaPorIa;
    private List<String> sugerencias = new ArrayList<>();

    public String getSesion() { return sesion; }
    public void setSesion(String sesion) { this.sesion = sesion; }
    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }
    public String getIntencion() { return intencion; }
    public void setIntencion(String intencion) { this.intencion = intencion; }
    public boolean isEscalar() { return escalar; }
    public void setEscalar(boolean escalar) { this.escalar = escalar; }
    public boolean isPedirCalificacion() { return pedirCalificacion; }
    public void setPedirCalificacion(boolean pedirCalificacion) { this.pedirCalificacion = pedirCalificacion; }
    public boolean isGeneradaPorIa() { return generadaPorIa; }
    public void setGeneradaPorIa(boolean generadaPorIa) { this.generadaPorIa = generadaPorIa; }
    public List<String> getSugerencias() { return sugerencias; }
    public void setSugerencias(List<String> sugerencias) { this.sugerencias = sugerencias; }
}
