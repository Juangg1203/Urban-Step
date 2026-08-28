package com.tiendaropa.dto;

import java.util.ArrayList;
import java.util.List;

/** Reporte mensual de atencion al cliente. */
public class ReporteDTO {

    private int anio;
    private int mes;
    private String nombreMes;
    private String periodo;

    // 1. Cantidad de personas atendidas
    private long clientesRegistrados;
    private long visitantesAnonimos;
    private long personasAtendidas;
    private long totalAtenciones;

    // 2. Calificacion de la atencion
    private long atencionesCalificadas;
    private double promedioCalificacion;
    private double satisfaccionPct;
    private List<ConteoDTO> distribucionEstrellas = new ArrayList<>();

    // Distribuciones de apoyo
    private List<ConteoDTO> porCanal = new ArrayList<>();
    private List<ConteoDTO> porTema = new ArrayList<>();
    private long escaladas;
    private double resueltasPct;

    // 3. Recomendaciones de los clientes
    private List<RecomendacionDTO> recomendaciones = new ArrayList<>();

    // 4. Sugerencias para la administracion
    private List<SugerenciaDTO> sugerencias = new ArrayList<>();

    // Comparativo con el mes anterior
    private long personasMesAnterior;
    private double promedioMesAnterior;
    private double variacionPersonas;
    private double variacionCalificacion;

    private String generadoPor;
    private String fechaGeneracion;

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }
    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }
    public String getNombreMes() { return nombreMes; }
    public void setNombreMes(String nombreMes) { this.nombreMes = nombreMes; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
    public long getClientesRegistrados() { return clientesRegistrados; }
    public void setClientesRegistrados(long v) { this.clientesRegistrados = v; }
    public long getVisitantesAnonimos() { return visitantesAnonimos; }
    public void setVisitantesAnonimos(long v) { this.visitantesAnonimos = v; }
    public long getPersonasAtendidas() { return personasAtendidas; }
    public void setPersonasAtendidas(long v) { this.personasAtendidas = v; }
    public long getTotalAtenciones() { return totalAtenciones; }
    public void setTotalAtenciones(long v) { this.totalAtenciones = v; }
    public long getAtencionesCalificadas() { return atencionesCalificadas; }
    public void setAtencionesCalificadas(long v) { this.atencionesCalificadas = v; }
    public double getPromedioCalificacion() { return promedioCalificacion; }
    public void setPromedioCalificacion(double v) { this.promedioCalificacion = v; }
    public double getSatisfaccionPct() { return satisfaccionPct; }
    public void setSatisfaccionPct(double v) { this.satisfaccionPct = v; }
    public List<ConteoDTO> getDistribucionEstrellas() { return distribucionEstrellas; }
    public void setDistribucionEstrellas(List<ConteoDTO> v) { this.distribucionEstrellas = v; }
    public List<ConteoDTO> getPorCanal() { return porCanal; }
    public void setPorCanal(List<ConteoDTO> v) { this.porCanal = v; }
    public List<ConteoDTO> getPorTema() { return porTema; }
    public void setPorTema(List<ConteoDTO> v) { this.porTema = v; }
    public long getEscaladas() { return escaladas; }
    public void setEscaladas(long v) { this.escaladas = v; }
    public double getResueltasPct() { return resueltasPct; }
    public void setResueltasPct(double v) { this.resueltasPct = v; }
    public List<RecomendacionDTO> getRecomendaciones() { return recomendaciones; }
    public void setRecomendaciones(List<RecomendacionDTO> v) { this.recomendaciones = v; }
    public List<SugerenciaDTO> getSugerencias() { return sugerencias; }
    public void setSugerencias(List<SugerenciaDTO> v) { this.sugerencias = v; }
    public long getPersonasMesAnterior() { return personasMesAnterior; }
    public void setPersonasMesAnterior(long v) { this.personasMesAnterior = v; }
    public double getPromedioMesAnterior() { return promedioMesAnterior; }
    public void setPromedioMesAnterior(double v) { this.promedioMesAnterior = v; }
    public double getVariacionPersonas() { return variacionPersonas; }
    public void setVariacionPersonas(double v) { this.variacionPersonas = v; }
    public double getVariacionCalificacion() { return variacionCalificacion; }
    public void setVariacionCalificacion(double v) { this.variacionCalificacion = v; }
    public String getGeneradoPor() { return generadoPor; }
    public void setGeneradoPor(String v) { this.generadoPor = v; }
    public String getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(String v) { this.fechaGeneracion = v; }

    public boolean isSinDatos() { return totalAtenciones == 0; }
}
