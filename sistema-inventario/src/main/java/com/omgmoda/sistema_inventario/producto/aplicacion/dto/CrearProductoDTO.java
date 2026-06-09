package com.omgmoda.sistema_inventario.producto.aplicacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Solicitud para crear un producto con una o mas variantes.")
public record CrearProductoDTO(

        @NotBlank(message = "El nombre del producto es obligatorio.")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
        @Schema(description = "Nombre comercial del producto.", example = "Camisa Oxford")
        String nombre,

        @NotBlank(message = "La categoria es obligatoria.")
        @Size(max = 50, message = "La categoria no puede superar 50 caracteres.")
        @Schema(description = "Categoria del producto.", example = "Camisas")
        String categoria,

        @NotBlank(message = "La marca es obligatoria.")
        @Size(max = 50, message = "La marca no puede superar 50 caracteres.")
        @Schema(description = "Marca del producto.", example = "OMG MODA")
        String marca,

        @NotEmpty(message = "Debe incluir al menos una variante.")
        @Valid
        @Schema(description = "Variantes iniciales del producto.")
        List<VarianteDTO> variantes

) {
    @Schema(description = "Variante o SKU del producto.")
    public record VarianteDTO(

            @NotBlank(message = "La talla es obligatoria.")
            @Size(max = 20)
            @Schema(description = "Talla de la variante.", example = "M")
            String talla,

            @NotBlank(message = "El color es obligatorio.")
            @Size(max = 30)
            @Schema(description = "Color de la variante.", example = "Azul")
            String color,

            @Size(max = 50)
            @Schema(description = "Material de la prenda.", example = "Algodon")
            String material,

            @NotNull(message = "El precio de costo es obligatorio.")
            @DecimalMin(value = "0.0", inclusive = false,
                    message = "El precio de costo debe ser mayor a cero.")
            @Digits(integer = 8, fraction = 2)
            @Schema(description = "Precio de costo unitario.", example = "45.00")
            BigDecimal precioCosto,

            @NotNull(message = "El precio de venta es obligatorio.")
            @DecimalMin(value = "0.0", inclusive = false,
                    message = "El precio de venta debe ser mayor a cero.")
            @Digits(integer = 8, fraction = 2)
            @Schema(description = "Precio de venta unitario.", example = "89.90")
            BigDecimal precioVenta
    ) {}
}
