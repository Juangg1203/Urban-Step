package com.tiendaropa.model;

public enum Tema {
    TALLAS("Tallas y medidas"),
    ENVIOS("Envios y entregas"),
    DEVOLUCIONES("Cambios y devoluciones"),
    PAGOS("Pagos y facturacion"),
    PRODUCTO("Informacion de producto"),
    DATOS("Tratamiento de datos personales"),
    CUENTA("Cuenta y acceso"),
    PEDIDO("Estado del pedido"),
    PROMOCIONES("Promociones y descuentos"),
    OTRO("Otro");

    private final String etiqueta;
    Tema(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
}
