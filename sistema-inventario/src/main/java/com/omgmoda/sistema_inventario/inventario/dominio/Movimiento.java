package com.omgmoda.sistema_inventario.inventario.dominio;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;

import java.time.LocalDateTime;

/**
 * Entity de dominio que registra cada transacción de stock.
 * Inmutable una vez creado: el historial de movimientos no se modifica.
 * Corresponde a la tabla MOVIMIENTO (3FN).
 */
public class Movimiento {

    private Long id;
    private Long idVariante;
    private Long idUsuario;
    private TipoMovimiento tipo;
    private int cantidad;
    private String motivo;
    private LocalDateTime fecha;

    // Constructor para creación nueva desde el dominio
    public Movimiento(Long idVariante,
                      Long idUsuario,
                      TipoMovimiento tipo,
                      int cantidad,
                      String motivo) {
        this.idVariante = idVariante;
        this.idUsuario = idUsuario;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.fecha = LocalDateTime.now();
        validarTipo();
    }

    // Constructor para reconstitución desde persistencia
    public Movimiento(Long id,
                      Long idVariante,
                      Long idUsuario,
                      TipoMovimiento tipo,
                      int cantidad,
                      String motivo,
                      LocalDateTime fecha) {
        this.id = id;
        this.idVariante = idVariante;
        this.idUsuario = idUsuario;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.fecha = fecha;
        validarTipo();
    }

    /**
     * Aplica las reglas de negocio según el tipo de movimiento.
     * - ENTRADA y SALIDA: cantidad debe ser estrictamente positiva.
     * - AJUSTE: cantidad puede ser cero (corrección a cero es válida)
     *   pero siempre requiere motivo no vacío.
     */
    public void validarTipo() {
        if (tipo == null)
            throw new DomainException("El tipo de movimiento es obligatorio.");

        switch (tipo) {
            case ENTRADA, SALIDA -> {
                if (cantidad <= 0)
                    throw new DomainException(
                            "La cantidad para un movimiento de tipo " + tipo +
                            " debe ser mayor a cero."
                    );
            }
            case AJUSTE -> {
                if (cantidad < 0)
                    throw new DomainException(
                            "La cantidad de un ajuste no puede ser negativa."
                    );
                if (motivo == null || motivo.isBlank())
                    throw new DomainException(
                            "El motivo es obligatorio para movimientos de tipo AJUSTE."
                    );
            }
        }
    }

    // ── Getters (sin setters: inmutabilidad post-creación) ─────────────────────

    public Long getId()               { return id; }
    public Long getIdVariante()       { return idVariante; }
    public Long getIdUsuario()        { return idUsuario; }
    public TipoMovimiento getTipo()   { return tipo; }
    public int getCantidad()          { return cantidad; }
    public String getMotivo()         { return motivo; }
    public LocalDateTime getFecha()   { return fecha; }

    // Solo usado por el adaptador JPA al persistir
    public void setId(Long id)        { this.id = id; }
}
