package com.omgmoda.sistema_inventario.venta.dominio;

public enum MetodoPago {
    EFECTIVO,
    YAPE,
    PLIN,
    CARD,
    TARJETA;

    public MetodoPago normalizado() {
        return this == TARJETA ? CARD : this;
    }

    public boolean esDigital() {
        MetodoPago value = normalizado();
        return value == YAPE || value == PLIN || value == CARD;
    }
}
