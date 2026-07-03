package com.omgmoda.sistema_inventario.producto.infraestructura.adapters;

import com.omgmoda.sistema_inventario.producto.infraestructura.entities.ProductoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoJpaRepository extends JpaRepository<ProductoJpaEntity, Long> {
}
