package com.omgmoda.sistema_inventario.producto.aplicacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Solicitud para crear un producto con una o mas variantes.")
public record CrearProductoDTO(

        @NotBlank(message = "El nombre del producto es obligatorio.")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
        @Pattern(regexp = "^[\\p{L}\\p{N} .,'\\-/]+$", message = "El nombre contiene caracteres no permitidos.")
        @Schema(description = "Nombre comercial del producto.", example = "Camisa Oxford")
        String nombre,

        @NotBlank(message = "La categoria es obligatoria.")
        @Size(max = 50, message = "La categoria no puede superar 50 caracteres.")
        @Pattern(regexp = "^[\\p{L}\\p{N} .,'\\-/]+$", message = "La categoria contiene caracteres no permitidos.")
        @Schema(description = "Categoria del producto.", example = "Camisas")
        String categoria,

        @NotBlank(message = "La marca es obligatoria.")
        @Size(max = 50, message = "La marca no puede superar 50 caracteres.")
        @Pattern(regexp = "^[\\p{L}\\p{N} .,'\\-/]+$", message = "La marca contiene caracteres no permitidos.")
        @Schema(description = "Marca del producto.", example = "OMG MODA")
        String marca,

        @Size(max = 500, message = "La URL de imagen no puede superar 500 caracteres.")
        @Schema(description = "Direccion publica de la imagen del producto.", example = "/uploads/productos/camisa-oxford.webp")
        String imageUrl,

        @NotEmpty(message = "Debe incluir al menos una variante.")
        @Valid
        @Schema(description = "Variantes iniciales del producto.")
        List<VarianteDTO> variantes

) {
    @Schema(description = "Variante o SKU del producto.")
    public record VarianteDTO(

            @Size(max = 40)
            @Pattern(regexp = "^[A-Za-z0-9 -]*$", message = "El SKU solo puede contener letras, numeros, espacios y guiones.")
            @Schema(description = "SKU opcional. Si se omite, el backend genera uno.", example = "CW-CAMISA-OXFORD-M-AZUL-01")
            String sku,

            @NotBlank(message = "La talla es obligatoria.")
            @Size(max = 20)
            @Pattern(regexp = "^[\\p{L}\\p{N} .,'\\-/]+$", message = "La talla contiene caracteres no permitidos.")
            @Schema(description = "Talla de la variante.", example = "M")
            String talla,

            @NotBlank(message = "El color es obligatorio.")
            @Size(max = 30)
            @Pattern(regexp = "^[\\p{L}\\p{N} .,'\\-/]+$", message = "El color contiene caracteres no permitidos.")
            @Schema(description = "Color de la variante.", example = "Azul")
            String color,

            @Size(max = 50)
            @Pattern(regexp = "^[\\p{L}\\p{N} .,'\\-/]*$", message = "El material contiene caracteres no permitidos.")
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
    ) {
        public VarianteDTO(String talla,
                           String color,
                           String material,
                           BigDecimal precioCosto,
                           BigDecimal precioVenta) {
            this(null, talla, color, material, precioCosto, precioVenta);
        }
    }
}
