package com.omgmoda.sistema_inventario.producto.aplicacion.ports;

import com.omgmoda.sistema_inventario.producto.aplicacion.dto.ActualizarProductoDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.dto.VarianteResponseDTO;

import java.util.List;

public interface IActualizarProductoUseCase {

    List<VarianteResponseDTO> actualizar(Long idProducto, ActualizarProductoDTO dto);
}
