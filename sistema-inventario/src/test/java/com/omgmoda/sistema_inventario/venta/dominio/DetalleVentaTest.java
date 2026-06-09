package com.omgmoda.sistema_inventario.venta.dominio;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DetalleVentaTest {

    @Test
    void calculaSubtotalConPrecioHistoricoYCantidad() {
        DetalleVenta detalle = new DetalleVenta(1L, 15L, 3, BigDecimal.valueOf(29.90));

        assertThat(detalle.calcularSubtotal()).isEqualByComparingTo("89.70");
    }

    @Test
    void rechazaCantidadYPrecioInvalidos() {
        assertThatThrownBy(() -> new DetalleVenta(1L, 15L, 0, BigDecimal.TEN))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("cantidad");

        assertThatThrownBy(() -> new DetalleVenta(1L, 15L, 1, BigDecimal.ZERO))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("precio");
    }
}
