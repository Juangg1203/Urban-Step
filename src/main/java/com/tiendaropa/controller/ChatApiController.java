package com.tiendaropa.controller;

import java.util.Map;

import com.tiendaropa.dto.RespuestaChatDTO;
import com.tiendaropa.model.Cliente;
import com.tiendaropa.service.ChatbotService;
import com.tiendaropa.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Endpoints que consume el widget de chat del sitio. */
@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    private final ChatbotService chatbotService;
    private final ClienteService clienteService;

    public ChatApiController(ChatbotService chatbotService, ClienteService clienteService) {
        this.chatbotService = chatbotService;
        this.clienteService = clienteService;
    }

    @PostMapping("/iniciar")
    public RespuestaChatDTO iniciar(Authentication autenticacion) {
        return chatbotService.iniciar(clienteActual(autenticacion));
    }

    @PostMapping("/mensaje")
    public RespuestaChatDTO mensaje(@RequestBody Map<String, String> cuerpo, Authentication autenticacion) {
        return chatbotService.responder(cuerpo.get("sesion"), cuerpo.get("texto"),
                clienteActual(autenticacion));
    }

    @PostMapping("/calificar")
    public ResponseEntity<Map<String, Object>> calificar(@RequestBody Map<String, String> cuerpo) {
        int estrellas;
        try {
            estrellas = Integer.parseInt(cuerpo.getOrDefault("estrellas", "0"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false,
                    "mensaje", "La calificacion debe ser un numero de 1 a 5"));
        }
        boolean ok = chatbotService.calificar(cuerpo.get("sesion"), estrellas,
                cuerpo.getOrDefault("recomendacion", ""));
        return ResponseEntity.ok(Map.of("ok", ok, "mensaje", ok
                ? "Gracias, tu calificacion quedo registrada en el reporte del mes."
                : "No encontramos esa conversacion."));
    }

    @PostMapping("/escalar")
    public ResponseEntity<Map<String, Object>> escalar(@RequestBody Map<String, String> cuerpo) {
        chatbotService.escalar(cuerpo.get("sesion"));
        return ResponseEntity.ok(Map.of("ok", true,
                "mensaje", "Un asesor humano continuara con tu caso."));
    }

    private Cliente clienteActual(Authentication autenticacion) {
        if (autenticacion == null || !autenticacion.isAuthenticated()
                || "anonymousUser".equals(autenticacion.getPrincipal())) {
            return null;
        }
        return clienteService.porNombreUsuario(autenticacion.getName()).orElse(null);
    }
}
