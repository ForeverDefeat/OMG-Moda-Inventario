package com.omgmoda.sistema_inventario.venta.infraestructura.entities;

import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que mapea la tabla VENTA (3FN).
 * No contiene el campo total — cumple 3FN (dato derivado).
 * La relación con DetalleVentaJpaEntity usa CascadeType.ALL:
 * al persistir la venta se persisten automáticamente sus detalles.
 */
@Entity
@Table(name = "VENTA", uniqueConstraints = {
        @UniqueConstraint(name = "uk_venta_idempotency_key", columnNames = "idempotency_key")
})
public class VentaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Long id;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 24)
    private EstadoVenta estado;

    @Column(name = "metodo_pago", nullable = false, length = 20)
    private String metodoPago;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "idempotency_key", length = 120)
    private String idempotencyKey;

    @Column(name = "idempotency_payload_hash", length = 128)
    private String idempotencyPayloadHash;

    @OneToMany(mappedBy = "venta",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    private List<DetalleVentaJpaEntity> detalles = new ArrayList<>();

    // Constructor vacío requerido por JPA
    public VentaJpaEntity() {}

    // ── Getters y Setters ──────────────────────────────────────────────────────

    public Long getId()                                  { return id; }
    public void setId(Long id)                           { this.id = id; }
    public Long getIdUsuario()                           { return idUsuario; }
    public void setIdUsuario(Long idUsuario)             { this.idUsuario = idUsuario; }
    public EstadoVenta getEstado()                       { return estado; }
    public void setEstado(EstadoVenta estado)            { this.estado = estado; }
    public String getMetodoPago()                        { return metodoPago; }
    public void setMetodoPago(String metodoPago)         { this.metodoPago = metodoPago; }
    public LocalDateTime getFecha()                      { return fecha; }
    public void setFecha(LocalDateTime fecha)            { this.fecha = fecha; }
    public LocalDateTime getCompletedAt()                { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getCancelledAt()                { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public LocalDateTime getExpiredAt()                  { return expiredAt; }
    public void setExpiredAt(LocalDateTime expiredAt)    { this.expiredAt = expiredAt; }
    public String getIdempotencyKey()                    { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getIdempotencyPayloadHash()            { return idempotencyPayloadHash; }
    public void setIdempotencyPayloadHash(String idempotencyPayloadHash) { this.idempotencyPayloadHash = idempotencyPayloadHash; }
    public List<DetalleVentaJpaEntity> getDetalles()     { return detalles; }
    public void setDetalles(List<DetalleVentaJpaEntity> d) { this.detalles = d; }
}
