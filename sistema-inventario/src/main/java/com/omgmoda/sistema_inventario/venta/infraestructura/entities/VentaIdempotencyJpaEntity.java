package com.omgmoda.sistema_inventario.venta.infraestructura.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidad JPA que conserva en base de datos el estado asociado a VentaIdempotencyJpaEntity.
 */
@Entity
@Table(name = "VENTA_IDEMPOTENCY")
public class VentaIdempotencyJpaEntity {
    @Id
    @Column(name = "idempotency_key", length = 120)
    private String idempotencyKey;

    @Column(name = "payload_hash", nullable = false, length = 128)
    private String payloadHash;

    @Column(name = "id_venta", nullable = false)
    private Long idVenta;

    @Column(name = "id_payment", nullable = false)
    private Long idPayment;

    @Lob
    @Column(name = "response_payload")
    private String responsePayload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getPayloadHash() { return payloadHash; }
    public void setPayloadHash(String payloadHash) { this.payloadHash = payloadHash; }
    public Long getIdVenta() { return idVenta; }
    public void setIdVenta(Long idVenta) { this.idVenta = idVenta; }
    public Long getIdPayment() { return idPayment; }
    public void setIdPayment(Long idPayment) { this.idPayment = idPayment; }
    public String getResponsePayload() { return responsePayload; }
    public void setResponsePayload(String responsePayload) { this.responsePayload = responsePayload; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
