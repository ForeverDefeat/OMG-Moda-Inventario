package com.omgmoda.sistema_inventario.producto.aplicacion.dto;

import com.omgmoda.sistema_inventario.shared.dominio.valueobjects.StockStatus;

import java.math.BigDecimal;

/**
 * Record de salida para listados, búsquedas y detalle de variante (CU-05).
 * Proyecta los datos necesarios para el frontend sin exponer la entidad de dominio.
 */
public record VarianteResponseDTO(

        Long idVariante,
        Long idProducto,
        String nombreProducto,
        String categoria,
        String marca,
        String talla,
        String color,
        String material,
        BigDecimal precioCosto,
        BigDecimal precioVenta,
        int stockActual,
        int stockMinimo,
        StockStatus stockStatus

) {}
