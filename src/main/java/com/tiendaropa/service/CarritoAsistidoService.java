package com.tiendaropa.service;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

/**
 * Carrito separado para cuando un VENDEDOR arma una venta asistida por un
 * cliente. Es una copia de sesion distinta al carrito personal del vendedor
 * (CarritoService): si no lo fueran, el vendedor no podria tener su propio
 * carrito como comprador y a la vez estar armando una venta por otra
 * persona sin que se mezclen.
 */
@Service("carritoAsistido")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CarritoAsistidoService extends CarritoService {
    private static final long serialVersionUID = 1L;

    /** A que cliente se le esta armando la venta. Se limpia al confirmar o cancelar. */
    private Long clienteObjetivoId;
    private String clienteObjetivoNombre;

    public Long getClienteObjetivoId() { return clienteObjetivoId; }
    public String getClienteObjetivoNombre() { return clienteObjetivoNombre; }

    public void elegirCliente(Long id, String nombre) {
        this.clienteObjetivoId = id;
        this.clienteObjetivoNombre = nombre;
        vaciar();   // cambiar de cliente a mitad de camino no debe arrastrar el carrito anterior
    }

    public boolean isTieneClienteElegido() { return clienteObjetivoId != null; }

    public void reiniciar() {
        this.clienteObjetivoId = null;
        this.clienteObjetivoNombre = null;
        vaciar();
    }
}
