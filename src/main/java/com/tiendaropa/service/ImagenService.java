package com.tiendaropa.service;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Guarda las imagenes de producto en una carpeta del disco.
 *
 * En la base solo queda el nombre del archivo. Meter los binarios dentro de
 * MySQL la infla, encarece los respaldos y obliga a pasar la imagen por la
 * aplicacion cada vez que alguien abre el catalogo.
 *
 * El nombre se genera aqui, nunca se usa el que trae el navegador: un archivo
 * llamado "../../algo.jsp" podria escribir fuera de la carpeta prevista.
 */
@Service
public class ImagenService {

    private static final Logger log = LoggerFactory.getLogger(ImagenService.class);

    // Una foto de celular facilmente pesa 5-6 MB; 3 MB rechazaba de mas.
    private static final long TAMANO_MAXIMO = 8 * 1024 * 1024;   // 8 MB
    private static final List<String> EXTENSIONES = List.of("jpg", "jpeg", "png", "webp", "gif");
    private static final List<String> TIPOS = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");

    @Value("${app.imagenes.carpeta}")
    private String carpeta;

    /** Excepcion con mensaje pensado para mostrarle al usuario tal cual. */
    public static class ImagenInvalida extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public ImagenInvalida(String mensaje) { super(mensaje); }
    }

    /**
     * Guarda el archivo y devuelve el nombre con el que quedo, o null si no
     * se subio nada (el formulario puede venir sin imagen y eso es valido).
     */
    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) return null;

        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new ImagenInvalida("La imagen pesa mas de 3 MB. Reducela antes de subirla.");
        }

        String tipo = archivo.getContentType();
        if (tipo == null || !TIPOS.contains(tipo.toLowerCase(Locale.ROOT))) {
            throw new ImagenInvalida("Solo se aceptan imagenes JPG, PNG, WEBP o GIF.");
        }

        String extension = extensionDe(archivo.getOriginalFilename());
        if (!EXTENSIONES.contains(extension)) {
            throw new ImagenInvalida("La extension del archivo no corresponde a una imagen.");
        }

        try {
            Path destino = carpetaDestino();
            // Nombre generado por nosotros: el del navegador no es de fiar.
            String nombre = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path archivoFinal = destino.resolve(nombre);
            Files.copy(archivo.getInputStream(), archivoFinal, StandardCopyOption.REPLACE_EXISTING);
            log.info("Imagen guardada: {}", archivoFinal);
            return nombre;
        } catch (IOException e) {
            throw new ImagenInvalida("No fue posible guardar la imagen: " + e.getMessage());
        }
    }

    /** Borra el archivo anterior cuando se reemplaza o se elimina el producto. */
    public void borrar(String nombre) {
        if (nombre == null || nombre.isBlank()) return;
        if (nombre.startsWith("http")) return;   // imagen externa: no es nuestra
        try {
            Files.deleteIfExists(carpetaDestino().resolve(nombre));
        } catch (IOException e) {
            // Que no se pueda borrar un archivo no debe tumbar la operacion.
            log.warn("No se pudo borrar la imagen {}: {}", nombre, e.getMessage());
        }
    }

    public Path carpetaDestino() throws IOException {
        Path ruta = Paths.get(carpeta).toAbsolutePath().normalize();
        Files.createDirectories(ruta);
        return ruta;
    }

    private String extensionDe(String nombreOriginal) {
        if (nombreOriginal == null) return "";
        int punto = nombreOriginal.lastIndexOf('.');
        if (punto < 0 || punto == nombreOriginal.length() - 1) return "";
        return nombreOriginal.substring(punto + 1).toLowerCase(Locale.ROOT);
    }
}
