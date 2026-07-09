package com.omgmoda.sistema_inventario.venta.infraestructura.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "PAYMENT_WEBHOOK_EVENT", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_provider_event", columnNames = "provider_event_id")
})
/**
 * Entidad JPA que conserva en base de datos el estado asociado a PaymentWebhookEventJpaEntity.
 */
public class PaymentWebhookEventJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_event")
    private Long id;

    @Column(name = "provider", nullable = false, length = 30)
    private String provider;

    @Column(name = "provider_event_id", nullable = false, length = 120)
    private String providerEventId;

    @Column(name = "payload_hash", nullable = false, length = 128)
    private String payloadHash;

    @Lob
    @Column(name = "payload")
    private String payload;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getProviderEventId() { return providerEventId; }
    public void setProviderEventId(String providerEventId) { this.providerEventId = providerEventId; }
    public String getPayloadHash() { return payloadHash; }
    public void setPayloadHash(String payloadHash) { this.payloadHash = payloadHash; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
