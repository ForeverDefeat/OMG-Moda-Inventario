package com.omgmoda.sistema_inventario.venta.aplicacion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO que define la estructura de datos intercambiada por la capa de aplicacion/API.
 */
public record ConfirmarEfectivoDTO(
        @DecimalMin(value = "0.0", inclusive = false, message = "El monto recibido debe ser mayor a cero.")
        @Digits(integer = 10, fraction = 2)
        BigDecimal amountReceived,
        @Size(max = 120)
        String reference,
        @Size(max = 500)
        String observation
) {}
