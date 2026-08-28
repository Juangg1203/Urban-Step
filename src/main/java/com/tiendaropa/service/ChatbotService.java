package com.tiendaropa.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.tiendaropa.dto.RespuestaChatDTO;
import com.tiendaropa.model.*;
import com.tiendaropa.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Chatbot de atencion al cliente, disponible 24/7.
 * Orden de resolucion: reglas -> IA opcional -> escalamiento a un agente.
 * Cada conversacion genera una Atencion, que es lo que despues alimenta
 * el reporte mensual.
 *
 * Dos intenciones NO se responden con texto fijo: estado del pedido y
 * busqueda de producto. Esas consultan la base en el momento, porque un
 * "tu pedido va en camino" generico para alguien logueado que si tiene el
 * numero a la mano es peor que no responder nada.
 */
@Service
public class ChatbotService {

    private final ConversacionRepository conversacionRepo;
    private final MensajeChatRepository mensajeRepo;
    private final AtencionRepository atencionRepo;
    private final AsistenteIaService asistenteIa;
    private final AuditoriaService auditoria;
    private final PedidoService pedidoService;
    private final ProductoService productoService;

    public ChatbotService(ConversacionRepository conversacionRepo, MensajeChatRepository mensajeRepo,
                          AtencionRepository atencionRepo, AsistenteIaService asistenteIa,
                          AuditoriaService auditoria, PedidoService pedidoService,
                          ProductoService productoService) {
        this.conversacionRepo = conversacionRepo;
        this.mensajeRepo = mensajeRepo;
        this.atencionRepo = atencionRepo;
        this.asistenteIa = asistenteIa;
        this.auditoria = auditoria;
        this.pedidoService = pedidoService;
        this.productoService = productoService;
    }

    // ------------------------------------------------------------------
    @Transactional
    public RespuestaChatDTO iniciar(Cliente cliente) {
        Conversacion conversacion = new Conversacion();
        conversacion.setSesion(UUID.randomUUID().toString());
        conversacion.setCliente(cliente);
        conversacionRepo.save(conversacion);

        Atencion atencion = new Atencion(cliente, Canal.CHATBOT, Tema.OTRO);
        atencionRepo.save(atencion);
        conversacion.setAtencion(atencion);
        conversacionRepo.save(conversacion);

        String saludo = BaseConocimiento.saludoPorHora(
                cliente != null ? cliente.getNombres() : null);
        guardarMensaje(conversacion, "BOT", saludo, "SALUDO", false);

        RespuestaChatDTO r = new RespuestaChatDTO();
        r.setSesion(conversacion.getSesion());
        r.setRespuesta(saludo);
        r.setIntencion("SALUDO");
        r.setSugerencias(BaseConocimiento.SUGERENCIAS_INICIALES);
        return r;
    }

