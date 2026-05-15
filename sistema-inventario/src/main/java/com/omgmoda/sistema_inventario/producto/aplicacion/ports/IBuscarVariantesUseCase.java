package com.omgmoda.sistema_inventario.producto.aplicacion.ports;

import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;

import java.util.List;

/**
 * Input Port — contrato de consulta y búsqueda de variantes con filtros (CU-05).
 * Soporta filtrado por talla, color y categoría (parámetros opcionales).
 */
public interface IBuscarVariantesUseCase {

    /**
     * Retorna variantes aplicando los filtros recibidos.
     * Cualquier parámetro puede ser null para omitir ese filtro.
     *
     * @param talla     filtro por talla (ej. "M", "XL") — opcional.
     * @param color     filtro por color (ej. "Negro") — opcional.
     * @param categoria filtro por categoría de producto — opcional.
     * @return lista de variantes que cumplen los criterios.
     */
    List<VarianteResponseDTO> buscar(String talla, String color, String categoria);

    /**
     * Retorna todas las variantes con stock en o por debajo del mínimo.
     * Usado para el módulo de alertas de stock crítico (RF09).
     */
    List<VarianteResponseDTO> buscarBajoStock();
}
