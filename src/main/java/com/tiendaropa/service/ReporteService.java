package com.tiendaropa.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.tiendaropa.dto.*;
import com.tiendaropa.model.*;
import com.tiendaropa.repository.AtencionRepository;
import com.tiendaropa.repository.ClienteRepository;
import com.tiendaropa.repository.ReporteMensualRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Arma el reporte mensual de atencion al cliente:
 *   1. cuantas personas se atendieron,
 *   2. como calificaron la atencion,
 *   3. que recomendaron por escrito,
 *   4. que deberia hacer la administracion con esa informacion.
 */
@Service
public class ReporteService {

    private static final String[] MESES = {"enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};
    private static final DateTimeFormatter F_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter F_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AtencionRepository atencionRepo;
    private final ClienteRepository clienteRepo;
    private final ReporteMensualRepository reporteRepo;
    private final AuditoriaService auditoria;

    public ReporteService(AtencionRepository atencionRepo, ClienteRepository clienteRepo,
                          ReporteMensualRepository reporteRepo, AuditoriaService auditoria) {
        this.atencionRepo = atencionRepo;
        this.clienteRepo = clienteRepo;
        this.reporteRepo = reporteRepo;
        this.auditoria = auditoria;
    }

    // ==================================================================
    public ReporteDTO generar(int anio, int mes, String generadoPor) {
        LocalDateTime desde = LocalDateTime.of(anio, mes, 1, 0, 0);
        LocalDateTime hasta = desde.plusMonths(1).minusSeconds(1);
        List<Atencion> atenciones = atencionRepo.findByFechaInicioBetweenOrderByFechaInicioDesc(desde, hasta);

        ReporteDTO r = new ReporteDTO();
        r.setAnio(anio);
        r.setMes(mes);
        r.setNombreMes(MESES[mes - 1]);
        r.setPeriodo(F_FECHA.format(desde) + " al " + F_FECHA.format(hasta));
        r.setGeneradoPor(generadoPor);
        r.setFechaGeneracion(F_FECHA_HORA.format(LocalDateTime.now()));

        // ---------- 1. personas atendidas ----------
        long registrados = atencionRepo.contarClientesDistintos(desde, hasta);
        long anonimos = atencionRepo.contarAnonimos(desde, hasta);
        r.setClientesRegistrados(registrados);
        r.setVisitantesAnonimos(anonimos);
        r.setPersonasAtendidas(registrados + anonimos);
        r.setTotalAtenciones(atenciones.size());

        if (atenciones.isEmpty()) {
            r.setSugerencias(List.of(new SugerenciaDTO("MEDIA",
                    "No hay atenciones registradas en el periodo",
                    "Verifique que el chatbot este publicado en el sitio y que los agentes esten "
                  + "cerrando los casos en el sistema. Sin registros no hay indicadores que analizar.",
                    "0 atenciones en " + MESES[mes - 1] + " de " + anio)));
            return r;
        }

        // ---------- 2. calificacion ----------
        List<Atencion> calificadas = atenciones.stream()
                .filter(a -> a.getCalificacion() != null).toList();
        r.setAtencionesCalificadas(calificadas.size());

        double promedio = calificadas.stream().mapToInt(Atencion::getCalificacion).average().orElse(0);
        r.setPromedioCalificacion(redondear(promedio));

        long satisfechos = calificadas.stream().filter(a -> a.getCalificacion() >= 4).count();
        r.setSatisfaccionPct(porcentaje(satisfechos, calificadas.size()));

        List<ConteoDTO> distribucion = new ArrayList<>();
        for (int estrella = 5; estrella >= 1; estrella--) {
            final int valor = estrella;
            long cantidad = calificadas.stream().filter(a -> a.getCalificacion() == valor).count();
            distribucion.add(new ConteoDTO(estrella + " estrella" + (estrella == 1 ? "" : "s"),
                    cantidad, porcentaje(cantidad, calificadas.size())));
        }
        r.setDistribucionEstrellas(distribucion);

        // ---------- distribuciones de apoyo ----------
        Map<Canal, Long> porCanal = atenciones.stream()
                .collect(Collectors.groupingBy(Atencion::getCanal, Collectors.counting()));
        r.setPorCanal(porCanal.entrySet().stream()
                .sorted(Map.Entry.<Canal, Long>comparingByValue().reversed())
                .map(e -> new ConteoDTO(etiquetaCanal(e.getKey()), e.getValue(),
                        porcentaje(e.getValue(), atenciones.size())))
                .toList());

        Map<Tema, Long> porTema = atenciones.stream()
                .collect(Collectors.groupingBy(a -> a.getTema() == null ? Tema.OTRO : a.getTema(),
                        Collectors.counting()));
        List<Map.Entry<Tema, Long>> temasOrdenados = porTema.entrySet().stream()
                .sorted(Map.Entry.<Tema, Long>comparingByValue().reversed()).toList();
        r.setPorTema(temasOrdenados.stream()
                .map(e -> new ConteoDTO(e.getKey().getEtiqueta(), e.getValue(),
                        porcentaje(e.getValue(), atenciones.size())))
                .toList());

        long escaladas = atenciones.stream()
                .filter(a -> a.getEstado() == EstadoAtencion.ESCALADA).count();
        r.setEscaladas(escaladas);
        long resueltas = atenciones.stream().filter(Atencion::isResuelta).count();
        r.setResueltasPct(porcentaje(resueltas, atenciones.size()));

        // ---------- 3. recomendaciones escritas ----------
        r.setRecomendaciones(atenciones.stream()
                .filter(a -> a.getRecomendacion() != null && !a.getRecomendacion().isBlank())
                .sorted(Comparator.comparing(a -> a.getCalificacion() == null ? 5 : a.getCalificacion()))
                .map(a -> new RecomendacionDTO(
                        a.getNombreCliente(),
                        F_FECHA.format(a.getFechaInicio()),
                        a.getCalificacion() == null ? 0 : a.getCalificacion(),
                        a.getTema() == null ? "Otro" : a.getTema().getEtiqueta(),
                        etiquetaCanal(a.getCanal()),
                        a.getRecomendacion()))
                .toList());

        // ---------- comparativo con el mes anterior ----------
        LocalDateTime desdeAnterior = desde.minusMonths(1);
        LocalDateTime hastaAnterior = desde.minusSeconds(1);
        long personasAnterior = atencionRepo.contarClientesDistintos(desdeAnterior, hastaAnterior)
                + atencionRepo.contarAnonimos(desdeAnterior, hastaAnterior);
        double promedioAnterior = atencionRepo
                .findByFechaInicioBetweenOrderByFechaInicioDesc(desdeAnterior, hastaAnterior).stream()
                .filter(a -> a.getCalificacion() != null)
                .mapToInt(Atencion::getCalificacion).average().orElse(0);
        r.setPersonasMesAnterior(personasAnterior);
        r.setPromedioMesAnterior(redondear(promedioAnterior));
        r.setVariacionPersonas(personasAnterior == 0 ? 0
                : redondear((r.getPersonasAtendidas() - personasAnterior) * 100.0 / personasAnterior));
        r.setVariacionCalificacion(redondear(promedio - promedioAnterior));

        // ---------- 4. sugerencias ----------
        r.setSugerencias(construirSugerencias(r, atenciones, temasOrdenados));
        return r;
    }

    // ==================================================================
    //  Reglas que convierten los indicadores en acciones concretas
    // ==================================================================
    private List<SugerenciaDTO> construirSugerencias(ReporteDTO r, List<Atencion> atenciones,
                                                     List<Map.Entry<Tema, Long>> temas) {
        List<SugerenciaDTO> lista = new ArrayList<>();

        if (r.getAtencionesCalificadas() > 0 && r.getPromedioCalificacion() < 3.5) {
            lista.add(new SugerenciaDTO("ALTA",
                    "Intervenir la calidad de la atencion",
                    "El promedio quedo por debajo de 3.5. Revise las conversaciones con 1 y 2 estrellas, "
                  + "identifique el paso donde se pierde al cliente y programe una sesion de refuerzo con "
                  + "el equipo antes de que cierre el proximo mes.",
                    "Promedio de " + r.getPromedioCalificacion() + " sobre 5"));
        }

        long negativas = r.getRecomendaciones().stream().filter(RecomendacionDTO::isNegativa).count();
        if (negativas > 0) {
            lista.add(new SugerenciaDTO("ALTA",
                    "Contactar a los clientes insatisfechos",
                    "Hay " + negativas + " comentario(s) con 1 o 2 estrellas. Asigne a un agente para "
                  + "llamarlos esta semana: recuperar a un cliente molesto cuesta menos que conseguir uno nuevo.",
                    negativas + " calificaciones negativas con comentario"));
        }

        double pctEscaladas = porcentaje(r.getEscaladas(), r.getTotalAtenciones());
        if (pctEscaladas > 30) {
            String temaTop = temas.isEmpty() ? "las consultas frecuentes" : temas.get(0).getKey().getEtiqueta();
            lista.add(new SugerenciaDTO("ALTA",
                    "Ampliar la base de conocimiento del chatbot",
                    "El " + pctEscaladas + "% de las conversaciones terminaron en un agente humano. "
                  + "Agregue respuestas sobre " + temaTop.toLowerCase() + " al chatbot para descargar al "
                  + "equipo y responder mas rapido fuera de horario.",
                    pctEscaladas + "% de atenciones escaladas"));
        }

        if (!temas.isEmpty()) {
            Tema temaTop = temas.get(0).getKey();
            double pct = porcentaje(temas.get(0).getValue(), r.getTotalAtenciones());
            if (pct >= 25) {
                lista.add(new SugerenciaDTO("MEDIA",
                        "Atacar la causa de las consultas sobre " + temaTop.getEtiqueta().toLowerCase(),
                        accionPorTema(temaTop),
                        pct + "% de las consultas del mes"));
            }
        }

        if (r.getResueltasPct() < 70) {
            lista.add(new SugerenciaDTO("MEDIA",
                    "Subir la resolucion en el primer contacto",
                    "Solo el " + r.getResueltasPct() + "% de los casos quedo resuelto. Defina para cada "
                  + "tema quien decide y con que informacion, para que el agente no tenga que devolver "
                  + "el caso a otra area.",
                    r.getResueltasPct() + "% de casos resueltos"));
        }

        if (r.getPersonasMesAnterior() > 0 && r.getVariacionPersonas() < -15) {
            lista.add(new SugerenciaDTO("MEDIA",
                    "Revisar la caida en el volumen de atencion",
                    "Se atendieron " + Math.abs(r.getVariacionPersonas()) + "% menos personas que el mes "
                  + "anterior. Confirme que el chat este visible en todas las paginas y revise si hubo "
                  + "caida de trafico o de campanas.",
                    r.getPersonasAtendidas() + " personas frente a " + r.getPersonasMesAnterior()));
        }

        long fueraDeHorario = atenciones.stream()
                .filter(a -> a.getFechaInicio().getHour() < 8 || a.getFechaInicio().getHour() >= 18)
                .count();
        double pctFuera = porcentaje(fueraDeHorario, r.getTotalAtenciones());
        if (pctFuera >= 20) {
            lista.add(new SugerenciaDTO("MEDIA",
                    "Aprovechar la demanda fuera de horario",
                    "El " + pctFuera + "% de los contactos llegaron antes de las 8:00 o despues de las 18:00. "
                  + "El chatbot ya cubre esa franja; evalue dejar un agente de turno en la noche o habilitar "
                  + "respuesta diferida con compromiso de hora.",
                    pctFuera + "% de contactos fuera de horario de oficina"));
        }

        long sinMarketing = clienteRepo.count() - clienteRepo.countByAutorizaMarketingTrue();
        if (sinMarketing > 0) {
            lista.add(new SugerenciaDTO("BAJA",
                    "Depurar las bases antes de la proxima campana",
                    sinMarketing + " cliente(s) no autorizaron comunicaciones comerciales. Excluyalos de "
                  + "los envios: usar sus datos sin autorizacion expone a la empresa a una sancion y "
                  + "destruye la confianza que si tiene.",
                    sinMarketing + " clientes sin autorizacion de marketing"));
        }

        if (r.getSatisfaccionPct() >= 85 && r.getPromedioCalificacion() >= 4.3) {
            lista.add(new SugerenciaDTO("BAJA",
                    "Capitalizar la buena percepcion",
                    "La satisfaccion esta en " + r.getSatisfaccionPct() + "%. Pida a los clientes con 5 "
                  + "estrellas una resena publica y use sus comentarios en la ficha de producto.",
                    r.getSatisfaccionPct() + "% de clientes satisfechos"));
        }

        if (lista.isEmpty()) {
            lista.add(new SugerenciaDTO("BAJA",
                    "Sostener el estandar actual",
                    "Los indicadores del mes estan dentro de lo esperado. Mantenga el seguimiento y "
                  + "compare de nuevo al cierre del proximo periodo.",
                    "Sin alertas en " + r.getNombreMes()));
        }
        return lista;
    }

    private String accionPorTema(Tema tema) {
        switch (tema) {
            case TALLAS:
                return "Publique la tabla de medidas en la ficha de cada producto y agregue fotos con la "
                     + "talla que usa el modelo. Cada consulta de talla evitada es un cambio menos.";
            case ENVIOS:
                return "Muestre la fecha estimada de entrega antes de pagar y envie el numero de guia "
                     + "automaticamente. La mayoria de estas consultas son solo por falta de informacion.";
            case DEVOLUCIONES:
                return "Simplifique el formulario de cambios y publique el estado del caso en Mi cuenta, "
                     + "para que el cliente no tenga que escribir para saber en que va.";
            case PAGOS:
                return "Revise los rechazos de la pasarela y aclare en el checkout que medios acepta y "
                     + "en cuanto tiempo se confirma el pago.";
            case PRODUCTO:
                return "Complete las fichas con material, cuidados y existencias reales; la mayoria de "
                     + "estas preguntas se responden con mejor informacion en el catalogo.";
            case DATOS:
                return "Deje mas visible la politica de tratamiento y el boton para revocar autorizaciones. "
                     + "Que el cliente pregunte por sus datos es buena senal, pero deberia poder resolverlo solo.";
            case CUENTA:
                return "Revise el registro y la recuperacion de clave: si la gente escribe para poder "
                     + "entrar, el problema esta en el formulario, no en el cliente.";
            case PEDIDO:
                return "Publique el seguimiento del pedido en Mi cuenta y notifique cada cambio de estado. "
                     + "Preguntar donde va el pedido es el sintoma de que el sistema no lo esta contando.";
            case PROMOCIONES:
                return "Marque el precio anterior y el descuento en la ficha, y revise si la percepcion de "
                     + "precio alto se concentra en algunas categorias.";
            default:
                return "Clasifique mejor los casos al cerrarlos: con el tema bien marcado se puede atacar "
                     + "la causa en lugar de responder lo mismo cada mes.";
        }
    }

    // ==================================================================
    @Transactional
    public ReporteMensual guardar(ReporteDTO r, String usuario) {
        ReporteMensual entidad = new ReporteMensual();
        entidad.setAnio(r.getAnio());
        entidad.setMes(r.getMes());
        entidad.setPersonasAtendidas((int) r.getPersonasAtendidas());
        entidad.setTotalAtenciones((int) r.getTotalAtenciones());
        entidad.setPromedioCalificacion(r.getPromedioCalificacion());
        entidad.setSatisfaccionPct(r.getSatisfaccionPct());
        entidad.setAtencionesChatbot((int) cantidadCanal(r, "Chatbot"));
        entidad.setAtencionesAgente((int) cantidadCanal(r, "Agente"));
        entidad.setEscaladas((int) r.getEscaladas());
        entidad.setSugerencias(r.getSugerencias().stream()
                .map(s -> "[" + s.getPrioridad() + "] " + s.getTitulo() + ": " + s.getDetalle())
                .collect(Collectors.joining("\n")));
        entidad.setGeneradoPor(usuario);
        ReporteMensual guardado = reporteRepo.save(entidad);

        auditoria.registrar("REPORTE_MENSUAL", NivelDato.PUBLICO, "ReporteMensual", guardado.getId(),
                "Reporte de " + r.getNombreMes() + " " + r.getAnio() + " generado por " + usuario);
        return guardado;
    }

    /**
     * Serie de los ultimos meses para el grafico de evolucion. Se calcula al
     * vuelo desde las atenciones, no desde el historico guardado, para que la
     * grafica funcione aunque nadie haya pulsado "guardar reporte".
     */
    public List<ConteoDTO> evolucionPersonas(int anio, int mes, int cuantosMeses) {
        List<ConteoDTO> serie = new ArrayList<>();
        java.time.YearMonth actual = java.time.YearMonth.of(anio, mes);
        for (int i = cuantosMeses - 1; i >= 0; i--) {
            java.time.YearMonth periodo = actual.minusMonths(i);
            LocalDateTime desde = periodo.atDay(1).atStartOfDay();
            LocalDateTime hasta = periodo.atEndOfMonth().atTime(23, 59, 59);
            long personas = atencionRepo.contarClientesDistintos(desde, hasta)
                          + atencionRepo.contarAnonimos(desde, hasta);
            String etiqueta = MESES[periodo.getMonthValue() - 1].substring(0, 3)
                    + " " + String.valueOf(periodo.getYear()).substring(2);
            serie.add(new ConteoDTO(etiqueta, personas, 0));
        }
        return serie;
    }

    public List<ReporteMensual> historico() { return reporteRepo.findAllByOrderByAnioDescMesDesc(); }

    /** Exportacion en CSV para abrir en Excel. */
    public String csv(ReporteDTO r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Reporte mensual de atencion al cliente\n");
        sb.append("Periodo;").append(r.getNombreMes()).append(" ").append(r.getAnio()).append("\n\n");
        sb.append("Indicador;Valor\n");
        sb.append("Personas atendidas;").append(r.getPersonasAtendidas()).append("\n");
        sb.append("Clientes registrados;").append(r.getClientesRegistrados()).append("\n");
        sb.append("Visitantes sin registro;").append(r.getVisitantesAnonimos()).append("\n");
        sb.append("Total de atenciones;").append(r.getTotalAtenciones()).append("\n");
        sb.append("Calificacion promedio;").append(r.getPromedioCalificacion()).append("\n");
        sb.append("Satisfaccion (%);").append(r.getSatisfaccionPct()).append("\n");
        sb.append("Atenciones escaladas;").append(r.getEscaladas()).append("\n\n");
        sb.append("Calificacion;Cantidad;Porcentaje\n");
        r.getDistribucionEstrellas().forEach(c -> sb.append(c.getEtiqueta()).append(";")
                .append(c.getCantidad()).append(";").append(c.getPorcentaje()).append("\n"));
        sb.append("\nRecomendaciones de los clientes\n");
        sb.append("Fecha;Cliente;Estrellas;Tema;Comentario\n");
        r.getRecomendaciones().forEach(c -> sb.append(c.getFecha()).append(";").append(c.getCliente())
                .append(";").append(c.getEstrellas()).append(";").append(c.getTema()).append(";")
                .append(c.getTexto().replace(";", ",")).append("\n"));
        sb.append("\nSugerencias para la administracion\n");
        sb.append("Prioridad;Sugerencia;Detalle;Indicador\n");
        r.getSugerencias().forEach(s -> sb.append(s.getPrioridad()).append(";").append(s.getTitulo())
                .append(";").append(s.getDetalle().replace(";", ",")).append(";")
                .append(s.getIndicador()).append("\n"));
        return sb.toString();
    }

    // ------------------------------------------------------------------
    private long cantidadCanal(ReporteDTO r, String etiqueta) {
        return r.getPorCanal().stream()
                .filter(c -> c.getEtiqueta().equalsIgnoreCase(etiqueta))
                .mapToLong(ConteoDTO::getCantidad).sum();
    }

    private String etiquetaCanal(Canal canal) {
        if (canal == null) return "Otro";
        switch (canal) {
            case CHATBOT: return "Chatbot";
            case AGENTE: return "Agente";
            case CORREO: return "Correo";
            case TIENDA: return "Tienda";
            default: return "Otro";
        }
    }

    private double porcentaje(long parte, long total) {
        return total == 0 ? 0 : redondear(parte * 100.0 / total);
    }

    private double redondear(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
}
