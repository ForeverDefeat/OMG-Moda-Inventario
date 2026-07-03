package com.omgmoda.sistema_inventario.cliente.aplicacion.usecases;

import com.omgmoda.sistema_inventario.cliente.aplicacion.dto.CrearClienteDTO;
import com.omgmoda.sistema_inventario.cliente.dominio.Cliente;
import com.omgmoda.sistema_inventario.cliente.dominio.ports.IClienteRepository;
import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClienteUseCaseImplTest {

    private final IClienteRepository repository = mock(IClienteRepository.class);
    private final ClienteUseCaseImpl useCase = new ClienteUseCaseImpl(repository);

    @Test
    void creaClienteConDefaultsCuandoCamposOpcionalesNoLlegan() {
        when(repository.existsByCorreo("ana@email.com")).thenReturn(false);
        when(repository.save(any(Cliente.class))).thenReturn(new Cliente(
                8L,
                "Ana Diaz",
                "ana@email.com",
                "999",
                "Nuevo",
                BigDecimal.ZERO,
                LocalDate.now()
        ));

        var response = useCase.crear(new CrearClienteDTO(
                "Ana Diaz",
                "ana@email.com",
                "999",
                "Nuevo",
                null,
                null
        ));

        assertThat(response.id()).isEqualTo(8L);
        assertThat(response.totalCompras()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void rechazaCorreoDuplicado() {
        when(repository.existsByCorreo("ana@email.com")).thenReturn(true);

        assertThatThrownBy(() -> useCase.crear(new CrearClienteDTO(
                "Ana Diaz",
                "ana@email.com",
                "999",
                "Nuevo",
                BigDecimal.ZERO,
                LocalDate.now()
        ))).isInstanceOf(DomainException.class);
    }

    @Test
    void listaClientesOrdenadosPorNombre() {
        when(repository.findAll()).thenReturn(List.of(
                cliente(2L, "Zoe"),
                cliente(1L, "Ana")
        ));

        var response = useCase.listar();

        assertThat(response).extracting("nombre").containsExactly("Ana", "Zoe");
    }

    private Cliente cliente(Long id, String nombre) {
        return new Cliente(id, nombre, nombre.toLowerCase() + "@email.com", "999", "VIP", BigDecimal.TEN, LocalDate.now());
    }
}
