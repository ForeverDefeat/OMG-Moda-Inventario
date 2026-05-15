package com.omgmoda.sistema_inventario.producto.aplicacion.ports;

import com.omgmoda.sistema_inventario.producto.aplicacion.dto.CrearProductoDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;

import java.util.List;

/**
 * Input Port — contrato de orquestación para el registro de productos.
 * El controlador REST invoca este puerto sin conocer la implementación.
 */
public interface IRegistrarProductoUseCase {

    /**
     * Registra un producto nuevo con todas sus variantes iniciales.
     * @param dto datos validados del producto y sus variantes.
     * @return lista de variantes creadas con sus datos completos.
     */
    List<VarianteResponseDTO> registrar(CrearProductoDTO dto);
}
