package com.omgmoda.sistema_inventario.venta.aplicacion.ports;

import com.omgmoda.sistema_inventario.venta.aplicacion.dto.VentaResponseDTO;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Input Port — contrato para consultas de ventas (RF06).
 */
public interface IConsultarVentaUseCase {

    /**
     * Busca una venta por su identificador.
     * @throws com.omgmoda.shared.dominio.exception.NotFoundException si no existe.
     */
    VentaResponseDTO buscarPorId(Long idVenta);

    /** Retorna todas las ventas de un usuario. */
    List<VentaResponseDTO> buscarPorUsuario(Long idUsuario);

    /** Filtra ventas por estado. */
    List<VentaResponseDTO> buscarPorEstado(EstadoVenta estado);

    /** Retorna ventas dentro de un rango de fechas para reportes. */
    List<VentaResponseDTO> buscarPorFechas(LocalDateTime desde, LocalDateTime hasta);
}
