package com.omgmoda.sistema_inventario.dashboard.aplicacion.dto;

import java.math.BigDecimal;

public record DashboardKpisDTO(
        BigDecimal valorStock,
        int skusActivos,
        int alertasStock,
        BigDecimal ventasPeriodo,
        int comprasSugeridas
) {}
