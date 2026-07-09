package com.omgmoda.sistema_inventario.venta.infraestructura.adapters;

import com.omgmoda.sistema_inventario.venta.infraestructura.entities.PaymentIntentJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoPago;
import com.omgmoda.sistema_inventario.venta.dominio.MetodoPago;

/**
 * Repositorio Spring Data JPA para consultas y persistencia de PaymentIntentJpaRepository.
 */
public interface PaymentIntentJpaRepository extends JpaRepository<PaymentIntentJpaEntity, Long> {
    Optional<PaymentIntentJpaEntity> findByIdVenta(Long idVenta);
    Optional<PaymentIntentJpaEntity> findByProviderReference(String providerReference);

    @Query("""
            SELECT p FROM PaymentIntentJpaEntity p
            JOIN VentaJpaEntity v ON v.id = p.idVenta
            WHERE (:status IS NULL OR p.status = :status)
              AND (:method IS NULL OR p.method = :method)
              AND (:desde IS NULL OR p.createdAt >= :desde)
              AND (:hasta IS NULL OR p.createdAt <= :hasta)
              AND (:idUsuario IS NULL OR v.idUsuario = :idUsuario)
            ORDER BY p.createdAt DESC
            """)
    List<PaymentIntentJpaEntity> findForBandeja(
            @Param("status") EstadoPago status,
            @Param("method") MetodoPago method,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("idUsuario") Long idUsuario
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentIntentJpaEntity p WHERE p.id = :id")
    Optional<PaymentIntentJpaEntity> findByIdForUpdate(@Param("id") Long id);
}