    // ------------------------------------------------------------------
    @Transactional
    public RespuestaChatDTO responder(String sesion, String texto, Cliente cliente) {
        Conversacion conversacion = conversacionRepo.findBySesion(sesion)
                .orElseGet(() -> {
                    Conversacion nueva = new Conversacion();
                    nueva.setSesion(sesion != null && !sesion.isBlank() ? sesion : UUID.randomUUID().toString());
                    nueva.setCliente(cliente);
                    conversacionRepo.save(nueva);
                    Atencion a = new Atencion(cliente, Canal.CHATBOT, Tema.OTRO);
                    atencionRepo.save(a);
                    nueva.setAtencion(a);
                    return conversacionRepo.save(nueva);
                });

        guardarMensaje(conversacion, "CLIENTE", recortar(texto), null, false);

        RespuestaChatDTO r = new RespuestaChatDTO();
        r.setSesion(conversacion.getSesion());

        BaseConocimiento.Regla regla = BaseConocimiento.buscar(texto);

        if (regla != null) {
            // Estas dos intenciones se responden con datos reales de la base,
            // no con el texto fijo de la regla, cuando hay con que.
            Optional<String> respuestaViva = respuestaConDatosReales(regla.getIntencion(), texto, cliente);

            r.setRespuesta(respuestaViva.orElse(regla.getRespuesta()));
            r.setIntencion(regla.getIntencion());
            // Una queja abierta tambien pasa a una persona: no la resuelve un bot.
            r.setEscalar("AGENTE".equals(regla.getIntencion()) || "QUEJA".equals(regla.getIntencion()));
            r.setPedirCalificacion("DESPEDIDA".equals(regla.getIntencion()));
            r.setSugerencias(regla.getSugerencias());
            actualizarTema(conversacion, regla.getTema());
            if (r.isEscalar()) escalar(conversacion.getSesion());
        } else {
            Optional<String> ia = asistenteIa.responder(texto);
            if (ia.isPresent()) {
                r.setRespuesta(ia.get());
                r.setIntencion("IA");
                r.setGeneradaPorIa(true);
                r.setSugerencias(BaseConocimiento.SUGERENCIAS_INICIALES);
            } else {
                r.setRespuesta("No estoy seguro de haber entendido. Puedo ayudarte con tallas, catalogo, "
                        + "envios, el estado de tu pedido, cambios, pagos, horarios o el manejo de tus datos. "
                        + "Elige una de las opciones de abajo, o escribe \"asesor\" y paso tu caso a una "
                        + "persona del equipo.");
                r.setIntencion("SIN_COINCIDENCIA");
                r.setSugerencias(BaseConocimiento.SUGERENCIAS_INICIALES);
            }
        }

        guardarMensaje(conversacion, "BOT", r.getRespuesta(), r.getIntencion(), r.isGeneradaPorIa());
        return r;
    }

    // ------------------------------------------------------------------
    @Transactional
    public void escalar(String sesion) {
        conversacionRepo.findBySesion(sesion).ifPresent(conversacion -> {
            conversacion.setEscalada(true);
            conversacionRepo.save(conversacion);
            Atencion atencion = conversacion.getAtencion();
            if (atencion != null) {
                atencion.setEstado(EstadoAtencion.ESCALADA);
                atencionRepo.save(atencion);
            }
        });
    }

    @Transactional
    public boolean calificar(String sesion, int estrellas, String recomendacion) {
        Optional<Conversacion> posible = conversacionRepo.findBySesion(sesion);
        if (posible.isEmpty() || posible.get().getAtencion() == null) return false;

        Conversacion conversacion = posible.get();
        conversacion.setFechaFin(LocalDateTime.now());
        conversacionRepo.save(conversacion);

        Atencion atencion = conversacion.getAtencion();
        atencion.setCalificacion(Math.max(1, Math.min(5, estrellas)));
        atencion.setRecomendacion(recortar(recomendacion));
        atencion.setFechaCierre(LocalDateTime.now());
        atencion.setResuelta(estrellas >= 3 && atencion.getEstado() != EstadoAtencion.ESCALADA);
        if (atencion.getEstado() != EstadoAtencion.ESCALADA) {
            atencion.setEstado(EstadoAtencion.CERRADA);
        }
        atencionRepo.save(atencion);

        auditoria.registrar("CALIFICACION", NivelDato.PUBLICO, "Atencion", atencion.getId(),
                "Calificacion " + estrellas + "/5 registrada desde el chatbot");
        return true;
    }

    public List<MensajeChat> historial(Long conversacionId) {
        return mensajeRepo.findByConversacionIdOrderByFechaAsc(conversacionId);
    }

    public boolean iaActiva() { return asistenteIa.estaHabilitada(); }

    // ------------------------------------------------------------------
    /**
     * Para ESTADO_PEDIDO y PRODUCTO, se intenta responder con datos reales
     * en vez del texto fijo de la regla. Optional.empty() significa "no hay
     * con que": ahi el llamador usa la respuesta generica de siempre.
     */
    private Optional<String> respuestaConDatosReales(String intencion, String textoUsuario, Cliente cliente) {
        if ("ESTADO_PEDIDO".equals(intencion)) return estadoDelUltimoPedido(cliente);
        if ("PRODUCTO".equals(intencion)) return buscarEnCatalogo(textoUsuario);
        return Optional.empty();
    }

