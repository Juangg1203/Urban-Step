package com.tiendaropa.service;

import java.util.List;

import com.tiendaropa.dto.ConteoDTO;
import org.springframework.stereotype.Service;

/**
 * Graficos del reporte, dibujados como SVG en el servidor.
 *
 * No se usa una libreria de JavaScript a proposito: el SVG viaja dentro del
 * HTML, se ve igual sin conexion y sale bien al imprimir o al guardar la
 * pagina como PDF desde el navegador. Una libreria como Chart.js dibuja sobre
 * un canvas que muchas veces sale en blanco en la impresion.
 */
@Service
public class GraficoService {

    // Paleta UrbanStep. Los graficos se dibujan sobre fondo oscuro, asi que
    // el texto va en tono hueso y las lineas guia en un gris azulado tenue.
    private static final String TINTA  = "#EDF1F7";   // texto principal
    private static final String HILO   = "#C6FF3D";   // lima, acento
    private static final String VERDE  = "#29E5D4";   // cian
    private static final String CARMIN = "#FF3D7F";   // magenta
    private static final String INDIGO = "#8B5CFF";   // violeta
    private static final String AMBAR  = "#FFB43D";
    private static final String LINEA  = "#263042";   // guias
    private static final String GRIS   = "#8C99AE";   // texto secundario

    private static final String[] PALETA = {HILO, INDIGO, VERDE, CARMIN, AMBAR, "#5D6880"};

    // ==================================================================
    /** Anillo de satisfaccion: un solo valor sobre 100. */
    public String anillo(double porcentaje, String etiqueta) {
        int radio = 54;
        double circunferencia = 2 * Math.PI * radio;
        double pintado = circunferencia * Math.max(0, Math.min(100, porcentaje)) / 100.0;
        String color = porcentaje >= 80 ? VERDE : (porcentaje >= 60 ? HILO : CARMIN);

        StringBuilder svg = new StringBuilder();
        svg.append("<svg viewBox=\"0 0 140 140\" class=\"grafico-anillo\" role=\"img\" ")
           .append("aria-label=\"").append(escapar(etiqueta)).append(": ")
           .append(redondear(porcentaje)).append(" por ciento\">");
        svg.append("<circle cx=\"70\" cy=\"70\" r=\"").append(radio)
           .append("\" fill=\"none\" stroke=\"").append(LINEA).append("\" stroke-width=\"14\"/>");
        svg.append("<circle cx=\"70\" cy=\"70\" r=\"").append(radio)
           .append("\" fill=\"none\" stroke=\"").append(color)
           .append("\" stroke-width=\"14\" stroke-linecap=\"butt\"")
           .append(" stroke-dasharray=\"").append(fmt(pintado)).append(" ")
           .append(fmt(circunferencia - pintado)).append("\"")
           .append(" transform=\"rotate(-90 70 70)\"/>");
        svg.append("<text x=\"70\" y=\"68\" text-anchor=\"middle\" ")
           .append("font-family=\"JetBrains Mono, monospace\" font-size=\"26\" fill=\"").append(TINTA)
           .append("\">").append(redondear(porcentaje)).append("%</text>");
        svg.append("<text x=\"70\" y=\"88\" text-anchor=\"middle\" ")
           .append("font-family=\"Space Grotesk, sans-serif\" font-size=\"10\" fill=\"").append(GRIS)
           .append("\">").append(escapar(etiqueta)).append("</text>");
        svg.append("</svg>");
        return svg.toString();
    }

