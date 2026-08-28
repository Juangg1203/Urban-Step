package com.tiendaropa.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiendaropa.model.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Integracion con Wompi (pasarela de pagos colombiana).
 *
 * Como funciona el flujo, que es lo que hay que poder explicar:
 *
 *   1. La tienda arma un formulario con el monto, la referencia y una FIRMA,
 *      y manda al cliente al Checkout de Wompi.
 *   2. El cliente paga alla. Los datos de la tarjeta nunca pasan por nuestro
 *      servidor: esa es la razon de usar una pasarela y no cobrar uno mismo.
 *   3. Wompi devuelve al cliente a nuestra pagina de retorno con el id de la
 *      transaccion, y ademas envia un evento al webhook.
 *   4. Nosotros CONSULTAMOS la transaccion contra la API de Wompi antes de dar
 *      por bueno el pago.
 *
 * El punto 4 es el importante. Nunca se confia en lo que llega por la URL de
 * retorno: cualquiera puede escribir a mano "...retorno?id=123&estado=APPROVED".
 * El estado real se le pregunta a Wompi o se valida con la firma del evento.
 *
 * NOTA: verificar los nombres de los campos en docs.wompi.co antes de usar en
 * produccion; la API cambia con cierta frecuencia.
 */
@Service
public class WompiService {

