package com.omgmoda.sistema_inventario.venta.aplicacion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record WebhookStubPagoDTO(
        @NotBlank String providerEventId,
        @NotBlank String providerReference,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
        @NotBlank @Pattern(regexp = "PEN") String currency
) {}
