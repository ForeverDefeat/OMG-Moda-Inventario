package com.omgmoda.sistema_inventario.producto.aplicacion.dto;

import com.omgmoda.sistema_inventario.shared.dominio.valueobjects.StockStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Datos de una variante de producto.")
public record VarianteResponseDTO(

        @Schema(description = "Identificador de la variante.", example = "1")
        Long idVariante,
        @Schema(description = "Identificador del producto.", example = "1")
        Long idProducto,
        @Schema(description = "Nombre del producto.", example = "Camisa Oxford")
        String nombreProducto,
        @Schema(description = "Categoria del producto.", example = "Camisas")
        String categoria,
        @Schema(description = "Marca del producto.", example = "OMG MODA")
        String marca,
        @Schema(description = "Talla de la variante.", example = "M")
        String talla,
        @Schema(description = "Color de la variante.", example = "Azul")
        String color,
        @Schema(description = "Material de la variante.", example = "Algodon")
        String material,
        @Schema(description = "Precio de costo.", example = "45.00")
        BigDecimal precioCosto,
        @Schema(description = "Precio de venta.", example = "89.90")
        BigDecimal precioVenta,
        @Schema(description = "Stock actual.", example = "15")
        int stockActual,
        @Schema(description = "Stock minimo configurado.", example = "5")
        int stockMinimo,
        @Schema(description = "Estado calculado del stock.", example = "NORMAL")
        StockStatus stockStatus

) {}
