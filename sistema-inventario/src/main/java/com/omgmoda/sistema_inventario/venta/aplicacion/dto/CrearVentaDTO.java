package com.omgmoda.sistema_inventario.venta.aplicacion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Record de entrada para registrar una venta completa con sus líneas de detalle.
 * Usado en el cuerpo del POST /api/v1/ventas.
 *
 * Nota: idUsuario NO viene en este DTO — se extrae del token JWT
 * en el controlador para evitar suplantación de identidad (mejora de seguridad).
 */
public record CrearVentaDTO(

        @NotBlank(message = "El método de pago es obligatorio.")
        String metodoPago,

        @NotEmpty(message = "La venta debe contener al menos un ítem.")
        @Valid
        List<ItemVentaDTO> items

) {
    /**
     * Record anidado que representa cada línea de la venta.
     */
    public record ItemVentaDTO(

            @NotNull(message = "El id de la variante es obligatorio.")
            Long idVariante,

            @NotNull(message = "La cantidad es obligatoria.")
            @Min(value = 1, message = "La cantidad debe ser mayor a cero.")
            Integer cantidad,

            @NotNull(message = "El precio unitario es obligatorio.")
            @DecimalMin(value = "0.0", inclusive = false,
                    message = "El precio unitario debe ser mayor a cero.")
            @Digits(integer = 8, fraction = 2)
            BigDecimal precioUnitario

    ) {}
}
