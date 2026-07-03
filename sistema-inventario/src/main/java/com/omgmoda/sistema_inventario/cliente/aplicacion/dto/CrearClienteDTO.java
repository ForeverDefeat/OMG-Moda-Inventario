package com.omgmoda.sistema_inventario.cliente.aplicacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Solicitud para registrar un cliente.")
public record CrearClienteDTO(
        @NotBlank(message = "El nombre del cliente es obligatorio.")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
        String nombre,

        @NotBlank(message = "El correo del cliente es obligatorio.")
        @Email(message = "El correo del cliente no tiene un formato valido.")
        @Size(max = 100, message = "El correo no puede superar 100 caracteres.")
        String correo,

        @NotBlank(message = "El telefono del cliente es obligatorio.")
        @Size(max = 20, message = "El telefono no puede superar 20 caracteres.")
        String telefono,

        @NotBlank(message = "El segmento del cliente es obligatorio.")
        @Pattern(regexp = "VIP|Frecuente|Nuevo", message = "El segmento debe ser VIP, Frecuente o Nuevo.")
        String segmento,

        @DecimalMin(value = "0.0", message = "El total de compras no puede ser negativo.")
        @Digits(integer = 10, fraction = 2)
        BigDecimal totalCompras,

        LocalDate ultimaCompra
) {}
