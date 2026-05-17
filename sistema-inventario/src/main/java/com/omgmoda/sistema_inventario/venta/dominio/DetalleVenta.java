package com.omgmoda.sistema_inventario.venta.dominio;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;

import java.math.BigDecimal;

/**
 * Entity que representa una línea de la venta.
 * Vincula una VarianteProducto con la cantidad vendida y el precio
 * en el momento de la venta (precio histórico).
 *
 * No contiene subtotal como atributo persistido — cumple 3FN.
 * El subtotal se calcula mediante calcularSubtotal() cuando se necesite.
 *
 * Solo puede ser creada a través del Aggregate Root Venta.
 */
public class DetalleVenta {

    private Long id;
    private Long idVariante;
    private int cantidad;
    private BigDecimal precioUnitario;

    // Constructor usado por Venta.agregarDetalle()
    DetalleVenta(Long idVariante, int cantidad, BigDecimal precioUnitario) {
        validar(idVariante, cantidad, precioUnitario);
        this.idVariante = idVariante;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    // Constructor para reconstitución desde persistencia
    public DetalleVenta(Long id,
                        Long idVariante,
                        int cantidad,
                        BigDecimal precioUnitario) {
        validar(idVariante, cantidad, precioUnitario);
        this.id = id;
        this.idVariante = idVariante;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    // ── Lógica de negocio ──────────────────────────────────────────────────────

    /**
     * Calcula el subtotal de esta línea.
     * Método calculado — no atributo persistido (3FN).
     */
    public BigDecimal calcularSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    // ── Validación interna ─────────────────────────────────────────────────────

    private void validar(Long idVariante, int cantidad, BigDecimal precioUnitario) {
        if (idVariante == null)
            throw new DomainException("El id de variante en el detalle no puede ser nulo.");
        if (cantidad <= 0)
            throw new DomainException("La cantidad del detalle debe ser mayor a cero.");
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) <= 0)
            throw new DomainException("El precio unitario del detalle debe ser mayor a cero.");
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public Long getId()                     { return id; }
    public Long getIdVariante()             { return idVariante; }
    public int getCantidad()                { return cantidad; }
    public BigDecimal getPrecioUnitario()   { return precioUnitario; }

    public void setId(Long id)              { this.id = id; }
}
