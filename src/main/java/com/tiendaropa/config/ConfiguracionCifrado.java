package com.tiendaropa.config;

import com.tiendaropa.util.CifradoAes;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Entrega la frase de cifrado a la utilidad AES al arrancar la aplicacion. */
@Component
public class ConfiguracionCifrado {

    @Value("${app.cifrado.clave}")
    private String clave;

    @PostConstruct
    public void iniciar() {
        CifradoAes.inicializar(clave);
    }
}
