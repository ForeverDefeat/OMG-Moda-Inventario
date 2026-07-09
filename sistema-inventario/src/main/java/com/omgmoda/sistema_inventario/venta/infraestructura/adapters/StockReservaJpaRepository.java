package com.omgmoda.sistema_inventario.venta.infraestructura.adapters;

import com.omgmoda.sistema_inventario.venta.dominio.EstadoReservaStock;
import com.omgmoda.sistema_inventario.venta.infraestructura.entities.StockReservaJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio Spring Data JPA para consultas y persistencia de StockReservaJpaRepository.
 */
public interface StockReservaJpaRepository extends JpaRepository<StockReservaJpaEntity, Long> {
    List<StockReservaJpaEntity> findByIdVenta(Long idVenta);
    List<StockReservaJpaEntity> findByIdVentaAndEstado(Long idVenta, EstadoReservaStock estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM StockReservaJpaEntity r WHERE r.idVenta = :idVenta AND r.estado = :estado")
    List<StockReservaJpaEntity> findByIdVentaAndEstadoForUpdate(@Param("idVenta") Long idVenta,
                                                                 @Param("estado") EstadoReservaStock estado);
}
