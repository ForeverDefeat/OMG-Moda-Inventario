package com.omgmoda.sistema_inventario.producto.dominio;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import com.omgmoda.sistema_inventario.shared.dominio.valueobjects.StockStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VarianteProductoTest {

    @Test
    void registrarEntradaIncrementaStockYActualizaEstado() {
        VarianteProducto variante = varianteConStock(0, 5);

        variante.registrarEntrada(7);

        assertThat(variante.getStockActual()).isEqualTo(7);
        assertThat(variante.getStockStatus()).isEqualTo(StockStatus.NORMAL);
    }

    @Test
    void registrarSalidaRechazaStockInsuficiente() {
        VarianteProducto variante = varianteConStock(3, 5);

        assertThatThrownBy(() -> variante.registrarSalida(4))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Stock insuficiente");
        assertThat(variante.getStockActual()).isEqualTo(3);
    }

    @Test
    void ajustarStockRequiereMotivo() {
        VarianteProducto variante = varianteConStock(10, 5);

        assertThatThrownBy(() -> variante.ajustarStock(2, " "))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("motivo");
    }

    @Test
    void stockStatusDistingueAgotadoBajoYNormal() {
        assertThat(varianteConStock(0, 5).getStockStatus()).isEqualTo(StockStatus.AGOTADO);
        assertThat(varianteConStock(5, 5).getStockStatus()).isEqualTo(StockStatus.BAJO_STOCK);
        assertThat(varianteConStock(6, 5).getStockStatus()).isEqualTo(StockStatus.NORMAL);
    }

    @Test
    void reservaConsumeYLiberaStockSinSobreReservar() {
        VarianteProducto variante = varianteConStock(5, 2);

        variante.reservarStock(3);
        assertThat(variante.getStockActual()).isEqualTo(5);
        assertThat(variante.getStockReservado()).isEqualTo(3);
        assertThat(variante.stockDisponible()).isEqualTo(2);

        variante.liberarReserva(1);
        assertThat(variante.getStockReservado()).isEqualTo(2);

        variante.consumirReserva(2);
        assertThat(variante.getStockActual()).isEqualTo(3);
        assertThat(variante.getStockReservado()).isZero();
    }

    private VarianteProducto varianteConStock(int stockActual, int stockMinimo) {
        Producto producto = new Producto(1L, "Camisa Oxford", "Camisas", "OMG MODA");
        return new VarianteProducto(
                10L,
                producto,
                "M",
                "Azul",
                "Algodon",
                BigDecimal.valueOf(45),
                BigDecimal.valueOf(89.90),
                stockActual,
                stockMinimo
        );
    }
}
