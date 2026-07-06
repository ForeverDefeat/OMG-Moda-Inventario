package com.omgmoda.sistema_inventario.venta.dominio;

/**
 * Enum puro de dominio que define los estados del ciclo de vida de una Venta.
 *
 * PENDIENTE  : venta iniciada pero no confirmada. El stock aún no se descuenta.
 * COMPLETADA : venta confirmada y pagada. El stock fue descontado de las variantes.
 * ANULADA    : venta cancelada después de ser completada. Requiere reversión de stock.
 */
public enum EstadoVenta {
    PENDING_PAYMENT,
    COMPLETED,
    CANCELLED,
    EXPIRED,

    /** Alias historico aceptado solo para compatibilidad API/tests antiguos. */
    PENDIENTE,
    /** Alias historico aceptado solo para compatibilidad API/tests antiguos. */
    COMPLETADA,
    /** Alias historico aceptado solo para compatibilidad API/tests antiguos. */
    ANULADA;

    /**
     * Verifica si desde este estado se puede transicionar a COMPLETADA.
     */
    public boolean puedeCompletar() {
        return this == PENDING_PAYMENT || this == PENDIENTE;
    }

    /**
     * Verifica si desde este estado se puede transicionar a ANULADA.
     */
    public boolean puedeAnular() {
        return this == COMPLETED || this == COMPLETADA;
    }

    public EstadoVenta normalizado() {
        return switch (this) {
            case PENDIENTE -> PENDING_PAYMENT;
            case COMPLETADA -> COMPLETED;
            case ANULADA -> CANCELLED;
            default -> this;
        };
    }
}
