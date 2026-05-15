package com.omgmoda.sistema_inventario.inventario.aplicacion.ports;

import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.MovimientoResponseDTO;
import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.RegistrarMovimientoDTO;

/**
 * Input Port — contrato para el caso de uso de ajuste manual de stock.
 * Corrige el stock físico de una variante con motivo obligatorio (CU-08).
 */
public interface IRegistrarAjusteUseCase {

    /**
     * Aplica una corrección manual al stock de una variante.
     * El campo cantidad representa el nuevo valor absoluto del stock,
     * no una diferencia. El motivo es obligatorio para trazabilidad.
     *
     * @param dto datos validados con idVariante, idUsuario, cantidad y motivo.
     * @return movimiento de tipo AJUSTE persistido con el stock resultante.
     */
    MovimientoResponseDTO ajustar(RegistrarMovimientoDTO dto);
}
