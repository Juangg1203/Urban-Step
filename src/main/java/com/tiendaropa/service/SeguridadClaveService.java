package com.tiendaropa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.tiendaropa.dto.FuerzaClaveDTO;
import org.springframework.stereotype.Service;

/**
 * Evalua que tan segura es una clave antes de crear la cuenta.
 *
 * La misma logica corre en el navegador (recursos/js/seguridad-clave.js) para
 * dar aviso en vivo, y aqui en el servidor para decidir de verdad: la
 * validacion del navegador es comodidad, no seguridad, porque cualquiera
 * puede saltarsela enviando el formulario por otro medio.
 */
@Service
public class SeguridadClaveService {

    /** Puntaje minimo para aceptar el registro. */
    public static final int MINIMO_ACEPTABLE = 50;

    /** Claves y patrones que aparecen en cualquier lista de filtraciones. */
    private static final Set<String> PROHIBIDAS = Set.of(
            "123456", "1234567", "12345678", "123456789", "1234567890",
            "password", "contrasena", "clave", "qwerty", "abc123", "111111",
            "colombia", "admin", "administrador", "usuario", "iloveyou",
            "bucaramanga", "santander", "tiendaropa", "urbanstep");

    private static final String[] SECUENCIAS = {
            "abcdef", "qwerty", "asdfgh", "zxcvbn", "123456", "098765" };

    public FuerzaClaveDTO evaluar(String clave, String nombreUsuario, String correo) {
        FuerzaClaveDTO r = new FuerzaClaveDTO();
        List<String> avisos = new ArrayList<>();

        if (clave == null || clave.isBlank()) {
            r.setPuntaje(0);
            r.setNivel("VACIA");
            r.setEtiqueta("Sin clave");
            avisos.add("Escribe una clave");
            r.setAvisos(avisos);
            r.setAceptable(false);
            return r;
        }

        String minus = clave.toLowerCase();
        int puntaje = 0;

        // --- longitud: es lo que mas pesa ---
        if (clave.length() >= 12)      puntaje += 40;
        else if (clave.length() >= 10) puntaje += 30;
        else if (clave.length() >= 8)  puntaje += 20;
        else {
            puntaje += 5;
            avisos.add("Usa al menos 8 caracteres; 12 es mucho mejor");
        }

        // --- variedad de caracteres ---
        boolean tieneMinuscula = clave.chars().anyMatch(Character::isLowerCase);
        boolean tieneMayuscula = clave.chars().anyMatch(Character::isUpperCase);
        boolean tieneNumero    = clave.chars().anyMatch(Character::isDigit);
        boolean tieneSimbolo   = clave.chars().anyMatch(c -> !Character.isLetterOrDigit(c));

        if (tieneMinuscula) puntaje += 10;
        if (tieneMayuscula) puntaje += 15; else avisos.add("Agrega alguna mayuscula");
        if (tieneNumero)    puntaje += 15; else avisos.add("Agrega algun numero");
        if (tieneSimbolo)   puntaje += 15;

        // --- penalizaciones ---
        for (String prohibida : PROHIBIDAS) {
            if (minus.contains(prohibida)) {
                puntaje -= 35;
                avisos.add("Contiene una palabra muy comun en claves filtradas");
                break;
            }
        }
        for (String secuencia : SECUENCIAS) {
            if (minus.contains(secuencia)) {
                puntaje -= 20;
                avisos.add("Evita secuencias seguidas del teclado o de numeros");
                break;
            }
        }
        if (clave.matches("(?i).*(.)\\1{2,}.*")) {
            puntaje -= 10;
            avisos.add("Evita repetir el mismo caracter tres veces seguidas");
        }
        if (nombreUsuario != null && nombreUsuario.length() >= 4
                && minus.contains(nombreUsuario.toLowerCase())) {
            puntaje -= 30;
            avisos.add("No uses tu nombre de usuario dentro de la clave");
        }
        if (correo != null && correo.contains("@")) {
            String parte = correo.substring(0, correo.indexOf('@')).toLowerCase();
            if (parte.length() >= 4 && minus.contains(parte)) {
                puntaje -= 30;
                avisos.add("No uses tu correo dentro de la clave");
            }
        }
        if (minus.matches("^[a-z]+$") || minus.matches("^[0-9]+$")) {
            puntaje -= 10;
            avisos.add("Mezcla letras, numeros y algun simbolo");
        }

        puntaje = Math.max(0, Math.min(100, puntaje));
        r.setPuntaje(puntaje);
        r.setAvisos(avisos);
        r.setAceptable(puntaje >= MINIMO_ACEPTABLE);

        if (puntaje < 25)      { r.setNivel("MUY_DEBIL"); r.setEtiqueta("Muy debil"); }
        else if (puntaje < 50) { r.setNivel("DEBIL");     r.setEtiqueta("Debil"); }
        else if (puntaje < 70) { r.setNivel("ACEPTABLE"); r.setEtiqueta("Aceptable"); }
        else if (puntaje < 90) { r.setNivel("BUENA");     r.setEtiqueta("Buena"); }
        else                   { r.setNivel("EXCELENTE"); r.setEtiqueta("Excelente"); }

        return r;
    }
}
