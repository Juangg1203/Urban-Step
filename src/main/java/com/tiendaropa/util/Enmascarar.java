package com.tiendaropa.util;

/**
 * Enmascaramiento para los roles que necesitan verificar un dato
 * pero no tienen permiso de leerlo completo (por ejemplo el agente
 * confirmando la identidad de quien llama).
 */
public final class Enmascarar {

    private Enmascarar() { }

    public static String documento(String valor) {
        if (valor == null || valor.length() < 5) return "*****";
        return valor.substring(0, 2) + "*".repeat(valor.length() - 4)
                + valor.substring(valor.length() - 2);
    }

    public static String telefono(String valor) {
        if (valor == null || valor.length() < 4) return "*****";
        return "*".repeat(valor.length() - 3) + valor.substring(valor.length() - 3);
    }

    public static String correo(String valor) {
        if (valor == null || !valor.contains("@")) return "*****";
        int arroba = valor.indexOf('@');
        String usuario = valor.substring(0, arroba);
        String dominio = valor.substring(arroba);
        if (usuario.length() <= 2) return usuario.charAt(0) + "***" + dominio;
        return usuario.charAt(0) + "*".repeat(usuario.length() - 2)
                + usuario.charAt(usuario.length() - 1) + dominio;
    }

    public static String direccion(String valor) {
        if (valor == null || valor.isBlank()) return "*****";
        String[] partes = valor.trim().split("\\s+");
        return partes[0] + " " + "*".repeat(6);
    }

    public static String oculto() {
        return "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022";
    }
}
