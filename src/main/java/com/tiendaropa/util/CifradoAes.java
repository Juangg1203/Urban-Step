package com.tiendaropa.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Cifrado simetrico AES-256-GCM para los datos privados y sensibles.
 * El IV (12 bytes) se genera aleatorio en cada cifrado y viaja al inicio
 * del texto cifrado, todo codificado en Base64.
 */
public final class CifradoAes {

    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int LARGO_IV = 12;
    private static final int LARGO_TAG = 128;
    private static final String MARCA = "enc:";

    private static SecretKeySpec llave;

    private CifradoAes() { }

    /** La invoca ConfiguracionCifrado al arrancar la aplicacion. */
    public static void inicializar(String frase) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(frase.getBytes(StandardCharsets.UTF_8));
            llave = new SecretKeySpec(hash, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo derivar la llave de cifrado", e);
        }
    }

    public static String cifrar(String texto) {
        if (texto == null || texto.isBlank()) return texto;
        verificarLlave();
        try {
            byte[] iv = new byte[LARGO_IV];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, llave, new GCMParameterSpec(LARGO_TAG, iv));
            byte[] datos = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
            byte[] salida = new byte[iv.length + datos.length];
            System.arraycopy(iv, 0, salida, 0, iv.length);
            System.arraycopy(datos, 0, salida, iv.length, datos.length);
            return MARCA + Base64.getEncoder().encodeToString(salida);
        } catch (Exception e) {
            throw new IllegalStateException("Error al cifrar el dato", e);
        }
    }

    public static String descifrar(String cifrado) {
        if (cifrado == null || cifrado.isBlank()) return cifrado;
        if (!cifrado.startsWith(MARCA)) return cifrado; // dato antiguo sin cifrar
        verificarLlave();
        try {
            byte[] todo = Base64.getDecoder().decode(cifrado.substring(MARCA.length()));
            byte[] iv = new byte[LARGO_IV];
            System.arraycopy(todo, 0, iv, 0, LARGO_IV);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, llave, new GCMParameterSpec(LARGO_TAG, iv));
            byte[] datos = cipher.doFinal(todo, LARGO_IV, todo.length - LARGO_IV);
            return new String(datos, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "(dato ilegible)";
        }
    }

    private static void verificarLlave() {
        if (llave == null) {
            throw new IllegalStateException("La llave de cifrado no fue inicializada");
        }
    }
}