    // ==================================================================
    /** Barras verticales, una por cada conteo. */
    public String barras(List<ConteoDTO> datos, boolean colorPorPosicion) {
        if (datos == null || datos.isEmpty()) return vacio("Sin datos en el periodo");

        int ancho = 520, alto = 220;
        int margenIzq = 34, margenAbajo = 46, margenArriba = 14;
        int utilAncho = ancho - margenIzq - 12;
        int utilAlto = alto - margenAbajo - margenArriba;

        long maximo = datos.stream().mapToLong(ConteoDTO::getCantidad).max().orElse(1);
        if (maximo == 0) maximo = 1;
        double paso = (double) utilAncho / datos.size();
        double anchoBarra = Math.min(58, paso * 0.62);

        StringBuilder svg = new StringBuilder();
        svg.append("<svg viewBox=\"0 0 ").append(ancho).append(" ").append(alto)
           .append("\" class=\"grafico\" role=\"img\" aria-label=\"Grafico de barras\">");

        // lineas guia
        for (int i = 0; i <= 4; i++) {
            double y = margenArriba + utilAlto * i / 4.0;
            long valor = Math.round(maximo * (4 - i) / 4.0);
            svg.append("<line x1=\"").append(margenIzq).append("\" y1=\"").append(fmt(y))
               .append("\" x2=\"").append(ancho - 12).append("\" y2=\"").append(fmt(y))
               .append("\" stroke=\"").append(LINEA).append("\" stroke-width=\"1\"/>");
            svg.append("<text x=\"").append(margenIzq - 6).append("\" y=\"").append(fmt(y + 3))
               .append("\" text-anchor=\"end\" font-family=\"JetBrains Mono, monospace\" ")
               .append("font-size=\"9\" fill=\"").append(GRIS).append("\">").append(valor)
               .append("</text>");
        }

        int i = 0;
        for (ConteoDTO d : datos) {
            double altura = utilAlto * (d.getCantidad() / (double) maximo);
            double x = margenIzq + paso * i + (paso - anchoBarra) / 2;
            double y = margenArriba + utilAlto - altura;
            String color = colorPorPosicion ? PALETA[i % PALETA.length] : HILO;

            svg.append("<rect x=\"").append(fmt(x)).append("\" y=\"").append(fmt(y))
               .append("\" width=\"").append(fmt(anchoBarra)).append("\" height=\"")
               .append(fmt(Math.max(1, altura))).append("\" fill=\"").append(color).append("\"/>");
            svg.append("<text x=\"").append(fmt(x + anchoBarra / 2)).append("\" y=\"")
               .append(fmt(y - 4)).append("\" text-anchor=\"middle\" ")
               .append("font-family=\"JetBrains Mono, monospace\" font-size=\"10\" fill=\"")
               .append(TINTA).append("\">").append(d.getCantidad()).append("</text>");
            svg.append("<text x=\"").append(fmt(x + anchoBarra / 2)).append("\" y=\"")
               .append(alto - 26).append("\" text-anchor=\"middle\" ")
               .append("font-family=\"Space Grotesk, sans-serif\" font-size=\"9\" fill=\"").append(GRIS)
               .append("\">").append(escapar(recortar(d.getEtiqueta()))).append("</text>");
            svg.append("<text x=\"").append(fmt(x + anchoBarra / 2)).append("\" y=\"")
               .append(alto - 14).append("\" text-anchor=\"middle\" ")
               .append("font-family=\"JetBrains Mono, monospace\" font-size=\"8\" fill=\"").append(GRIS)
               .append("\">").append(redondear(d.getPorcentaje())).append("%</text>");
            i++;
        }
        svg.append("</svg>");
        return svg.toString();
    }

    // ==================================================================
    /** Torta por canal o por tema. */
    public String torta(List<ConteoDTO> datos) {
        if (datos == null || datos.isEmpty()) return vacio("Sin datos en el periodo");

        long total = datos.stream().mapToLong(ConteoDTO::getCantidad).sum();
        if (total == 0) return vacio("Sin datos en el periodo");

        int cx = 110, cy = 110, radio = 92;
        double angulo = -Math.PI / 2;   // arranca arriba

        StringBuilder svg = new StringBuilder();
        svg.append("<svg viewBox=\"0 0 400 220\" class=\"grafico\" role=\"img\" ")
           .append("aria-label=\"Grafico de torta\">");

        int i = 0;
        for (ConteoDTO d : datos) {
            double porcion = 2 * Math.PI * d.getCantidad() / total;
            double fin = angulo + porcion;
            String color = PALETA[i % PALETA.length];

            if (datos.size() == 1) {
                svg.append("<circle cx=\"").append(cx).append("\" cy=\"").append(cy)
                   .append("\" r=\"").append(radio).append("\" fill=\"").append(color).append("\"/>");
            } else {
                double x1 = cx + radio * Math.cos(angulo), y1 = cy + radio * Math.sin(angulo);
                double x2 = cx + radio * Math.cos(fin),    y2 = cy + radio * Math.sin(fin);
                int arcoGrande = porcion > Math.PI ? 1 : 0;
                svg.append("<path d=\"M ").append(cx).append(" ").append(cy)
                   .append(" L ").append(fmt(x1)).append(" ").append(fmt(y1))
                   .append(" A ").append(radio).append(" ").append(radio)
                   .append(" 0 ").append(arcoGrande).append(" 1 ")
                   .append(fmt(x2)).append(" ").append(fmt(y2)).append(" Z\" fill=\"")
                   .append(color).append("\" stroke=\"#FCFBF8\" stroke-width=\"2\"/>");
            }

            // leyenda a la derecha
            int ly = 26 + i * 22;
            svg.append("<rect x=\"224\" y=\"").append(ly - 9)
               .append("\" width=\"11\" height=\"11\" fill=\"").append(color).append("\"/>");
            svg.append("<text x=\"242\" y=\"").append(ly)
               .append("\" font-family=\"Space Grotesk, sans-serif\" font-size=\"10\" fill=\"")
               .append(TINTA).append("\">").append(escapar(recortar(d.getEtiqueta())))
               .append("</text>");
            svg.append("<text x=\"242\" y=\"").append(ly + 11)
               .append("\" font-family=\"JetBrains Mono, monospace\" font-size=\"8\" fill=\"")
               .append(GRIS).append("\">").append(d.getCantidad()).append(" - ")
               .append(redondear(d.getPorcentaje())).append("%</text>");

            angulo = fin;
            i++;
        }
        svg.append("</svg>");
        return svg.toString();
    }

