package com.omgmoda.sistema_inventario.cliente.infraestructura.adapters;

import com.omgmoda.sistema_inventario.cliente.dominio.Cliente;
import com.omgmoda.sistema_inventario.cliente.dominio.ports.IClienteRepository;
import com.omgmoda.sistema_inventario.cliente.infraestructura.entities.ClienteJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaClienteAdapter implements IClienteRepository {

    private final ClienteJpaRepository jpaRepository;

    public JpaClienteAdapter(ClienteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Cliente> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Cliente save(Cliente cliente) {
        return toDomain(jpaRepository.save(toEntity(cliente)));
    }

    @Override
    public boolean existsByCorreo(String correo) {
        return jpaRepository.existsByCorreo(correo);
    }

    private ClienteJpaEntity toEntity(Cliente cliente) {
        ClienteJpaEntity entity = new ClienteJpaEntity();
        entity.setId(cliente.getId());
        entity.setNombre(cliente.getNombre());
        entity.setCorreo(cliente.getCorreo());
        entity.setTelefono(cliente.getTelefono());
        entity.setSegmento(cliente.getSegmento());
        entity.setTotalCompras(cliente.getTotalCompras());
        entity.setUltimaCompra(cliente.getUltimaCompra());
        return entity;
    }

    private Cliente toDomain(ClienteJpaEntity entity) {
        return new Cliente(
                entity.getId(),
                entity.getNombre(),
                entity.getCorreo(),
                entity.getTelefono(),
                entity.getSegmento(),
                entity.getTotalCompras(),
                entity.getUltimaCompra()
        );
    }
}
