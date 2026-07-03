package com.omgmoda.sistema_inventario.reportes.aplicacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Resumen ejecutivo para reportes.")
public record ReporteResumenDTO(
        BigDecimal ventasMes,
        BigDecimal crecimientoPorcentaje,
        int skusConAlerta,
        int reportesActivos
) {}
