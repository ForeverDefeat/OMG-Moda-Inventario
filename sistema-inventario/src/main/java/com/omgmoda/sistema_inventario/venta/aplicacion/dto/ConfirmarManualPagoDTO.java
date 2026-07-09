package com.omgmoda.sistema_inventario.venta.aplicacion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO que define la estructura de datos intercambiada por la capa de aplicacion/API.
 */
public record ConfirmarManualPagoDTO(
        @DecimalMin(value = "0.0", inclusive = false, message = "El monto recibido debe ser mayor a cero.")
        @Digits(integer = 10, fraction = 2)
        BigDecimal amountReceived,
        @NotBlank(message = "La moneda es obligatoria.")
        @Pattern(regexp = "PEN", message = "La moneda debe ser PEN.")
        String currency,
        @NotBlank(message = "La referencia es obligatoria.")
        @Size(max = 120)
        String reference,
        @NotBlank(message = "La observacion es obligatoria.")
        @Size(max = 500)
        String observation
) {}
