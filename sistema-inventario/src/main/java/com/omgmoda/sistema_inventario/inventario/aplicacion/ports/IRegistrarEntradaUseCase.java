package com.omgmoda.sistema_inventario.inventario.aplicacion.ports;

import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.MovimientoResponseDTO;
import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.RegistrarMovimientoDTO;

/**
 * Input Port — contrato para el caso de uso de registro de entrada de mercadería.
 * Actualiza el stock de la variante y genera el movimiento de tipo ENTRADA (RF03, CU-06).
 */
public interface IRegistrarEntradaUseCase {

    /**
     * Registra una entrada de mercadería al inventario.
     * @param dto datos validados con idVariante, idUsuario, cantidad y motivo opcional.
     * @return movimiento persistido con el stock resultante.
     */
    MovimientoResponseDTO registrar(RegistrarMovimientoDTO dto);
}
