package com.omgmoda.sistema_inventario.inventario.dominio;

/**
 * Enum puro de dominio que define los tipos válidos de movimiento de stock.
 * No depende de ninguna tecnología externa.
 *
 * ENTRADA : ingreso de mercadería por compra a proveedor (RF03).
 * SALIDA  : descuento de stock al concretar una venta (RF04).
 * AJUSTE  : corrección manual del stock físico con motivo obligatorio (CU-08).
 */
public enum TipoMovimiento {
    ENTRADA,
    SALIDA,
    AJUSTE
}
