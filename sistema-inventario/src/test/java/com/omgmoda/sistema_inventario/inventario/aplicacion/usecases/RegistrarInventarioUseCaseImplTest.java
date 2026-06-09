package com.omgmoda.sistema_inventario.inventario.aplicacion.usecases;

import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.MovimientoResponseDTO;
import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.RegistrarAjusteDTO;
import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.RegistrarEntradaDTO;
import com.omgmoda.sistema_inventario.inventario.dominio.Movimiento;
import com.omgmoda.sistema_inventario.inventario.dominio.TipoMovimiento;
import com.omgmoda.sistema_inventario.inventario.dominio.ports.IMovimientoRepository;
import com.omgmoda.sistema_inventario.producto.dominio.Producto;
import com.omgmoda.sistema_inventario.producto.dominio.VarianteProducto;
import com.omgmoda.sistema_inventario.producto.dominio.ports.IVarianteRepository;
import com.omgmoda.sistema_inventario.shared.dominio.exception.NotFoundException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrarInventarioUseCaseImplTest {

    private final IVarianteRepository varianteRepository = mock(IVarianteRepository.class);
    private final IMovimientoRepository movimientoRepository = mock(IMovimientoRepository.class);

    @Test
    void registrarEntradaIncrementaStockYGuardaMovimiento() {
        VarianteProducto variante = varianteConStock(2);
        when(varianteRepository.findById(10L)).thenReturn(Optional.of(variante));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
            Movimiento movimiento = invocation.getArgument(0);
            movimiento.setId(50L);
            return movimiento;
        });
        RegistrarEntradaUseCaseImpl useCase = new RegistrarEntradaUseCaseImpl(
                varianteRepository,
                movimientoRepository
        );

        MovimientoResponseDTO respuesta = useCase.registrar(
                new RegistrarEntradaDTO(10L, 5, "Reposicion"),
                3L
        );

        assertThat(variante.getStockActual()).isEqualTo(7);
        assertThat(respuesta.idMovimiento()).isEqualTo(50L);
        assertThat(respuesta.tipo()).isEqualTo(TipoMovimiento.ENTRADA);
        assertThat(respuesta.stockResultante()).isEqualTo(7);
        verify(varianteRepository).save(variante);
        verify(movimientoRepository).save(any(Movimiento.class));
    }

    @Test
    void registrarAjusteCorrigeStockYGuardaTrazabilidad() {
        VarianteProducto variante = varianteConStock(12);
        when(varianteRepository.findById(10L)).thenReturn(Optional.of(variante));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        RegistrarAjusteUseCaseImpl useCase = new RegistrarAjusteUseCaseImpl(
                varianteRepository,
                movimientoRepository
        );

        MovimientoResponseDTO respuesta = useCase.ajustar(
                new RegistrarAjusteDTO(10L, 4, "Conteo fisico"),
                3L
        );

        assertThat(variante.getStockActual()).isEqualTo(4);
        assertThat(respuesta.tipo()).isEqualTo(TipoMovimiento.AJUSTE);
        assertThat(respuesta.stockResultante()).isEqualTo(4);
        verify(varianteRepository).save(variante);
        verify(movimientoRepository).save(any(Movimiento.class));
    }

    @Test
    void entradaLanzaNotFoundSiVarianteNoExiste() {
        when(varianteRepository.findById(99L)).thenReturn(Optional.empty());
        RegistrarEntradaUseCaseImpl useCase = new RegistrarEntradaUseCaseImpl(
                varianteRepository,
                movimientoRepository
        );

        assertThatThrownBy(() -> useCase.registrar(new RegistrarEntradaDTO(99L, 1, null), 3L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Variante no encontrada");
    }

    private VarianteProducto varianteConStock(int stockActual) {
        return new VarianteProducto(
                10L,
                new Producto(1L, "Camisa Oxford", "Camisas", "OMG MODA"),
                "M",
                "Azul",
                "Algodon",
                BigDecimal.valueOf(45),
                BigDecimal.valueOf(89.90),
                stockActual,
                5
        );
    }
}