    // ==================================================================
    /** Linea de evolucion mensual (por ejemplo, personas atendidas por mes). */
    public String linea(List<ConteoDTO> serie, String unidad) {
        if (serie == null || serie.size() < 2) return vacio("Se necesitan al menos dos periodos");

        int ancho = 520, alto = 200;
        int margenIzq = 38, margenAbajo = 34, margenArriba = 16;
        int utilAncho = ancho - margenIzq - 14;
        int utilAlto = alto - margenAbajo - margenArriba;

        long maximo = serie.stream().mapToLong(ConteoDTO::getCantidad).max().orElse(1);
        if (maximo == 0) maximo = 1;
        double paso = (double) utilAncho / (serie.size() - 1);

        StringBuilder puntos = new StringBuilder();
        StringBuilder marcas = new StringBuilder();
        StringBuilder svg = new StringBuilder();

        svg.append("<svg viewBox=\"0 0 ").append(ancho).append(" ").append(alto)
           .append("\" class=\"grafico\" role=\"img\" aria-label=\"Evolucion por periodo\">");

        for (int i = 0; i <= 3; i++) {
            double y = margenArriba + utilAlto * i / 3.0;
            long valor = Math.round(maximo * (3 - i) / 3.0);
            svg.append("<line x1=\"").append(margenIzq).append("\" y1=\"").append(fmt(y))
               .append("\" x2=\"").append(ancho - 14).append("\" y2=\"").append(fmt(y))
               .append("\" stroke=\"").append(LINEA).append("\" stroke-width=\"1\"/>");
            svg.append("<text x=\"").append(margenIzq - 6).append("\" y=\"").append(fmt(y + 3))
               .append("\" text-anchor=\"end\" font-family=\"JetBrains Mono, monospace\" ")
               .append("font-size=\"9\" fill=\"").append(GRIS).append("\">").append(valor)
               .append("</text>");
        }

        for (int i = 0; i < serie.size(); i++) {
            ConteoDTO d = serie.get(i);
            double x = margenIzq + paso * i;
            double y = margenArriba + utilAlto - utilAlto * (d.getCantidad() / (double) maximo);
            puntos.append(fmt(x)).append(",").append(fmt(y)).append(" ");

            marcas.append("<circle cx=\"").append(fmt(x)).append("\" cy=\"").append(fmt(y))
                  .append("\" r=\"4\" fill=\"").append(HILO).append("\"/>");
            marcas.append("<text x=\"").append(fmt(x)).append("\" y=\"").append(fmt(y - 9))
                  .append("\" text-anchor=\"middle\" font-family=\"JetBrains Mono, monospace\" ")
                  .append("font-size=\"9\" fill=\"").append(TINTA).append("\">")
                  .append(d.getCantidad()).append("</text>");
            marcas.append("<text x=\"").append(fmt(x)).append("\" y=\"").append(alto - 14)
                  .append("\" text-anchor=\"middle\" font-family=\"Space Grotesk, sans-serif\" ")
                  .append("font-size=\"9\" fill=\"").append(GRIS).append("\">")
                  .append(escapar(recortar(d.getEtiqueta()))).append("</text>");
        }

        svg.append("<polyline points=\"").append(puntos.toString().trim())
           .append("\" fill=\"none\" stroke=\"").append(HILO).append("\" stroke-width=\"2.5\" ")
           .append("stroke-linejoin=\"round\"/>");
        svg.append(marcas);
        svg.append("<text x=\"").append(margenIzq).append("\" y=\"11\" ")
           .append("font-family=\"Space Grotesk, sans-serif\" font-size=\"9\" fill=\"").append(GRIS)
           .append("\">").append(escapar(unidad)).append("</text>");
        svg.append("</svg>");
        return svg.toString();
    }

    // ------------------------------------------------------------------
    private String vacio(String mensaje) {
        return "<svg viewBox=\"0 0 520 120\" class=\"grafico\"><text x=\"260\" y=\"64\" "
             + "text-anchor=\"middle\" font-family=\"Space Grotesk, sans-serif\" font-size=\"12\" "
             + "fill=\"" + GRIS + "\">" + escapar(mensaje) + "</text></svg>";
    }

    private String fmt(double valor) {
        return String.format(java.util.Locale.US, "%.1f", valor);
    }

    private long redondear(double valor) {
        return Math.round(valor);
    }

    private String recortar(String texto) {
        if (texto == null) return "";
        return texto.length() > 18 ? texto.substring(0, 17) + "." : texto;
    }

    /** El SVG se inserta sin escapar en la JSP, asi que aqui se limpia. */
    private String escapar(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;").replace("<", "&lt;")
                    .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
