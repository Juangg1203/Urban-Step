package com.tiendaropa.model;

/**
 * Ciclo de vida de un pedido, en orden.
 *
 * El cliente paga directo, sin esperar una aprobacion previa: lo unico que
 * controla el sistema antes de eso es que no compre mas de lo que hay en
 * inventario (se valida en el checkout). Despues del pago hay dos filtros
 * humanos en cascada, cada uno responsable de una sola cosa:
 *
 *   1. El VENDEDOR confirma que el dinero realmente entro.
 *   2. El JEFE da el visto bueno final antes de que se despache.
 *
 * Cada rol solo ve la bandeja que le corresponde: el vendedor no ve
 * despachos, el bodeguero no ve pagos. Eso se aplica en las consultas del
 * repositorio y en las vistas, no aqui; este enum solo describe la secuencia.
 */
public enum EstadoPedido {
    COTIZACION("Cotizacion", 0),
    PENDIENTE_PAGO("Pendiente de pago", 1),
    PAGO_EN_VERIFICACION("Pago en verificacion", 2),
    PENDIENTE_ACEPTACION_JEFE("Esperando visto bueno del jefe", 3),
    RECHAZADO("Rechazado", 3),
    PAGADO("Pago confirmado", 4),
    EN_PREPARACION("En preparacion en bodega", 5),
    DESPACHADO("Despachado", 6),
    ENTREGADO("Entregado", 7),
    CANCELADO("Cancelado", 0);

    private final String etiqueta;
    private final int paso;   // posicion en la linea de seguimiento

    EstadoPedido(String etiqueta, int paso) {
        this.etiqueta = etiqueta;
        this.paso = paso;
    }
    public String getEtiqueta() { return etiqueta; }
    public int getPaso() { return paso; }

    public boolean isFinalizado() {
        return this == ENTREGADO || this == CANCELADO || this == RECHAZADO;
    }
    /** Mientras no salga de bodega el cliente todavia puede cancelar. */
    public boolean isCancelablePorCliente() {
        return this == COTIZACION || this == PENDIENTE_PAGO
            || this == PAGO_EN_VERIFICACION || this == PENDIENTE_ACEPTACION_JEFE;
    }
}
