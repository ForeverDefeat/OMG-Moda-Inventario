package com.omgmoda.sistema_inventario.producto.dominio.ports;

import com.omgmoda.sistema_inventario.producto.dominio.VarianteProducto;

import java.util.List;
import java.util.Optional;

/**
 * Output Port — contrato puro de persistencia para VarianteProducto.
 * El dominio define este contrato; la infraestructura lo implementa.
 * No importa ninguna clase de Spring ni JPA.
 */
public interface IVarianteRepository {

    /** Persiste una variante nueva o actualiza una existente. */
    VarianteProducto save(VarianteProducto variante);

    /** Busca una variante por su identificador. */
    Optional<VarianteProducto> findById(Long id);

    /** Retorna todas las variantes de un producto por su id. */
    List<VarianteProducto> findByProductoId(Long idProducto);

    /** Actualiza los datos visuales del producto y retorna sus variantes renovadas. */
    List<VarianteProducto> updateProductoVisuals(Long idProducto, String nombre, String imageUrl);

    /** Retorna variantes filtrando por talla y/o color (null = sin filtro). */
    List<VarianteProducto> findByFiltros(String talla, String color, String categoria, String sku);

    /** Indica si ya existe una variante con el SKU normalizado. */
    boolean existsBySku(String sku);

    /** Retorna todas las variantes cuyo stock actual <= stock mínimo. */
    List<VarianteProducto> findBajoStock();

    /** Elimina una variante por su id. */
    void deleteById(Long id);
}
