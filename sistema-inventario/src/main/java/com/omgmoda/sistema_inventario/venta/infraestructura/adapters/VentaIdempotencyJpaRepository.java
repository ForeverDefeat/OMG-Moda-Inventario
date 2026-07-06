package com.omgmoda.sistema_inventario.venta.infraestructura.adapters;

import com.omgmoda.sistema_inventario.venta.infraestructura.entities.VentaIdempotencyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaIdempotencyJpaRepository extends JpaRepository<VentaIdempotencyJpaEntity, String> {
}
