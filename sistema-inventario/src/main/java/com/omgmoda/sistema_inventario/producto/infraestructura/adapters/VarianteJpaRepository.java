package com.omgmoda.sistema_inventario.producto.infraestructura.adapters;

import com.omgmoda.sistema_inventario.producto.infraestructura.entities.VarianteJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz Spring Data JPA interna del adaptador.
 * Solo visible dentro del paquete de infraestructura.
 * Nunca expuesta al dominio ni a la capa de aplicación.
 */
public interface VarianteJpaRepository extends JpaRepository<VarianteJpaEntity, Long> {

    @Override
    @EntityGraph(attributePaths = "producto")
    Optional<VarianteJpaEntity> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "producto")
    @Query("SELECT v FROM VarianteJpaEntity v WHERE v.id = :id")
    Optional<VarianteJpaEntity> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = "producto")
    List<VarianteJpaEntity> findByProductoId(Long idProducto);

    @EntityGraph(attributePaths = "producto")
    @Query("""
            SELECT v FROM VarianteJpaEntity v
            JOIN v.producto p
            WHERE (:talla IS NULL OR v.talla = :talla)
              AND (:color IS NULL OR v.color = :color)
              AND (:categoria IS NULL OR p.categoria = :categoria)
              AND (:sku IS NULL OR UPPER(v.sku) LIKE CONCAT('%', UPPER(:sku), '%'))
            """)
    List<VarianteJpaEntity> findByFiltros(
            @Param("talla") String talla,
            @Param("color") String color,
            @Param("categoria") String categoria,
            @Param("sku") String sku
    );

    boolean existsBySku(String sku);

    @EntityGraph(attributePaths = "producto")
    @Query("SELECT v FROM VarianteJpaEntity v WHERE (v.stockActual - v.stockReservado) <= v.stockMinimo")
    List<VarianteJpaEntity> findBajoStock();
}
