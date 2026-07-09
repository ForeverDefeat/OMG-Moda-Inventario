package com.omgmoda.sistema_inventario.producto.infraestructura.adapters;

import com.omgmoda.sistema_inventario.producto.infraestructura.entities.ProductoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA para consultas y persistencia de ProductoJpaRepository.
 */
public interface ProductoJpaRepository extends JpaRepository<ProductoJpaEntity, Long> {
}
