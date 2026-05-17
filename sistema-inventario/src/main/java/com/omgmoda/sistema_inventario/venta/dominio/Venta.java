package com.omgmoda.sistema_inventario.venta.dominio;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root del módulo Venta.
 * Controla el ciclo de vida completo de una venta y garantiza que los
 * DetalleVenta solo se creen o modifiquen a través de este agregado.
 *
 * No persiste el campo total — cumple 3FN.
 * El total se calcula mediante calcularTotal() sumando los subtotales.
 *
 * Corresponde a las tablas VENTA y DETALLE_VENTA.
 */
public class Venta {

    private Long id;
    private Long idUsuario;
    private EstadoVenta estado;
    private String metodoPago;
    private LocalDateTime fecha;
    private final List<DetalleVenta> detalles = new ArrayList<>();

    // Constructor para creación nueva
    public Venta(Long idUsuario, String metodoPago) {
        validar(idUsuario, metodoPago);
        this.idUsuario = idUsuario;
        this.metodoPago = metodoPago;
        this.estado = EstadoVenta.PENDIENTE;
        this.fecha = LocalDateTime.now();
    }

    // Constructor para reconstitución desde persistencia
    public Venta(Long id,
                 Long idUsuario,
                 EstadoVenta estado,
                 String metodoPago,
                 LocalDateTime fecha,
                 List<DetalleVenta> detalles) {
        validar(idUsuario, metodoPago);
        this.id = id;
        this.idUsuario = idUsuario;
        this.estado = estado;
        this.metodoPago = metodoPago;
        this.fecha = fecha;
        this.detalles.addAll(detalles);
    }

    // ── Lógica de negocio ──────────────────────────────────────────────────────

    /**
     * Agrega una línea de detalle a la venta.
     * Solo permitido mientras la venta está en estado PENDIENTE.
     *
     * @param idVariante     identificador de la variante a vender.
     * @param cantidad       unidades vendidas.
     * @param precioUnitario precio en el momento de la venta (histórico).
     */
    public DetalleVenta agregarDetalle(Long idVariante,
                                       int cantidad,
                                       BigDecimal precioUnitario) {
        if (!EstadoVenta.PENDIENTE.equals(this.estado))
            throw new DomainException(
                    "No se pueden agregar detalles a una venta en estado: " + this.estado
            );

        DetalleVenta detalle = new DetalleVenta(idVariante, cantidad, precioUnitario);
        detalles.add(detalle);
        return detalle;
    }

    /**
     * Transiciona la venta a COMPLETADA.
     * Debe llamarse después de haber descontado el stock de cada variante
     * en el caso de uso RegistrarVentaUseCaseImpl.
     */
    public void completar() {
        if (!estado.puedeCompletar())
            throw new DomainException(
                    "No se puede completar una venta en estado: " + this.estado
            );
        if (detalles.isEmpty())
            throw new DomainException(
                    "No se puede completar una venta sin detalles."
            );
        this.estado = EstadoVenta.COMPLETADA;
    }

    /**
     * Transiciona la venta a ANULADA.
     * La reversión del stock es responsabilidad del caso de uso.
     */
    public void anular() {
        if (!estado.puedeAnular())
            throw new DomainException(
                    "No se puede anular una venta en estado: " + this.estado
            );
        this.estado = EstadoVenta.ANULADA;
    }

    /**
     * Calcula el total de la venta sumando los subtotales de todos los detalles.
     * Método calculado — no atributo persistido (3FN).
     */
    public BigDecimal calcularTotal() {
        return detalles.stream()
                .map(DetalleVenta::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ── Validación interna ─────────────────────────────────────────────────────

    private void validar(Long idUsuario, String metodoPago) {
        if (idUsuario == null)
            throw new DomainException("El id del usuario es obligatorio para registrar una venta.");
        if (metodoPago == null || metodoPago.isBlank())
            throw new DomainException("El método de pago es obligatorio.");
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public Long getId()                      { return id; }
    public Long getIdUsuario()               { return idUsuario; }
    public EstadoVenta getEstado()           { return estado; }
    public String getMetodoPago()            { return metodoPago; }
    public LocalDateTime getFecha()          { return fecha; }
    public List<DetalleVenta> getDetalles()  {
        return Collections.unmodifiableList(detalles);
    }

    public void setId(Long id)               { this.id = id; }
}
