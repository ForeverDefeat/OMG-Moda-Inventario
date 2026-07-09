package com.omgmoda.sistema_inventario.cliente.aplicacion.ports;

import com.omgmoda.sistema_inventario.cliente.aplicacion.dto.ClienteResponseDTO;
import com.omgmoda.sistema_inventario.cliente.aplicacion.dto.CrearClienteDTO;

import java.util.List;

/**
 * Puerto de entrada que declara una operacion disponible para los adaptadores de infraestructura.
 */
public interface IClienteUseCase {

    List<ClienteResponseDTO> listar();

    ClienteResponseDTO crear(CrearClienteDTO dto);
}
