package com.omgmoda.sistema_inventario.inventario.aplicacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitud para ajustar manualmente el stock.")
public record RegistrarAjusteDTO(

        @NotNull(message = "El id de la variante es obligatorio.")
        @Schema(description = "Identificador de la variante a ajustar.", example = "1")
        Long idVariante,

        @NotNull(message = "La cantidad es obligatoria.")
        @Min(value = 0, message = "La cantidad no puede ser negativa.")
        @Schema(description = "Nuevo stock absoluto de la variante.", example = "4")
        Integer cantidad,

        @NotBlank(message = "El motivo del ajuste es obligatorio.")
        @Size(max = 150, message = "El motivo no puede superar 150 caracteres.")
        @Schema(description = "Motivo obligatorio del ajuste.", example = "Correccion por conteo fisico")
        String motivo

) {}
