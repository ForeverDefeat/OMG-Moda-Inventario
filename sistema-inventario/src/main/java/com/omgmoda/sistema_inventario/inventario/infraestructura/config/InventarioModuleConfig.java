package com.omgmoda.sistema_inventario.inventario.infraestructura.config;

import com.omgmoda.sistema_inventario.inventario.aplicacion.ports.IRegistrarAjusteUseCase;
import com.omgmoda.sistema_inventario.inventario.aplicacion.ports.IRegistrarEntradaUseCase;
import com.omgmoda.sistema_inventario.inventario.aplicacion.usecases.RegistrarAjusteUseCaseImpl;
import com.omgmoda.sistema_inventario.inventario.aplicacion.usecases.RegistrarEntradaUseCaseImpl;
import com.omgmoda.sistema_inventario.inventario.dominio.ports.IMovimientoRepository;
import com.omgmoda.sistema_inventario.inventario.infraestructura.adapters.JpaMovimientoAdapter;
import com.omgmoda.sistema_inventario.inventario.infraestructura.adapters.MovimientoJpaRepository;
import com.omgmoda.sistema_inventario.inventario.infraestructura.transaction.TransactionalRegistrarAjusteUseCase;
import com.omgmoda.sistema_inventario.inventario.infraestructura.transaction.TransactionalRegistrarEntradaUseCase;
import com.omgmoda.sistema_inventario.producto.dominio.ports.IVarianteRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Punto de ensamblado del módulo Inventario.
 * Inyecta los adaptadores JPA en los casos de uso mediante sus interfaces.
 * Los casos de uso nunca conocen implementaciones concretas.
 *
 * Nota: IVarianteRepository proviene del módulo Producto y se inyecta
 * aquí porque los casos de uso de inventario necesitan leer y actualizar
 * el stock de las variantes.
 */
@Configuration
public class InventarioModuleConfig {

    /**
     * Adaptador de salida: implementa IMovimientoRepository usando Spring Data JPA.
     */
    @Bean
    public IMovimientoRepository movimientoRepository(MovimientoJpaRepository jpaRepository) {
        return new JpaMovimientoAdapter(jpaRepository);
    }

    /**
     * Caso de uso: registrar entrada de mercadería.
     * Depende de IVarianteRepository (módulo producto) e IMovimientoRepository.
     */
    @Bean
    public IRegistrarEntradaUseCase registrarEntradaUseCase(
            IVarianteRepository varianteRepository,
            IMovimientoRepository movimientoRepository) {
        IRegistrarEntradaUseCase useCase =
                new RegistrarEntradaUseCaseImpl(varianteRepository, movimientoRepository);
        return new TransactionalRegistrarEntradaUseCase(useCase);
    }

    /**
     * Caso de uso: ajuste manual de stock.
     * Depende de IVarianteRepository (módulo producto) e IMovimientoRepository.
     */
    @Bean
    public IRegistrarAjusteUseCase registrarAjusteUseCase(
            IVarianteRepository varianteRepository,
            IMovimientoRepository movimientoRepository) {
        IRegistrarAjusteUseCase useCase =
                new RegistrarAjusteUseCaseImpl(varianteRepository, movimientoRepository);
        return new TransactionalRegistrarAjusteUseCase(useCase);
    }
}
