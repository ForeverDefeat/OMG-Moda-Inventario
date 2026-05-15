package com.omgmoda.sistema_inventario.producto.aplicacion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Record de entrada para registrar un producto con sus variantes iniciales.
 * Usado en el cuerpo del POST /api/v1/productos.
 */
public record CrearProductoDTO(

        @NotBlank(message = "El nombre del producto es obligatorio.")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
        String nombre,

        @NotBlank(message = "La categoría es obligatoria.")
        @Size(max = 50, message = "La categoría no puede superar 50 caracteres.")
        String categoria,

        @NotBlank(message = "La marca es obligatoria.")
        @Size(max = 50, message = "La marca no puede superar 50 caracteres.")
        String marca,

        @NotEmpty(message = "Debe incluir al menos una variante.")
        @Valid
        List<VarianteDTO> variantes

) {
    /**
     * Record anidado que representa cada variante a crear junto con el producto.
     */
    public record VarianteDTO(

            @NotBlank(message = "La talla es obligatoria.")
            @Size(max = 20)
            String talla,

            @NotBlank(message = "El color es obligatorio.")
            @Size(max = 30)
            String color,

            @Size(max = 50)
            String material,

            @NotNull(message = "El precio de costo es obligatorio.")
            @DecimalMin(value = "0.0", inclusive = false,
                    message = "El precio de costo debe ser mayor a cero.")
            @Digits(integer = 8, fraction = 2)
            BigDecimal precioCosto,

            @NotNull(message = "El precio de venta es obligatorio.")
            @DecimalMin(value = "0.0", inclusive = false,
                    message = "El precio de venta debe ser mayor a cero.")
            @Digits(integer = 8, fraction = 2)
            BigDecimal precioVenta
    ) {}
}
