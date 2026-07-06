package com.omgmoda.sistema_inventario.venta.infraestructura.adapters;

import com.omgmoda.sistema_inventario.producto.infraestructura.adapters.VarianteJpaRepository;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoReservaStock;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentConcurrencyLockTest {

    @Test
    void repositoriosCriticosUsanBloqueoPesimista() throws Exception {
        assertLock(VentaJpaRepository.class, "findByIdForUpdate", Long.class);
        assertLock(PaymentIntentJpaRepository.class, "findByIdForUpdate", Long.class);
        assertLock(StockReservaJpaRepository.class, "findByIdVentaAndEstadoForUpdate", Long.class, EstadoReservaStock.class);
        assertLock(VarianteJpaRepository.class, "findByIdForUpdate", Long.class);
    }

    private void assertLock(Class<?> type, String method, Class<?>... args) throws Exception {
        Lock lock = type.getMethod(method, args).getAnnotation(Lock.class);
        assertThat(lock).isNotNull();
        assertThat(lock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }
}
