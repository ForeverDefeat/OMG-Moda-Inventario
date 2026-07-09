package com.omgmoda.sistema_inventario.venta.infraestructura.adapters;

import com.omgmoda.sistema_inventario.venta.infraestructura.entities.PaymentAuditJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para consultas y persistencia de PaymentAuditJpaRepository.
 */
public interface PaymentAuditJpaRepository extends JpaRepository<PaymentAuditJpaEntity, Long> {
    List<PaymentAuditJpaEntity> findByIdPaymentOrderByCreatedAtAsc(Long idPayment);
}
