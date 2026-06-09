package com.omgmoda.sistema_inventario.venta.dominio;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VentaTest {

    @Test
    void completarRequiereAlMenosUnDetalleYCalculaTotal() {
        Venta venta = new Venta(7L, "EFECTIVO");

        venta.agregarDetalle(11L, 2, BigDecimal.valueOf(30));
        venta.agregarDetalle(12L, 1, BigDecimal.valueOf(15.50));
        venta.completar();

        assertThat(venta.getEstado()).isEqualTo(EstadoVenta.COMPLETADA);
        assertThat(venta.calcularTotal()).isEqualByComparingTo("75.50");
    }

    @Test
    void completarVentaSinDetallesLanzaErrorDeDominio() {
        Venta venta = new Venta(7L, "EFECTIVO");

        assertThatThrownBy(venta::completar)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("sin detalles");
    }

    @Test
    void noPermiteAgregarDetalleLuegoDeCompletar() {
        Venta venta = new Venta(7L, "EFECTIVO");
        venta.agregarDetalle(11L, 1, BigDecimal.TEN);
        venta.completar();

        assertThatThrownBy(() -> venta.agregarDetalle(12L, 1, BigDecimal.TEN))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("No se pueden agregar detalles");
    }

    @Test
    void soloVentaCompletadaPuedeAnularse() {
        Venta pendiente = new Venta(7L, "EFECTIVO");

        assertThatThrownBy(pendiente::anular)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("No se puede anular");

        Venta completada = new Venta(
                1L,
                7L,
                EstadoVenta.COMPLETADA,
                "EFECTIVO",
                LocalDateTime.now(),
                List.of(new DetalleVenta(1L, 11L, 2, BigDecimal.TEN))
        );
        completada.anular();

        assertThat(completada.getEstado()).isEqualTo(EstadoVenta.ANULADA);
    }
}
