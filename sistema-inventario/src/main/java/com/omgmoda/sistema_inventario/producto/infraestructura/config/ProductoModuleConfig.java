package com.omgmoda.sistema_inventario.producto.infraestructura.config;

import com.omgmoda.sistema_inventario.producto.aplicacion.ports.IBuscarVariantesUseCase;
import com.omgmoda.sistema_inventario.producto.aplicacion.ports.IRegistrarProductoUseCase;
import com.omgmoda.sistema_inventario.producto.aplicacion.usecases.BuscarVariantesUseCaseImpl;
import com.omgmoda.sistema_inventario.producto.aplicacion.usecases.RegistrarProductoUseCaseImpl;
import com.omgmoda.sistema_inventario.producto.dominio.ports.IVarianteRepository;
import com.omgmoda.sistema_inventario.producto.infraestructura.adapters.JpaVarianteAdapter;
import com.omgmoda.sistema_inventario.producto.infraestructura.adapters.VarianteJpaRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Punto de ensamblado del módulo Producto.
 * Es la única clase que conoce qué implementación concreta
 * se inyecta en cada caso de uso (Dependency Inversion).
 * Los casos de uso solo conocen interfaces, nunca implementaciones.
 */
@Configuration
public class ProductoModuleConfig {

    /**
     * Adaptador de salida: implementa IVarianteRepository usando Spring Data JPA.
     */
    @Bean
    public IVarianteRepository varianteRepository(VarianteJpaRepository jpaRepository) {
        return new JpaVarianteAdapter(jpaRepository);
    }

    /**
     * Caso de uso: registrar producto con variantes.
     * Recibe IVarianteRepository (interfaz), no JpaVarianteAdapter (implementación).
     */
    @Bean
    public IRegistrarProductoUseCase registrarProductoUseCase(IVarianteRepository repo) {
        return new RegistrarProductoUseCaseImpl(repo);
    }

    /**
     * Caso de uso: buscar variantes con filtros y alertas de bajo stock.
     */
    @Bean
    public IBuscarVariantesUseCase buscarVariantesUseCase(IVarianteRepository repo) {
        return new BuscarVariantesUseCaseImpl(repo);
    }
}
