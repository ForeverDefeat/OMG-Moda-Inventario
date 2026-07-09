package com.omgmoda.sistema_inventario.venta.dominio;
/**
 * Enumeracion de dominio que lista los estados o tipos validos para MetodoPago.
 */
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
