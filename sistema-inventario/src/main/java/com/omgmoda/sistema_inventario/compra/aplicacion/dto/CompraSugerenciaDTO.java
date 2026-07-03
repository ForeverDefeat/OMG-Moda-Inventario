package com.omgmoda.sistema_inventario.compra.aplicacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Sugerencia de reposicion calculada desde el inventario.")
public record CompraSugerenciaDTO(
        @Schema(description = "Identificador estable de la sugerencia.", example = "1")
        Long id,
        @Schema(description = "Producto y variante sugerida.", example = "Camisa Oxford Azul M")
        String producto,
        @Schema(description = "Proveedor sugerido o pendiente.", example = "Proveedor pendiente")
        String proveedor,
        @Schema(description = "Cantidad recomendada para reponer.", example = "24")
        int cantidadSugerida,
        @Schema(description = "Costo estimado de reposicion.", example = "1080.00")
        BigDecimal costoEstimado,
        @Schema(description = "Prioridad de reposicion.", example = "Alta")
        String prioridad,
        @Schema(description = "Motivo calculado.", example = "Stock bajo")
        String motivo
) {}
