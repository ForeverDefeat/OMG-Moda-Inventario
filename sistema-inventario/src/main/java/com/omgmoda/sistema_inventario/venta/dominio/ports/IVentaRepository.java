package com.omgmoda.sistema_inventario.venta.dominio.ports;

import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;
import com.omgmoda.sistema_inventario.venta.dominio.Venta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Output Port — contrato puro para persistencia y consulta de ventas.
 * El dominio define este contrato; la infraestructura lo implementa.
 * No importa ninguna clase de Spring ni JPA.
 */
public interface IVentaRepository {

    /** Persiste una venta nueva con todos sus detalles (cascade). */
    Venta save(Venta venta);

    /** Busca una venta por su identificador incluyendo sus detalles. */
    Optional<Venta> findById(Long id);

    /** Retorna todas las ventas de un usuario. */
    List<Venta> findByUsuarioId(Long idUsuario);

    /** Filtra ventas por estado (PENDIENTE, COMPLETADA, ANULADA). */
    List<Venta> findByEstado(EstadoVenta estado);

    /**
     * Retorna ventas registradas dentro de un rango de fechas.
     * Usado para reportes de ventas por período (RF06).
     */
    List<Venta> findByFechaEntre(LocalDateTime desde, LocalDateTime hasta);
}
