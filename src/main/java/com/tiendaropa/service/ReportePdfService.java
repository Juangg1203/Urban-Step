package com.tiendaropa.service;

// OpenPDF usa la clase Color de AWT, no una propia. Se importa explicita y
// no con java.awt.*, porque java.awt.Rectangle chocaria con com.lowagie.text.Rectangle.
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.Locale;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.tiendaropa.dto.ConteoDTO;
import com.tiendaropa.dto.RecomendacionDTO;
import com.tiendaropa.dto.ReporteDTO;
import com.tiendaropa.dto.SugerenciaDTO;
import com.tiendaropa.model.ItemPedido;
import com.tiendaropa.model.Pedido;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Genera los PDF descargables con OpenPDF.
 *
 * Se construye el documento a mano en vez de convertir la pagina HTML: asi el
 * PDF no arrastra la maquetacion de pantalla y queda con el formato de un
 * documento que se archiva o se imprime.
 */
@Service
public class ReportePdfService {

    /*
     * El PDF se imprime en papel, asi que NO hereda el fondo oscuro del sitio:
     * seria ilegible y gastaria toner. Conserva la tinta oscura sobre blanco y
     * toma los acentos de UrbanStep en versiones algo mas oscuras, porque el
     * lima de pantalla sobre papel blanco no se lee.
     */
    private static final Color TINTA  = new Color(0x0B, 0x0E, 0x14);   // asfalto
    private static final Color HILO   = new Color(0x7A, 0xA8, 0x0F);   // lima apagado
    private static final Color CARMIN = new Color(0xD1, 0x1F, 0x5E);   // magenta
    private static final Color VERDE  = new Color(0x10, 0x9E, 0x92);   // cian
    private static final Color LINEA  = new Color(0xDC, 0xE1, 0xE8);
    private static final Color GRIS   = new Color(0x6B, 0x76, 0x88);

