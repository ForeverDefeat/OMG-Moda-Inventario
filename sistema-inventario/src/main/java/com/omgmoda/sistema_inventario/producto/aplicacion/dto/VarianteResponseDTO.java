package com.omgmoda.sistema_inventario.producto.aplicacion.dto;

import com.omgmoda.sistema_inventario.shared.dominio.valueobjects.StockStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * DTO que define la estructura de datos intercambiada por la capa de aplicacion/API.
 */
@Schema(description = "Datos de una variante de producto.")
public record VarianteResponseDTO(

        @Schema(description = "Identificador de la variante.", example = "1")
        Long idVariante,
        @Schema(description = "Identificador del producto.", example = "1")
        Long idProducto,
        @Schema(description = "SKU unico de la variante.", example = "CW-CAMISA-OXFORD-M-AZUL-01")
        String sku,
        @Schema(description = "Nombre del producto.", example = "Camisa Oxford")
        String nombreProducto,
        @Schema(description = "Categoria del producto.", example = "Camisas")
        String categoria,
        @Schema(description = "Marca del producto.", example = "OMG MODA")
        String marca,
        @Schema(description = "Direccion publica de la imagen del producto.", example = "/uploads/productos/camisa-oxford.webp")
        String imageUrl,
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
        @Schema(description = "Stock reservado en ventas pendientes.", example = "2")
        int stockReservado,
        @Schema(description = "Stock disponible para vender.", example = "13")
        int stockDisponible,
        @Schema(description = "Stock minimo configurado.", example = "5")
        int stockMinimo,
        @Schema(description = "Estado calculado del stock.", example = "NORMAL")
        StockStatus stockStatus

) {
        public VarianteResponseDTO(Long idVariante,
                                   Long idProducto,
                                   String sku,
                                   String nombreProducto,
                                   String categoria,
                                   String marca,
                                   String imageUrl,
                                   String talla,
                                   String color,
                                   String material,
                                   BigDecimal precioCosto,
                                   BigDecimal precioVenta,
                                   int stockActual,
                                   int stockMinimo,
                                   StockStatus stockStatus) {
                this(idVariante, idProducto, sku, nombreProducto, categoria, marca, imageUrl,
                        talla, color, material, precioCosto, precioVenta, stockActual, 0, stockActual, stockMinimo, stockStatus);
        }

        public VarianteResponseDTO(Long idVariante,
                                   Long idProducto,
                                   String nombreProducto,
                                   String categoria,
                                   String marca,
                                   String imageUrl,
                                   String talla,
                                   String color,
                                   String material,
                                   BigDecimal precioCosto,
                                   BigDecimal precioVenta,
                                   int stockActual,
                                   int stockMinimo,
                                   StockStatus stockStatus) {
                this(idVariante, idProducto, "SKU-" + idVariante, nombreProducto, categoria, marca, imageUrl,
                        talla, color, material, precioCosto, precioVenta, stockActual, 0, stockActual, stockMinimo, stockStatus);
        }
}
