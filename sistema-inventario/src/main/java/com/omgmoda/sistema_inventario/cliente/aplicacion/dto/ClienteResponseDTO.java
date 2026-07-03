package com.omgmoda.sistema_inventario.cliente.aplicacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Datos de cliente para gestion comercial.")
public record ClienteResponseDTO(
        @Schema(description = "Identificador del cliente.", example = "1")
        Long id,
        @Schema(description = "Nombre completo.", example = "Mariela Torres")
        String nombre,
        @Schema(description = "Correo electronico.", example = "mariela@email.com")
        String correo,
        @Schema(description = "Telefono de contacto.", example = "999120554")
        String telefono,
        @Schema(description = "Segmento comercial.", example = "VIP")
        String segmento,
        @Schema(description = "Total historico comprado.", example = "1860.00")
        BigDecimal totalCompras,
        @Schema(description = "Fecha de ultima compra.")
        LocalDate ultimaCompra
) {}
