package com.omgmoda.sistema_inventario.dashboard.aplicacion.dto;

import java.math.BigDecimal;

/**
 * DTO que define la estructura de datos intercambiada por la capa de aplicacion/API.
 */
public record DashboardKpisDTO(
        BigDecimal valorStock,
        int skusActivos,
        int alertasStock,
        BigDecimal ventasPeriodo,
        int comprasSugeridas
) {}
