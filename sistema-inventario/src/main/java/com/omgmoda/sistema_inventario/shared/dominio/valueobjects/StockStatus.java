package com.omgmoda.sistema_inventario.shared.dominio.valueobjects;

/**
 * Value Object puro que representa el estado de stock de una VarianteProducto.
 * Calculado por VarianteProducto.getStockStatus() en tiempo de ejecución;
 * nunca persiste en la base de datos (campo derivado).
 *
 * NORMAL    : stockActual > stockMinimo. Sin alertas.
 * BAJO_STOCK: stockActual <= stockMinimo pero > 0. Dispara alerta RF09.
 * AGOTADO   : stockActual == 0. No se puede registrar una venta de esta variante.
 */
public enum StockStatus {

    NORMAL,
    BAJO_STOCK,
    AGOTADO;

    /**
     * Retorna true si el estado requiere una alerta al administrador.
     */
    public boolean requiereAlerta() {
        return this == BAJO_STOCK || this == AGOTADO;
    }

    /**
     * Retorna true si la variante puede venderse (stock > 0).
     */
    public boolean disponibleParaVenta() {
        return this != AGOTADO;
    }
}
