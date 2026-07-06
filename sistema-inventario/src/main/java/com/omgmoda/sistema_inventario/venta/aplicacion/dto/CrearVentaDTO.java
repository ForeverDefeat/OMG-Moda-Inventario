package com.omgmoda.sistema_inventario.venta.aplicacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Solicitud para registrar una venta completa.")
public record CrearVentaDTO(

        @NotBlank(message = "El metodo de pago es obligatorio.")
        @Size(max = 20, message = "El metodo de pago no puede superar 20 caracteres.")
        @Pattern(regexp = "EFECTIVO|TARJETA|YAPE|PLIN", message = "El metodo de pago debe ser EFECTIVO, TARJETA, YAPE o PLIN.")
        @Schema(description = "Metodo de pago usado en la venta.", example = "EFECTIVO")
        String metodoPago,

        @NotEmpty(message = "La venta debe contener al menos un item.")
        @Valid
        @Schema(description = "Items vendidos.")
        List<ItemVentaDTO> items

) {
    @Schema(description = "Linea de detalle de una venta.")
    public record ItemVentaDTO(

            @NotNull(message = "El id de la variante es obligatorio.")
            @Schema(description = "Identificador de la variante vendida.", example = "1")
            Long idVariante,

            @NotNull(message = "La cantidad es obligatoria.")
            @Min(value = 1, message = "La cantidad debe ser mayor a cero.")
            @Schema(description = "Cantidad vendida.", example = "2")
            Integer cantidad,

            @NotNull(message = "El precio unitario es obligatorio.")
            @DecimalMin(value = "0.0", inclusive = false,
                    message = "El precio unitario debe ser mayor a cero.")
            @Digits(integer = 8, fraction = 2)
            @Schema(description = "Precio unitario aplicado.", example = "89.90")
            BigDecimal precioUnitario

    ) {}
}
