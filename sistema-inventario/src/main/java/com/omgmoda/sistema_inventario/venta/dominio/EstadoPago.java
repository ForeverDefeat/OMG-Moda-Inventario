package com.omgmoda.sistema_inventario.venta.dominio;
/**
 * Enumeracion de dominio que lista los estados o tipos validos para EstadoPago.
 */
public enum EstadoPago {
    PENDING,
    CONFIRMED,
    MANUALLY_CONFIRMED,
    EXPIRED,
    CANCELLED,
    FAILED
}
