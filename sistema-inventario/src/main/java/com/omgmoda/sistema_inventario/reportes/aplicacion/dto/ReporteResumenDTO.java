package com.omgmoda.sistema_inventario.reportes.aplicacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * DTO que define la estructura de datos intercambiada por la capa de aplicacion/API.
 */
@Schema(description = "Resumen ejecutivo para reportes.")
public record ReporteResumenDTO(
        BigDecimal ventasMes,
        BigDecimal crecimientoPorcentaje,
        int skusConAlerta,
        int reportesActivos,
        int unidadesVendidas,
        BigDecimal ticketPromedio,
        String categoriaPrincipal,
        String productoMasVendido
) {}
