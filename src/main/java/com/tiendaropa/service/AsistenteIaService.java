package com.tiendaropa.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Capa opcional de IA para las preguntas que no cubre la base de reglas.
 *
 * Dos proveedores, ambos gratuitos:
 *   - "gemini": capa gratuita de Google AI Studio. Necesita llave e internet.
 *   - "ollama": modelo corriendo en tu propio PC (localhost:11434). Sin llave
 *               y sin internet, util para sustentar sin depender del wifi.
 *
 * Si esta deshabilitada, mal configurada o falla la red, devuelve vacio y el
 * chatbot sigue respondiendo con el motor de reglas.
 */
@Service
public class AsistenteIaService {

    private static final Logger log = LoggerFactory.getLogger(AsistenteIaService.class);
    private static final String URL_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";

    private static final String INSTRUCCION = """
        Eres Aguja, asistente de atencion al cliente de UrbanStep, una tienda colombiana de ropa
        y calzado. Responde en espanol, maximo 4 frases, con trato cercano y de usted no, de tu.
        Solo hablas de la tienda: productos, tallas, envios, cambios, pagos, horarios y tratamiento
        de datos personales. Nunca pidas ni repitas numero de documento, direccion, telefono ni datos
        de tarjetas. Si no sabes algo, dilo y ofrece pasar el caso a un asesor humano.
        """;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8)).build();

    @Value("${app.chatbot.ia-habilitada:false}")
    private boolean habilitada;

    @Value("${app.chatbot.gemini-api-key:}")
    private String apiKey;

    @Value("${app.chatbot.gemini-modelo:gemini-2.0-flash}")
    private String modelo;

    @Value("${app.chatbot.proveedor:gemini}")
    private String proveedor;

    @Value("${app.chatbot.ollama-url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${app.chatbot.ollama-modelo:llama3.2:3b}")
    private String ollamaModelo;

    private boolean usaOllama() {
        return "ollama".equalsIgnoreCase(proveedor);
    }

    public boolean estaHabilitada() {
        if (!habilitada) return false;
        // Ollama no necesita llave: basta con que el servicio este corriendo.
        return usaOllama() || (apiKey != null && !apiKey.isBlank());
    }

    public Optional<String> responder(String pregunta) {
        if (!estaHabilitada()) return Optional.empty();
        return usaOllama() ? responderOllama(pregunta) : responderGemini(pregunta);
    }

    // ------------------------------------------------------------------
    // Ollama: local, gratuito, sin internet
    // ------------------------------------------------------------------
    private Optional<String> responderOllama(String pregunta) {
        try {
            ObjectNode cuerpo = mapper.createObjectNode();
            cuerpo.put("model", ollamaModelo);
            cuerpo.put("stream", false);

            var mensajes = cuerpo.putArray("messages");
            mensajes.addObject().put("role", "system").put("content", INSTRUCCION);
            mensajes.addObject().put("role", "user").put("content", pregunta);

            ObjectNode opciones = cuerpo.putObject("options");
            opciones.put("temperature", 0.4);
            opciones.put("num_predict", 300);

            HttpRequest peticion = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaUrl + "/api/chat"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(60))   // un modelo local tarda mas que la nube
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(cuerpo)))
                    .build();

            HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() != 200) {
                log.warn("Ollama respondio {}: {}", respuesta.statusCode(), respuesta.body());
                return Optional.empty();
            }
            JsonNode texto = mapper.readTree(respuesta.body()).path("message").path("content");
            if (texto.isMissingNode() || texto.asText().isBlank()) return Optional.empty();
            return Optional.of(texto.asText().trim());

        } catch (Exception e) {
            log.warn("No fue posible consultar Ollama en {}: {}", ollamaUrl, e.getMessage());
            return Optional.empty();
        }
    }

    // ------------------------------------------------------------------
    // Gemini: capa gratuita de Google AI Studio
    // ------------------------------------------------------------------
    private Optional<String> responderGemini(String pregunta) {
        try {
            ObjectNode cuerpo = mapper.createObjectNode();

            ObjectNode instruccion = cuerpo.putObject("system_instruction");
            instruccion.putArray("parts").addObject().put("text", INSTRUCCION);

            ObjectNode turno = cuerpo.putArray("contents").addObject();
            turno.put("role", "user");
            turno.putArray("parts").addObject().put("text", pregunta);

            ObjectNode config = cuerpo.putObject("generationConfig");
            config.put("temperature", 0.4);
            config.put("maxOutputTokens", 300);

            // La llave va en cabecera, no en la URL: con el formato nuevo de claves
            // (AQ.Ab8...) Google interpreta el ?key= como credencial OAuth y
            // responde 401 ACCESS_TOKEN_TYPE_UNSUPPORTED.
            HttpRequest peticion = HttpRequest.newBuilder()
                    .uri(URI.create(URL_BASE + modelo + ":generateContent"))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(cuerpo)))
                    .build();

            HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() != 200) {
                log.warn("Gemini respondio {}: {}", respuesta.statusCode(), respuesta.body());
                return Optional.empty();
            }
            JsonNode texto = mapper.readTree(respuesta.body())
                    .path("candidates").path(0).path("content").path("parts").path(0).path("text");
            return texto.isMissingNode() ? Optional.empty() : Optional.of(texto.asText().trim());

        } catch (Exception e) {
            log.warn("No fue posible consultar la IA: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
