package com.omgmoda.sistema_inventario.venta.dominio;

/**
 * Enum puro de dominio que define los estados del ciclo de vida de una Venta.
 *
 * PENDIENTE  : venta iniciada pero no confirmada. El stock aún no se descuenta.
 * COMPLETADA : venta confirmada y pagada. El stock fue descontado de las variantes.
 * ANULADA    : venta cancelada después de ser completada. Requiere reversión de stock.
 */
public enum EstadoVenta {
    PENDIENTE,
    COMPLETADA,
    ANULADA;

    /**
     * Verifica si desde este estado se puede transicionar a COMPLETADA.
     */
    public boolean puedeCompletar() {
        return this == PENDIENTE;
    }

    /**
     * Verifica si desde este estado se puede transicionar a ANULADA.
     */
    public boolean puedeAnular() {
        return this == COMPLETADA;
    }
}
