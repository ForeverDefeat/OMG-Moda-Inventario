package com.omgmoda.sistema_inventario.inventario.dominio.ports;

import com.omgmoda.sistema_inventario.inventario.dominio.Movimiento;
import com.omgmoda.sistema_inventario.inventario.dominio.TipoMovimiento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Output Port — contrato puro para persistencia y consulta de movimientos.
 * El dominio define este contrato; la infraestructura lo implementa.
 * No importa ninguna clase de Spring ni JPA.
 */
public interface IMovimientoRepository {

    /** Persiste un movimiento nuevo. Los movimientos no se actualizan ni eliminan. */
    Movimiento save(Movimiento movimiento);

    /** Busca un movimiento por su identificador. */
    Optional<Movimiento> findById(Long id);

    /** Retorna el historial completo de movimientos de una variante. */
    List<Movimiento> findByVarianteId(Long idVariante);

    /** Filtra movimientos de una variante por tipo (ENTRADA, SALIDA, AJUSTE). */
    List<Movimiento> findByVarianteIdAndTipo(Long idVariante, TipoMovimiento tipo);

    /**
     * Retorna movimientos registrados dentro de un rango de fechas.
     * Útil para reportes de inventario por período (RF06).
     */
    List<Movimiento> findByFechaEntre(LocalDateTime desde, LocalDateTime hasta);

    /** Retorna todos los movimientos registrados por un usuario. */
    List<Movimiento> findByUsuarioId(Long idUsuario);
}
