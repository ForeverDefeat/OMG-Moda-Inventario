package com.omgmoda.sistema_inventario.cliente.infraestructura.controllers;

import com.omgmoda.sistema_inventario.cliente.aplicacion.dto.ClienteResponseDTO;
import com.omgmoda.sistema_inventario.cliente.aplicacion.dto.CrearClienteDTO;
import com.omgmoda.sistema_inventario.cliente.aplicacion.ports.IClienteUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClienteRestControllerTest {

    private final IClienteUseCase useCase = mock(IClienteUseCase.class);
    private final ClienteRestController controller = new ClienteRestController(useCase);

    @Test
    void listarDelegarAlCasoDeUso() {
        when(useCase.listar()).thenReturn(List.of());

        var response = controller.listar();

        assertThat(response.getBody()).isEmpty();
        verify(useCase).listar();
    }

    @Test
    void crearRetornaCreatedYConservaPayload() {
        CrearClienteDTO dto = new CrearClienteDTO("Ana", "ana@email.com", "999", "Nuevo", BigDecimal.ZERO, LocalDate.now());
        when(useCase.crear(dto)).thenReturn(new ClienteResponseDTO(1L, "Ana", "ana@email.com", "999", "Nuevo", BigDecimal.ZERO, LocalDate.now()));

        var response = controller.crear(dto);

        ArgumentCaptor<CrearClienteDTO> captor = ArgumentCaptor.forClass(CrearClienteDTO.class);
        verify(useCase).crear(captor.capture());
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(captor.getValue().correo()).isEqualTo("ana@email.com");
    }
}
