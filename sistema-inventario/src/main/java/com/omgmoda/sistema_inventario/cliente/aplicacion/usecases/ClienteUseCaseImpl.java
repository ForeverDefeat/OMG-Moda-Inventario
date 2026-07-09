package com.omgmoda.sistema_inventario.cliente.aplicacion.usecases;

import com.omgmoda.sistema_inventario.cliente.aplicacion.dto.ClienteResponseDTO;
import com.omgmoda.sistema_inventario.cliente.aplicacion.dto.CrearClienteDTO;
import com.omgmoda.sistema_inventario.cliente.aplicacion.ports.IClienteUseCase;
import com.omgmoda.sistema_inventario.cliente.dominio.Cliente;
import com.omgmoda.sistema_inventario.cliente.dominio.ports.IClienteRepository;
import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Implementacion de caso de uso que orquesta validaciones, dominio y puertos de persistencia.
 */
@Service
public class ClienteUseCaseImpl implements IClienteUseCase {

    private final IClienteRepository clienteRepository;

    public ClienteUseCaseImpl(IClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public List<ClienteResponseDTO> listar() {
        return clienteRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Cliente::getNombre))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ClienteResponseDTO crear(CrearClienteDTO dto) {
        if (clienteRepository.existsByCorreo(dto.correo())) {
            throw new DomainException("Ya existe un cliente registrado con ese correo.");
        }

        Cliente cliente = new Cliente(
                null,
                dto.nombre(),
                dto.correo(),
                dto.telefono(),
                dto.segmento(),
                dto.totalCompras() != null ? dto.totalCompras() : BigDecimal.ZERO,
                dto.ultimaCompra() != null ? dto.ultimaCompra() : LocalDate.now()
        );
        return toResponse(clienteRepository.save(cliente));
    }

    private ClienteResponseDTO toResponse(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getCorreo(),
                cliente.getTelefono(),
                cliente.getSegmento(),
                cliente.getTotalCompras(),
                cliente.getUltimaCompra()
        );
    }
}
