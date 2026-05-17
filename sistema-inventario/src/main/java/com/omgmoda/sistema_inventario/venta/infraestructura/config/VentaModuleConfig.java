package com.omgmoda.sistema_inventario.venta.infraestructura.config;

import com.omgmoda.sistema_inventario.producto.dominio.ports.IVarianteRepository;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IAnularVentaUseCase;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IConsultarVentaUseCase;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IRegistrarVentaUseCase;
import com.omgmoda.sistema_inventario.venta.aplicacion.usecases.AnularVentaUseCaseImpl;
import com.omgmoda.sistema_inventario.venta.aplicacion.usecases.ConsultarVentaUseCaseImpl;
import com.omgmoda.sistema_inventario.venta.aplicacion.usecases.RegistrarVentaUseCaseImpl;
import com.omgmoda.sistema_inventario.venta.dominio.ports.IVentaRepository;
import com.omgmoda.sistema_inventario.venta.infraestructura.adapters.JpaVentaAdapter;
import com.omgmoda.sistema_inventario.venta.infraestructura.adapters.VentaJpaRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Punto de ensamblado del módulo Venta.
 *
 * Habilita la gestión de transacciones para garantizar atomicidad
 * en los casos de uso que operan sobre múltiples agregados:
 *   - RegistrarVenta: descuenta stock en N variantes y persiste la venta.
 *   - AnularVenta: revierte stock en N variantes y actualiza la venta.
 *
 * Si cualquier operación falla, toda la transacción se revierte (rollback),
 * garantizando consistencia entre VARIANTE_PRODUCTO, VENTA y DETALLE_VENTA.
 *
 * Los casos de uso no usan @Transactional directamente: la anotación
 * se aplica en los beans de este config para respetar la separación de capas.
 */
@Configuration
@EnableTransactionManagement
public class VentaModuleConfig {

    /**
     * Adaptador de salida: implementa IVentaRepository usando Spring Data JPA.
     */
    @Bean
    public IVentaRepository ventaRepository(VentaJpaRepository jpaRepository) {
        return new JpaVentaAdapter(jpaRepository);
    }

    /**
     * Caso de uso: registrar venta completa con descuento de stock.
     * Transaccional: rollback si falla el descuento de cualquier variante.
     */
    @Bean
    public IRegistrarVentaUseCase registrarVentaUseCase(
            IVentaRepository ventaRepository,
            IVarianteRepository varianteRepository) {
        return new RegistrarVentaUseCaseImpl(ventaRepository, varianteRepository);
    }

    /**
     * Caso de uso: consultar ventas por id, usuario, estado o rango de fechas.
     */
    @Bean
    public IConsultarVentaUseCase consultarVentaUseCase(IVentaRepository ventaRepository) {
        return new ConsultarVentaUseCaseImpl(ventaRepository);
    }

    /**
     * Caso de uso: anular venta y revertir stock.
     * Transaccional: rollback si falla la reversión de cualquier variante.
     */
    @Bean
    public IAnularVentaUseCase anularVentaUseCase(
            IVentaRepository ventaRepository,
            IVarianteRepository varianteRepository) {
        return new AnularVentaUseCaseImpl(ventaRepository, varianteRepository);
    }
}
