package com.omgmoda.sistema_inventario.producto.aplicacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitud para actualizar datos visuales de un producto.")
public record ActualizarProductoDTO(

        @NotBlank(message = "El nombre del producto es obligatorio.")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
        @Pattern(regexp = "^[\\p{L}\\p{N} .,'\\-/]+$", message = "El nombre contiene caracteres no permitidos.")
        @Schema(description = "Nombre comercial del producto.", example = "Camisa Oxford Premium")
        String nombre,

        @Size(max = 500, message = "La URL de imagen no puede superar 500 caracteres.")
        @Schema(description = "Direccion publica de la imagen del producto.", example = "/uploads/productos/camisa-oxford.webp")
        String imageUrl
) {
}
