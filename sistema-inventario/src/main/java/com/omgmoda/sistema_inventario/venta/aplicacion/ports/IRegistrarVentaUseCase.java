package com.omgmoda.sistema_inventario.venta.aplicacion.ports;

import com.omgmoda.sistema_inventario.venta.aplicacion.dto.CrearVentaDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.VentaResponseDTO;

/**
 * Input Port — contrato para el registro completo de una venta (RF04, CU-07).
 * El controlador REST invoca este puerto sin conocer la implementación.
 */
public interface IRegistrarVentaUseCase {

    /**
     * Registra una venta completa: valida stock, descuenta variantes,
     * crea la venta con sus detalles y la completa.
     *
     * @param dto      ítems de la venta y método de pago.
     * @param idUsuario extraído del token JWT en el controlador.
     * @return venta completada con detalles y total calculado.
     */
    VentaResponseDTO registrar(CrearVentaDTO dto, Long idUsuario);
}
