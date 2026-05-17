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
@Table(name = "VENTA")
public class VentaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Long id;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private EstadoVenta estado;

    @Column(name = "metodo_pago", nullable = false, length = 20)
    private String metodoPago;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

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
    public List<DetalleVentaJpaEntity> getDetalles()     { return detalles; }
    public void setDetalles(List<DetalleVentaJpaEntity> d) { this.detalles = d; }
}