    private static final Font H1      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, TINTA);
    private static final Font H2      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, TINTA);
    private static final Font ROTULO  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, GRIS);
    private static final Font CUERPO  = FontFactory.getFont(FontFactory.HELVETICA, 9, TINTA);
    private static final Font CHICO   = FontFactory.getFont(FontFactory.HELVETICA, 8, GRIS);
    private static final Font DATO    = FontFactory.getFont(FontFactory.COURIER, 9, TINTA);
    private static final Font CIFRA   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, HILO);

    @Value("${app.empresa.nombre:UrbanStep}")
    private String empresa;

    private final NumberFormat pesos = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO"));

    // ==================================================================
    //  Reporte mensual
    // ==================================================================
    public byte[] reporteMensual(ReporteDTO r) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.LETTER, 40, 40, 46, 46);
        try {
            PdfWriter.getInstance(doc, salida);
            doc.open();

            encabezado(doc, "Reporte mensual de atencion al cliente",
                    capitalizar(r.getNombreMes()) + " de " + r.getAnio());
            doc.add(parrafo("Generado el " + r.getFechaGeneracion() + " por " + r.getGeneradoPor()
                    + " | Periodo " + r.getPeriodo(), CHICO, 10));

            if (r.isSinDatos()) {
                doc.add(parrafo("No hubo atenciones registradas en este periodo.", CUERPO, 20));
                doc.close();
                return salida.toByteArray();
            }

            // ---- 1. personas atendidas ----
            doc.add(titulo("1. Cuantas personas se atendieron"));
            PdfPTable cifras = new PdfPTable(4);
            cifras.setWidthPercentage(100);
            cifras.setSpacingBefore(6);
            tablero(cifras, "Personas atendidas", String.valueOf(r.getPersonasAtendidas()));
            tablero(cifras, "Clientes registrados", String.valueOf(r.getClientesRegistrados()));
            tablero(cifras, "Visitantes sin cuenta", String.valueOf(r.getVisitantesAnonimos()));
            tablero(cifras, "Total de atenciones", String.valueOf(r.getTotalAtenciones()));
            doc.add(cifras);

            doc.add(parrafo(variacionTexto(r), CHICO, 8));

            doc.add(subtitulo("Por canal de atencion"));
            doc.add(barras(r.getPorCanal(), HILO));
            doc.add(subtitulo("Por tema consultado"));
            doc.add(barras(r.getPorTema(), TINTA));

            // ---- 2. calificacion ----
            doc.add(titulo("2. Como calificaron la atencion"));
            PdfPTable calif = new PdfPTable(3);
            calif.setWidthPercentage(100);
            calif.setSpacingBefore(6);
            tablero(calif, "Calificacion promedio", r.getPromedioCalificacion() + " / 5");
            tablero(calif, "Clientes satisfechos", r.getSatisfaccionPct() + "%");
            tablero(calif, "Casos resueltos", r.getResueltasPct() + "%");
            doc.add(calif);

            doc.add(parrafo("Mes anterior: " + r.getPromedioMesAnterior() + " / 5. "
                    + r.getEscaladas() + " casos escalados a un agente sobre "
                    + r.getAtencionesCalificadas() + " respuestas recibidas.", CHICO, 8));

            doc.add(subtitulo("Distribucion de estrellas"));
            doc.add(barras(r.getDistribucionEstrellas(), VERDE));

            // ---- 3. recomendaciones ----
            doc.add(titulo("3. Que recomendaron los clientes"));
            if (r.getRecomendaciones().isEmpty()) {
                doc.add(parrafo("Nadie dejo comentarios escritos este mes.", CUERPO, 6));
            } else {
                PdfPTable tabla = new PdfPTable(new float[]{2.2f, 0.9f, 6f});
                tabla.setWidthPercentage(100);
                tabla.setSpacingBefore(6);
                encabezadoTabla(tabla, "Cliente", "Estrellas", "Comentario");
                for (RecomendacionDTO rec : r.getRecomendaciones()) {
                    tabla.addCell(celda(rec.getCliente() + "\n" + rec.getFecha(), CHICO));
                    tabla.addCell(celda(rec.getEstrellas() + " / 5",
                            rec.isNegativa() ? fuente(CARMIN) : fuente(VERDE)));
                    tabla.addCell(celda(rec.getTexto(), CUERPO));
                }
                doc.add(tabla);
            }

            // ---- 4. sugerencias ----
            doc.add(titulo("4. Sugerencias para la administracion"));
            for (SugerenciaDTO s : r.getSugerencias()) {
                PdfPTable caja = new PdfPTable(1);
                caja.setWidthPercentage(100);
                caja.setSpacingBefore(6);

                Color borde = "ALTA".equals(s.getPrioridad()) ? CARMIN
                            : ("MEDIA".equals(s.getPrioridad()) ? HILO : VERDE);
                PdfPCell c = new PdfPCell();
                c.setBorder(Rectangle.LEFT);
                c.setBorderColor(borde);
                c.setBorderWidthLeft(3f);
                c.setPaddingLeft(8);
                c.setPaddingBottom(6);
                c.addElement(new Paragraph("[" + s.getPrioridad() + "] " + s.getTitulo(), H2));
                c.addElement(new Paragraph(s.getDetalle(), CUERPO));
                c.addElement(new Paragraph("Indicador: " + s.getIndicador(), CHICO));
                caja.addCell(c);
                doc.add(caja);
            }

            pie(doc);
            doc.close();
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible generar el PDF del reporte", e);
        }
        return salida.toByteArray();
    }

    // ==================================================================
    //  Orden de pedido
    // ==================================================================
    public byte[] ordenPedido(Pedido p) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.LETTER, 40, 40, 46, 46);
        try {
            PdfWriter.getInstance(doc, salida);
            doc.open();

            encabezado(doc, "Orden de pedido", p.getNumero());
            doc.add(parrafo("Fecha: " + p.getFechaTexto()
                    + "  |  Estado: " + p.getEstado().getEtiqueta(), CHICO, 10));

            PdfPTable datos = new PdfPTable(new float[]{1f, 2f});
            datos.setWidthPercentage(100);
            datos.setSpacingBefore(8);
            fila(datos, "Cliente", p.getCliente().getNombreCompleto());
            fila(datos, "Ciudad", p.getCliente().getCiudad() + ", " + p.getCliente().getDepartamento());
            fila(datos, "Direccion de entrega", textoSeguro(p.getDireccionEntrega()));
            fila(datos, "Medio de pago", textoSeguro(p.getMedioPago()));
            if (p.getReferenciaPago() != null) fila(datos, "Referencia de pago", p.getReferenciaPago());
            if (p.getNumeroGuia() != null) fila(datos, "Guia", p.getNumeroGuia());
            doc.add(datos);

            doc.add(titulo("Productos"));
            PdfPTable tabla = new PdfPTable(new float[]{1.4f, 4f, 1f, 1f, 1.6f, 1.6f});
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(6);
            encabezadoTabla(tabla, "SKU", "Producto", "Talla", "Cant.", "Unitario", "Subtotal");
            for (ItemPedido it : p.getItems()) {
                tabla.addCell(celda(it.getProducto() == null ? "-" : it.getProducto().getSku(), DATO));
                tabla.addCell(celda(it.getNombreProducto(), CUERPO));
                tabla.addCell(celda(it.getTalla() == null ? "-" : it.getTalla(), DATO));
                tabla.addCell(celda(String.valueOf(it.getCantidad()), DATO));
                tabla.addCell(celdaDerecha("$" + pesos.format(it.getPrecioUnitario()), DATO));
                tabla.addCell(celdaDerecha("$" + pesos.format(it.getSubtotal()), DATO));
            }
            doc.add(tabla);

            PdfPTable totales = new PdfPTable(new float[]{6f, 2f});
            totales.setWidthPercentage(100);
            totales.setSpacingBefore(8);
            totalFila(totales, "Subtotal", "$" + pesos.format(p.getSubtotal()), false);
            totalFila(totales, "Envio", p.isEnvioGratis() ? "Gratis"
                    : "$" + pesos.format(p.getCostoEnvio()), false);
            totalFila(totales, "TOTAL", "$" + pesos.format(p.getTotal()), true);
            doc.add(totales);

            doc.add(titulo("Trazabilidad"));
            PdfPTable traza = new PdfPTable(new float[]{1f, 2f});
            traza.setWidthPercentage(100);
            traza.setSpacingBefore(6);
            if (p.getAprobadoPor() != null) {
                fila(traza, "Aprobado por", p.getAprobadoPor().getNombreUsuario()
                        + " el " + p.getFechaAprobacionTexto());
            }
            if (p.getMotivoDecision() != null) fila(traza, "Nota", p.getMotivoDecision());
            if (p.getPagoVerificadoPor() != null) {
                fila(traza, "Pago verificado por", p.getPagoVerificadoPor().getNombreUsuario());
            }
            if (p.getDespachadoPor() != null) {
                fila(traza, "Despachado por", p.getDespachadoPor().getNombreUsuario()
                        + " el " + p.getFechaDespachoTexto());
            }
            doc.add(traza);

            pie(doc);
            doc.close();
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible generar el PDF del pedido", e);
        }
        return salida.toByteArray();
    }

    // ==================================================================
    //  Piezas reutilizables
    // ==================================================================
    private void encabezado(Document doc, String rotulo, String tituloTexto) throws DocumentException {
        Paragraph marca = new Paragraph(empresa.toUpperCase(), ROTULO);
        marca.setSpacingAfter(2);
        doc.add(marca);
        doc.add(new Paragraph(tituloTexto, H1));
        Paragraph sub = new Paragraph(rotulo, CHICO);
        sub.setSpacingAfter(4);
        doc.add(sub);
        doc.add(regla(HILO, 2f));
    }

    private void pie(Document doc) throws DocumentException {
        doc.add(regla(LINEA, 0.7f));
        Paragraph nota = new Paragraph(
                "Documento generado automaticamente por el sistema de " + empresa
              + ". Los datos personales que aparecen aqui estan sujetos a la politica de "
              + "tratamiento y su consulta quedo registrada en la auditoria.", CHICO);
        nota.setSpacingBefore(4);
        doc.add(nota);
    }

    private PdfPTable regla(Color color, float grosor) {
        PdfPTable linea = new PdfPTable(1);
        linea.setWidthPercentage(100);
        linea.setSpacingBefore(4);
        linea.setSpacingAfter(6);
        PdfPCell c = new PdfPCell(new Phrase(" ", CHICO));
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(color);
        c.setBorderWidthBottom(grosor);
        c.setFixedHeight(2f);
        linea.addCell(c);
        return linea;
    }

    private Paragraph titulo(String texto) {
        Paragraph p = new Paragraph(texto, H2);
        p.setSpacingBefore(14);
        p.setSpacingAfter(2);
        return p;
    }

    private Paragraph subtitulo(String texto) {
        Paragraph p = new Paragraph(texto.toUpperCase(), ROTULO);
        p.setSpacingBefore(10);
        p.setSpacingAfter(3);
        return p;
    }

    private Paragraph parrafo(String texto, Font fuente, float espacioAntes) {
        Paragraph p = new Paragraph(texto, fuente);
        p.setSpacingBefore(espacioAntes);
        return p;
    }

    /** Barras horizontales dibujadas con celdas de color: no requiere imagenes. */
    private PdfPTable barras(java.util.List<ConteoDTO> datos, Color color) {
        PdfPTable t = new PdfPTable(new float[]{3.2f, 5.5f, 1.6f});
        t.setWidthPercentage(100);
        for (ConteoDTO d : datos) {
            t.addCell(celda(d.getEtiqueta(), CUERPO));

            // La barra es una tabla anidada: parte pintada + parte vacia.
            PdfPTable barra = new PdfPTable(new float[]{
                    Math.max(0.5f, (float) d.getPorcentaje()),
                    Math.max(0.5f, (float) (100 - d.getPorcentaje()))});
            barra.setWidthPercentage(100);
            PdfPCell llena = new PdfPCell(new Phrase(" ", CHICO));
            llena.setBackgroundColor(color);
            llena.setBorder(Rectangle.NO_BORDER);
            llena.setFixedHeight(8f);
            PdfPCell vacia = new PdfPCell(new Phrase(" ", CHICO));
            vacia.setBackgroundColor(LINEA);
            vacia.setBorder(Rectangle.NO_BORDER);
            vacia.setFixedHeight(8f);
            barra.addCell(llena);
            barra.addCell(vacia);

            PdfPCell contenedor = new PdfPCell(barra);
            contenedor.setBorder(Rectangle.NO_BORDER);
            contenedor.setPadding(3);
            t.addCell(contenedor);

            t.addCell(celdaDerecha(d.getCantidad() + " (" + d.getPorcentaje() + "%)", DATO));
        }
        return t;
    }

    private void tablero(PdfPTable tabla, String rotulo, String valor) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.BOX);
        c.setBorderColor(LINEA);
        c.setPadding(8);
        c.addElement(new Paragraph(rotulo.toUpperCase(), ROTULO));
        c.addElement(new Paragraph(valor, CIFRA));
        tabla.addCell(c);
    }

    private void encabezadoTabla(PdfPTable tabla, String... titulos) {
        for (String t : titulos) {
            PdfPCell c = new PdfPCell(new Phrase(t.toUpperCase(), ROTULO));
            c.setBackgroundColor(new Color(0xF4, 0xF6, 0xF9));
            c.setBorder(Rectangle.BOTTOM);
            c.setBorderColor(LINEA);
            c.setPadding(5);
            tabla.addCell(c);
        }
    }

    private PdfPCell celda(String texto, Font fuente) {
        PdfPCell c = new PdfPCell(new Phrase(texto == null ? "" : texto, fuente));
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(LINEA);
        c.setPadding(5);
        return c;
    }

    private PdfPCell celdaDerecha(String texto, Font fuente) {
        PdfPCell c = celda(texto, fuente);
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return c;
    }

    private void fila(PdfPTable tabla, String rotulo, String valor) {
        tabla.addCell(celda(rotulo, ROTULO));
        tabla.addCell(celda(valor, CUERPO));
    }

    private void totalFila(PdfPTable tabla, String rotulo, String valor, boolean fuerte) {
        Font f = fuerte ? H2 : CUERPO;
        PdfPCell r = new PdfPCell(new Phrase(rotulo, f));
        r.setHorizontalAlignment(Element.ALIGN_RIGHT);
        r.setBorder(fuerte ? Rectangle.TOP : Rectangle.NO_BORDER);
        r.setBorderColor(LINEA);
        r.setPadding(4);
        PdfPCell v = new PdfPCell(new Phrase(valor, fuerte ? CIFRA : DATO));
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        v.setBorder(fuerte ? Rectangle.TOP : Rectangle.NO_BORDER);
        v.setBorderColor(LINEA);
        v.setPadding(4);
        tabla.addCell(r);
        tabla.addCell(v);
    }

    private Font fuente(Color color) {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, color);
    }

    private String variacionTexto(ReporteDTO r) {
        double v = r.getVariacionPersonas();
        if (v == 0) return "Sin variacion frente al mes anterior ("
                + r.getPersonasMesAnterior() + " personas).";
        return (v > 0 ? "Crecimiento de " : "Caida de ") + Math.abs(v)
                + "% frente al mes anterior (" + r.getPersonasMesAnterior() + " personas).";
    }

    private String textoSeguro(String texto) {
        return texto == null || texto.isBlank() ? "-" : texto;
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return "";
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }
}
