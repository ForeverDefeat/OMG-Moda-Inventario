package com.omgmoda.sistema_inventario.inventario.aplicacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitud para registrar entrada de stock.")
public record RegistrarEntradaDTO(

        @NotNull(message = "El id de la variante es obligatorio.")
        @Schema(description = "Identificador de la variante que recibe stock.", example = "1")
        Long idVariante,

        @NotNull(message = "La cantidad es obligatoria.")
        @Min(value = 1, message = "La cantidad de entrada debe ser mayor a cero.")
        @Schema(description = "Cantidad de unidades a incrementar.", example = "10")
        Integer cantidad,

        @Size(max = 150, message = "El motivo no puede superar 150 caracteres.")
        @Schema(description = "Motivo u observacion de la entrada.", example = "Reposicion de mercaderia")
        String motivo

) {}
