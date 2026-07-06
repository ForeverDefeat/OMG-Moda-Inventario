package com.omgmoda.sistema_inventario.venta.infraestructura.adapters;

import com.omgmoda.sistema_inventario.venta.infraestructura.entities.PaymentWebhookEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentWebhookEventJpaRepository extends JpaRepository<PaymentWebhookEventJpaEntity, Long> {
    Optional<PaymentWebhookEventJpaEntity> findByProviderEventId(String providerEventId);
}