    private Optional<String> estadoDelUltimoPedido(Cliente cliente) {
        if (cliente == null) return Optional.empty();   // sin sesion, no hay a quien buscarle nada
        List<Pedido> pedidos = pedidoService.deCliente(cliente.getId());
        if (pedidos.isEmpty()) return Optional.empty();

        Pedido ultimo = pedidos.get(0);   // vienen ordenados del mas reciente al mas viejo
        StringBuilder texto = new StringBuilder("Tu pedido mas reciente es ")
                .append(ultimo.getNumero()).append(", y va en: ")
                .append(ultimo.getEstado().getEtiqueta()).append(".");

        if (ultimo.getEstado() == EstadoPedido.DESPACHADO && ultimo.getNumeroGuia() != null) {
            texto.append(" La guia es ").append(ultimo.getNumeroGuia()).append(".");
        }
        if (ultimo.getEstado() == EstadoPedido.PENDIENTE_PAGO) {
            texto.append(" Todavia no registra un pago: puedes pagarlo desde Mis pedidos.");
        }
        if (ultimo.getEstado() == EstadoPedido.RECHAZADO && ultimo.getMotivoDecision() != null) {
            texto.append(" Motivo: ").append(ultimo.getMotivoDecision());
        }
        texto.append(" Puedes ver el detalle completo en Mis pedidos.");
        return Optional.of(texto.toString());
    }

    /** Busca en el catalogo real las palabras del mensaje que no son ruido. */
    private Optional<String> buscarEnCatalogo(String textoUsuario) {
        String limpio = BaseConocimiento.normalizar(textoUsuario);
        String[] ruido = {"catalogo","precio","cuanto","cuesta","vale","disponible","stock","color",
                "material","que","tienen","venden","de","el","la","los","las","un","una","para","con"};
        String palabraClave = null;
        for (String palabra : limpio.split("\\s+")) {
            if (palabra.length() < 4) continue;
            boolean esRuido = false;
            for (String r : ruido) if (palabra.equals(r)) { esRuido = true; break; }
            if (!esRuido) { palabraClave = palabra; break; }
        }
        if (palabraClave == null) return Optional.empty();

        List<Producto> encontrados = productoService.catalogo(null, null, palabraClave);
        if (encontrados.isEmpty()) return Optional.empty();

        StringBuilder texto = new StringBuilder("En el catalogo tengo esto:\n");
        int mostrados = 0;
        for (Producto p : encontrados) {
            if (mostrados >= 4) break;
            texto.append("- ").append(p.getNombre()).append(": $")
                    .append(p.getPrecio().toBigInteger())
                    .append(p.getStock() > 0 ? "" : " (agotado)").append("\n");
            mostrados++;
        }
        if (encontrados.size() > mostrados) {
            texto.append("Hay ").append(encontrados.size() - mostrados).append(" mas en el catalogo.");
        }
        return Optional.of(texto.toString().trim());
    }

    // ------------------------------------------------------------------
    private void actualizarTema(Conversacion conversacion, Tema tema) {
        Atencion atencion = conversacion.getAtencion();
        if (atencion != null && tema != Tema.OTRO) {
            atencion.setTema(tema);
            atencionRepo.save(atencion);
        }
    }

    private void guardarMensaje(Conversacion conversacion, String emisor, String texto,
                                String intencion, boolean porIa) {
        MensajeChat mensaje = new MensajeChat(conversacion, emisor, texto);
        mensaje.setIntencion(intencion);
        mensaje.setRespondidoIa(porIa);
        mensajeRepo.save(mensaje);
    }

    private String recortar(String texto) {
        if (texto == null) return "";
        String limpio = texto.trim();
        return limpio.length() > 600 ? limpio.substring(0, 600) : limpio;
    }
}
