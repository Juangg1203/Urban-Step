package com.tiendaropa.model;

/**
 * Estado de la comision de venta asociada a un pedido.
 *
 * NO_APLICA: el pedido no tiene vendedor asociado.
 * PENDIENTE: se calculo cuando el jefe acepto el pedido (estado PAGADO), pero
 *            el pedido todavia no se entrego.
 * CONFIRMADA: el pedido llego a ENTREGADO; la comision ya es definitiva.
 * ANULADA: el pedido se rechazo o se cancelo despues de haber calculado la comision.
 */
public enum EstadoComision {
    NO_APLICA("No aplica"),
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    ANULADA("Anulada");

    private final String etiqueta;
    EstadoComision(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
}
