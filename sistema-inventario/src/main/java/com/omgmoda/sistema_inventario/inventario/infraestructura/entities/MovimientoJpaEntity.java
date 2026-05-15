package com.omgmoda.sistema_inventario.inventario.infraestructura.entities;

import com.omgmoda.sistema_inventario.inventario.dominio.TipoMovimiento;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA que mapea la tabla MOVIMIENTO (3FN).
 * Completamente separada de la entidad de dominio Movimiento.
 * Solo la capa de infraestructura conoce esta clase.
 */
@Entity
@Table(name = "MOVIMIENTO")
public class MovimientoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long id;

    @Column(name = "id_variante", nullable = false)
    private Long idVariante;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 10)
    private TipoMovimiento tipo;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @Column(name = "motivo", length = 150)
    private String motivo;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    // Constructor vacío requerido por JPA
    public MovimientoJpaEntity() {}

    // ── Getters y Setters ──────────────────────────────────────────────────────

    public Long getId()                         { return id; }
    public void setId(Long id)                  { this.id = id; }
    public Long getIdVariante()                 { return idVariante; }
    public void setIdVariante(Long idVariante)  { this.idVariante = idVariante; }
    public Long getIdUsuario()                  { return idUsuario; }
    public void setIdUsuario(Long idUsuario)    { this.idUsuario = idUsuario; }
    public TipoMovimiento getTipo()             { return tipo; }
    public void setTipo(TipoMovimiento tipo)    { this.tipo = tipo; }
    public int getCantidad()                    { return cantidad; }
    public void setCantidad(int cantidad)       { this.cantidad = cantidad; }
    public String getMotivo()                   { return motivo; }
    public void setMotivo(String motivo)        { this.motivo = motivo; }
    public LocalDateTime getFecha()             { return fecha; }
    public void setFecha(LocalDateTime fecha)   { this.fecha = fecha; }
}