    private static final Logger log = LoggerFactory.getLogger(WompiService.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();

    @Value("${app.pago.wompi.habilitado:false}")
    private boolean habilitado;

    /**
     * "real" usa el checkout de Wompi; "simulado" usa una pasarela local que
     * reproduce el mismo flujo sin cuenta ni internet.
     *
     * El modo simulado NO es una maqueta: recorre los mismos pasos (firma,
     * referencia unica, estado consultado, idempotencia) y termina llamando al
     * mismo metodo del servicio. Lo unico que cambia es quien responde: en vez
     * de Wompi, un controlador local. Sirve para desarrollar y demostrar
     * mientras el comercio esta en tramite.
     */
    @Value("${app.pago.wompi.modo:simulado}")
    private String modo;

    @Value("${app.pago.wompi.llave-publica:}")
    private String llavePublica;

    /** Secreto de integridad: firma el formulario del checkout. */
    @Value("${app.pago.wompi.llave-integridad:}")
    private String llaveIntegridad;

    /** Secreto de eventos: valida los webhooks que envia Wompi. */
    @Value("${app.pago.wompi.llave-eventos:}")
    private String llaveEventos;

    @Value("${app.pago.wompi.url-api:https://sandbox.wompi.co/v1}")
    private String urlApi;

    @Value("${app.pago.wompi.url-checkout:https://checkout.wompi.co/p/}")
    private String urlCheckout;

    @Value("${app.pago.wompi.url-retorno:http://localhost:8080/pagos/wompi/retorno}")
    private String urlRetorno;

    @Value("${app.pago.wompi.moneda:COP}")
    private String moneda;

    public boolean isModoSimulado() { return "simulado".equalsIgnoreCase(modo); }

    public boolean estaHabilitado() {
        if (!habilitado) return false;
        // El modo simulado no necesita llaves: por eso existe.
        if (isModoSimulado()) return true;
        return llavePublica != null && !llavePublica.isBlank()
                && llaveIntegridad != null && !llaveIntegridad.isBlank();
    }

    public String getLlavePublica() { return llavePublica; }
    public String getUrlCheckout()  { return urlCheckout; }
    public String getUrlRetorno()   { return urlRetorno; }
    public String getMoneda()       { return moneda; }

    /** true si la configuracion apunta al ambiente de pruebas. */
    public boolean isSandbox() {
        return urlApi != null && urlApi.contains("sandbox");
    }

    // ==================================================================
    //  Salida hacia el checkout
    // ==================================================================

    /**
     * Genera la referencia unica de la transaccion.
     *
     * Lleva un sufijo aleatorio porque Wompi rechaza una referencia repetida:
     * si el cliente intenta pagar, falla y vuelve a intentar, el segundo
     * intento necesita una referencia nueva o la pasarela lo bloquea.
     */
    public String nuevaReferencia(Pedido pedido) {
        return pedido.getNumero() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Firma de integridad: SHA-256 de referencia + monto + moneda + secreto.
     *
     * Sin esto, cualquiera podria copiar el formulario, cambiar el monto a
     * 1000 pesos y pagar. La firma se calcula en el SERVIDOR, nunca en el
     * navegador, porque de lo contrario el secreto quedaria a la vista.
     */
    public String firmaIntegridad(String referencia, long centavos) {
        String secreto = (llaveIntegridad == null || llaveIntegridad.isBlank())
                ? "clave-local-de-simulacion" : llaveIntegridad;
        return sha256(referencia + centavos + moneda + secreto);
    }

    /** Comprueba una firma recibida. Lo usa la pasarela simulada. */
    public boolean firmaValida(String referencia, long centavos, String firma) {
        return firma != null && firma.equals(firmaIntegridad(referencia, centavos));
    }

    // ==================================================================
    //  Verificacion
    // ==================================================================

    /** Lo que sabemos de una transaccion despues de preguntarle a Wompi. */
    public static class ResultadoPago {
        private final boolean consultado;
        private final String estado;      // APPROVED, DECLINED, VOIDED, ERROR, PENDING
        private final String referencia;
        private final String metodo;
        private final long centavos;
        private final String mensaje;

        public ResultadoPago(boolean consultado, String estado, String referencia,
                             String metodo, long centavos, String mensaje) {
            this.consultado = consultado;
            this.estado = estado;
            this.referencia = referencia;
            this.metodo = metodo;
            this.centavos = centavos;
            this.mensaje = mensaje;
        }
        public boolean isConsultado() { return consultado; }
        public String getEstado() { return estado; }
        public String getReferencia() { return referencia; }
        public String getMetodo() { return metodo; }
        public long getCentavos() { return centavos; }
        public String getMensaje() { return mensaje; }
        public boolean isAprobado() { return "APPROVED".equals(estado); }
    }

    /**
     * Consulta el estado real de una transaccion contra la API de Wompi.
     * Esta es la unica fuente de verdad sobre si el pago entro.
     */
    public Optional<ResultadoPago> consultar(String transaccionId) {
        if (transaccionId == null || transaccionId.isBlank()) return Optional.empty();
        try {
            HttpRequest peticion = HttpRequest.newBuilder()
                    .uri(URI.create(urlApi + "/transactions/" + transaccionId))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() != 200) {
                log.warn("Wompi respondio {} al consultar {}: {}",
                        respuesta.statusCode(), transaccionId, respuesta.body());
                return Optional.empty();
            }

            JsonNode datos = mapper.readTree(respuesta.body()).path("data");
            if (datos.isMissingNode()) return Optional.empty();

            return Optional.of(new ResultadoPago(
                    true,
                    datos.path("status").asText(""),
                    datos.path("reference").asText(""),
                    datos.path("payment_method_type").asText(""),
                    datos.path("amount_in_cents").asLong(0),
                    datos.path("status_message").asText("")));

        } catch (Exception e) {
            log.warn("No fue posible consultar la transaccion {} en Wompi: {}",
                    transaccionId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Valida la firma del evento que envia Wompi al webhook.
     *
     * El cuerpo del evento trae la lista de campos que entran en la firma; se
     * concatenan en ese orden, mas el timestamp y el secreto de eventos. Sin
     * esta validacion, cualquiera podria enviarnos un "pago aprobado" falso a
     * la URL del webhook y llevarse la mercancia gratis.
     */
    public boolean firmaEventoValida(JsonNode evento) {
        try {
            if (llaveEventos == null || llaveEventos.isBlank()) {
                log.warn("Evento de Wompi recibido sin llave de eventos configurada: se rechaza.");
                return false;
            }
            JsonNode firma = evento.path("signature");
            JsonNode propiedades = firma.path("properties");
            if (!propiedades.isArray()) return false;

            StringBuilder base = new StringBuilder();
            for (JsonNode propiedad : propiedades) {
                // "transaction.status" -> data.transaction.status
                JsonNode valor = evento.path("data");
                for (String parte : propiedad.asText().split("\\.")) {
                    valor = valor.path(parte);
                }
                base.append(valor.asText(""));
            }
            base.append(evento.path("timestamp").asText(""));
            base.append(llaveEventos);

            String calculada = sha256(base.toString());
            boolean valida = calculada.equalsIgnoreCase(firma.path("checksum").asText(""));
            if (!valida) log.warn("Evento de Wompi con firma invalida: se ignora.");
            return valida;

        } catch (Exception e) {
            log.warn("No fue posible validar la firma del evento: {}", e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------------
    private String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] resumen = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : resumen) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible calcular la firma", e);
        }
    }
}
