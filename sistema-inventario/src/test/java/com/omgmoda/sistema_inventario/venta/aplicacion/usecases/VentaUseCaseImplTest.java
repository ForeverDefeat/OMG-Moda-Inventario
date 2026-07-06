package com.omgmoda.sistema_inventario.venta.aplicacion.usecases;

import com.omgmoda.sistema_inventario.producto.dominio.Producto;
import com.omgmoda.sistema_inventario.producto.dominio.VarianteProducto;
import com.omgmoda.sistema_inventario.producto.dominio.ports.IVarianteRepository;
import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.CrearVentaDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.VentaResponseDTO;
import com.omgmoda.sistema_inventario.venta.dominio.DetalleVenta;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;
import com.omgmoda.sistema_inventario.venta.dominio.Venta;
import com.omgmoda.sistema_inventario.venta.dominio.ports.IVentaRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VentaUseCaseImplTest {

    private final IVentaRepository ventaRepository = mock(IVentaRepository.class);
    private final IVarianteRepository varianteRepository = mock(IVarianteRepository.class);

    @Test
    void registrarVentaDescuentaStockCompletaVentaYPersiste() {
        VarianteProducto variante = varianteConStock(8);
        when(varianteRepository.findById(10L)).thenReturn(Optional.of(variante));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> {
            Venta venta = invocation.getArgument(0);
            venta.setId(77L);
            return venta;
        });
        RegistrarVentaUseCaseImpl useCase = new RegistrarVentaUseCaseImpl(
                ventaRepository,
                varianteRepository
        );

        VentaResponseDTO respuesta = useCase.registrar(
                new CrearVentaDTO(
                        "EFECTIVO",
                        List.of(new CrearVentaDTO.ItemVentaDTO(10L, 3, BigDecimal.valueOf(20)))
                ),
                5L
        );

        assertThat(variante.getStockActual()).isEqualTo(5);
        assertThat(respuesta.idVenta()).isEqualTo(77L);
        assertThat(respuesta.estado()).isEqualTo(EstadoVenta.COMPLETED);
        assertThat(respuesta.total()).isEqualByComparingTo("60");
        verify(varianteRepository).save(variante);
        verify(ventaRepository).save(any(Venta.class));
    }

    @Test
    void registrarVentaValidaStockAntesDeDescontar() {
        VarianteProducto variante = varianteConStock(2);
        when(varianteRepository.findById(10L)).thenReturn(Optional.of(variante));
        RegistrarVentaUseCaseImpl useCase = new RegistrarVentaUseCaseImpl(
                ventaRepository,
                varianteRepository
        );

        assertThatThrownBy(() -> useCase.registrar(
                new CrearVentaDTO(
                        "EFECTIVO",
                        List.of(new CrearVentaDTO.ItemVentaDTO(10L, 3, BigDecimal.valueOf(20)))
                ),
                5L
        )).isInstanceOf(DomainException.class)
                .hasMessageContaining("Stock insuficiente");
        assertThat(variante.getStockActual()).isEqualTo(2);
    }

    @Test
    void anularVentaRevierteStockYPersisteVentaAnulada() {
        VarianteProducto variante = varianteConStock(5);
        Venta venta = ventaCompletada();
        when(ventaRepository.findById(77L)).thenReturn(Optional.of(venta));
        when(varianteRepository.findById(10L)).thenReturn(Optional.of(variante));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AnularVentaUseCaseImpl useCase = new AnularVentaUseCaseImpl(
                ventaRepository,
                varianteRepository
        );

        VentaResponseDTO respuesta = useCase.anular(77L, 5L);

        assertThat(variante.getStockActual()).isEqualTo(8);
        assertThat(respuesta.estado()).isEqualTo(EstadoVenta.CANCELLED);
        verify(varianteRepository).save(variante);
        verify(ventaRepository).save(venta);
    }

    private Venta ventaCompletada() {
        return new Venta(
                77L,
                5L,
                EstadoVenta.COMPLETED,
                "EFECTIVO",
                LocalDateTime.now(),
                List.of(new DetalleVenta(1L, 10L, 3, BigDecimal.valueOf(20)))
        );
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
