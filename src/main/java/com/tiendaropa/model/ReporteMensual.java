package com.tiendaropa.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

/** Copia guardada del reporte mensual, para poder consultarlo despues. */
@Entity
@Table(name = "reporte_mensual")
public class ReporteMensual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = false)
    private int mes;

    @Column(name = "personas_atendidas", nullable = false)
    private int personasAtendidas;

    @Column(name = "total_atenciones", nullable = false)
    private int totalAtenciones;

    @Column(name = "promedio_calificacion", nullable = false)
    private double promedioCalificacion;

    @Column(name = "satisfaccion_pct", nullable = false)
    private double satisfaccionPct;

    @Column(name = "atenciones_chatbot", nullable = false)
    private int atencionesChatbot;

    @Column(name = "atenciones_agente", nullable = false)
    private int atencionesAgente;

    @Column(nullable = false)
    private int escaladas;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String sugerencias;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    @Column(name = "generado_por", length = 60)
    private String generadoPor;

    public ReporteMensual() { }

    private static final String[] NOMBRES_MES = {"enero","febrero","marzo","abril","mayo","junio",
            "julio","agosto","septiembre","octubre","noviembre","diciembre"};

    public String getPeriodoTexto() {
        return (mes >= 1 && mes <= 12 ? NOMBRES_MES[mes - 1] : "?") + " de " + anio;
    }

    public String getFechaGeneracionTexto() {
        return fechaGeneracion == null ? "-"
                : fechaGeneracion.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }
    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }
    public int getPersonasAtendidas() { return personasAtendidas; }
    public void setPersonasAtendidas(int personasAtendidas) { this.personasAtendidas = personasAtendidas; }
    public int getTotalAtenciones() { return totalAtenciones; }
    public void setTotalAtenciones(int totalAtenciones) { this.totalAtenciones = totalAtenciones; }
    public double getPromedioCalificacion() { return promedioCalificacion; }
    public void setPromedioCalificacion(double promedioCalificacion) { this.promedioCalificacion = promedioCalificacion; }
    public double getSatisfaccionPct() { return satisfaccionPct; }
    public void setSatisfaccionPct(double satisfaccionPct) { this.satisfaccionPct = satisfaccionPct; }
    public int getAtencionesChatbot() { return atencionesChatbot; }
    public void setAtencionesChatbot(int atencionesChatbot) { this.atencionesChatbot = atencionesChatbot; }
    public int getAtencionesAgente() { return atencionesAgente; }
    public void setAtencionesAgente(int atencionesAgente) { this.atencionesAgente = atencionesAgente; }
    public int getEscaladas() { return escaladas; }
    public void setEscaladas(int escaladas) { this.escaladas = escaladas; }
    public String getSugerencias() { return sugerencias; }
    public void setSugerencias(String sugerencias) { this.sugerencias = sugerencias; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
    public String getGeneradoPor() { return generadoPor; }
    public void setGeneradoPor(String generadoPor) { this.generadoPor = generadoPor; }
}
