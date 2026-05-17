package com.omgmoda.sistema_inventario.venta.aplicacion.ports;

import com.omgmoda.sistema_inventario.venta.aplicacion.dto.VentaResponseDTO;

/**
 * Input Port — contrato para anular una venta completada.
 * La anulación revierte el stock de todas las variantes involucradas.
 */
public interface IAnularVentaUseCase {

    /**
     * Anula una venta completada y devuelve el stock a las variantes.
     *
     * @param idVenta   identificador de la venta a anular.
     * @param idUsuario extraído del token JWT en el controlador.
     * @return venta en estado ANULADA.
     */
    VentaResponseDTO anular(Long idVenta, Long idUsuario);
}
