package com.omgmoda.sistema_inventario.inventario.aplicacion.dto;

import com.omgmoda.sistema_inventario.inventario.dominio.TipoMovimiento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Movimiento de inventario registrado.")
public record MovimientoResponseDTO(

        @Schema(description = "Identificador del movimiento.", example = "1")
        Long idMovimiento,

        @Schema(description = "Identificador de la variante afectada.", example = "1")
        Long idVariante,

        @Schema(description = "Identificador del usuario autenticado que registro el movimiento.", example = "1")
        Long idUsuario,

        @Schema(description = "Tipo de movimiento.", example = "ENTRADA")
        TipoMovimiento tipo,

        @Schema(description = "Cantidad registrada en el movimiento.", example = "10")
        int cantidad,

        @Schema(description = "Motivo u observacion del movimiento.", example = "Reposicion")
        String motivo,

        @Schema(description = "Fecha de registro del movimiento.")
        LocalDateTime fecha,

        @Schema(description = "Stock resultante de la variante.", example = "15")
        int stockResultante

) {}
