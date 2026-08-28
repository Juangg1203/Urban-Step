package com.tiendaropa.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import com.tiendaropa.dto.ReporteDTO;
import com.tiendaropa.service.GraficoService;
import com.tiendaropa.service.ReportePdfService;
import com.tiendaropa.service.ReporteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Reporte mensual para la administracion.
 * Solo lo consultan los roles ADMIN y JEFE (ver SecurityConfig).
 */
@Controller
@RequestMapping("/panel/reportes")
public class ReporteController {

    private final ReporteService reporteService;
    private final ReportePdfService pdfService;
    private final GraficoService graficos;

    public ReporteController(ReporteService reporteService, ReportePdfService pdfService,
                             GraficoService graficos) {
        this.reporteService = reporteService;
        this.pdfService = pdfService;
        this.graficos = graficos;
    }

    @GetMapping
    public String reporte(@RequestParam(required = false) Integer anio,
                          @RequestParam(required = false) Integer mes,
                          Authentication autenticacion, Model modelo) {
        LocalDate hoy = LocalDate.now();
        int a = (anio == null) ? hoy.getYear() : anio;
        int m = (mes == null) ? hoy.getMonthValue() : mes;

        ReporteDTO reporte = reporteService.generar(a, m, autenticacion.getName());
        modelo.addAttribute("reporte", reporte);

        // Los graficos se arman en el servidor y viajan como SVG dentro del HTML:
        // asi salen bien al imprimir y no dependen de JavaScript.
        modelo.addAttribute("gAnillo", graficos.anillo(reporte.getSatisfaccionPct(), "satisfaccion"));
        modelo.addAttribute("gResueltas", graficos.anillo(reporte.getResueltasPct(), "resueltos"));
        modelo.addAttribute("gEstrellas", graficos.barras(reporte.getDistribucionEstrellas(), false));
        modelo.addAttribute("gCanal", graficos.torta(reporte.getPorCanal()));
        modelo.addAttribute("gTema", graficos.barras(reporte.getPorTema(), true));
        modelo.addAttribute("gEvolucion",
                graficos.linea(reporteService.evolucionPersonas(a, m, 6), "personas atendidas por mes"));
        modelo.addAttribute("anios", new int[]{hoy.getYear(), hoy.getYear() - 1, hoy.getYear() - 2});
        modelo.addAttribute("titulo", "Reporte mensual");
        return "panel/reporte";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam int anio, @RequestParam int mes,
                          Authentication autenticacion, RedirectAttributes flash) {
        ReporteDTO reporte = reporteService.generar(anio, mes, autenticacion.getName());
        reporteService.guardar(reporte, autenticacion.getName());
        flash.addFlashAttribute("mensaje", "Reporte de " + reporte.getNombreMes() + " guardado en el historico.");
        return "redirect:/panel/reportes?anio=" + anio + "&mes=" + mes;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam int anio, @RequestParam int mes,
                                      Authentication autenticacion) {
        ReporteDTO reporte = reporteService.generar(anio, mes, autenticacion.getName());
        byte[] contenido = pdfService.reporteMensual(reporte);
        String archivo = "reporte-atencion-" + anio + "-" + String.format("%02d", mes) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenido);
    }

    @GetMapping("/csv")
    public ResponseEntity<byte[]> csv(@RequestParam int anio, @RequestParam int mes,
                                      Authentication autenticacion) {
        ReporteDTO reporte = reporteService.generar(anio, mes, autenticacion.getName());
        byte[] contenido = reporteService.csv(reporte).getBytes(StandardCharsets.UTF_8);
        String archivo = "reporte-atencion-" + anio + "-" + String.format("%02d", mes) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(contenido);
    }
}
