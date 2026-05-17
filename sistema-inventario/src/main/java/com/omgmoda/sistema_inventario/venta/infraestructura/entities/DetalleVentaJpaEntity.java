package com.omgmoda.sistema_inventario.venta.infraestructura.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entidad JPA que mapea la tabla DETALLE_VENTA (3FN).
 * No contiene el campo subtotal — cumple 3FN (dato derivado).
 * Vinculada a VentaJpaEntity mediante @ManyToOne.
 */
@Entity
@Table(name = "DETALLE_VENTA")
public class DetalleVentaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", nullable = false)
    private VentaJpaEntity venta;

    @Column(name = "id_variante", nullable = false)
    private Long idVariante;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    // Constructor vacío requerido por JPA
    public DetalleVentaJpaEntity() {}

    // ── Getters y Setters ──────────────────────────────────────────────────────

    public Long getId()                              { return id; }
    public void setId(Long id)                       { this.id = id; }
    public VentaJpaEntity getVenta()                 { return venta; }
    public void setVenta(VentaJpaEntity venta)       { this.venta = venta; }
    public Long getIdVariante()                      { return idVariante; }
    public void setIdVariante(Long idVariante)       { this.idVariante = idVariante; }
    public int getCantidad()                         { return cantidad; }
    public void setCantidad(int cantidad)            { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario()            { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal p)      { this.precioUnitario = p; }
}
