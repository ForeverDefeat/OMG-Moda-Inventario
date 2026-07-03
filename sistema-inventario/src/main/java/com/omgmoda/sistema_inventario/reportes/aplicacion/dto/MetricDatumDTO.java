package com.omgmoda.sistema_inventario.reportes.aplicacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Punto de datos para graficos.")
public record MetricDatumDTO(
        @Schema(description = "Etiqueta del punto.", example = "Lun")
        String name,
        @Schema(description = "Valor numerico del punto.", example = "179.80")
        BigDecimal value
) {}
