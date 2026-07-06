package com.omgmoda.sistema_inventario.usuario.infraestructura.adapters;

import com.omgmoda.sistema_inventario.shared.dominio.exception.NotFoundException;
import com.omgmoda.sistema_inventario.usuario.dominio.RolUsuario;
import com.omgmoda.sistema_inventario.usuario.dominio.Usuario;
import com.omgmoda.sistema_inventario.usuario.dominio.ports.IUsuarioRepository;
import com.omgmoda.sistema_inventario.usuario.infraestructura.entities.UsuarioJpaEntity;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida que implementa IUsuarioRepository.
 * Traduce entre la entidad de dominio Usuario y UsuarioJpaEntity.
 * Es la única clase que conoce ambos mundos en este módulo.
 */
public class JpaUsuarioAdapter implements IUsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;

    public JpaUsuarioAdapter(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioJpaEntity entity = toEntity(usuario);
        UsuarioJpaEntity saved = jpaRepository.save(entity);
        usuario.setId(saved.getId());
        return usuario;
    }

    @Override
    public List<Usuario> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .sorted(Comparator.comparing(Usuario::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Usuario> findByCorreo(String correo) {
        return jpaRepository.findByCorreo(correo).map(this::toDomain);
    }

    @Override
    public boolean existsByCorreo(String correo) {
        return jpaRepository.existsByCorreo(correo);
    }

    @Override
    public long countByRolAndActivoTrue(RolUsuario rol) {
        return jpaRepository.countByRolAndActivoTrue(rol);
    }

    @Override
    public void deleteById(Long id) {
        if (!jpaRepository.existsById(id))
            throw new NotFoundException("Usuario no encontrado con id: " + id);
        jpaRepository.deleteById(id);
    }

    // ── Mapeos entre dominio ↔ JPA ─────────────────────────────────────────────

    private UsuarioJpaEntity toEntity(Usuario u) {
        UsuarioJpaEntity entity = new UsuarioJpaEntity();
        entity.setId(u.getId());
        entity.setNombre(u.getNombre());
        entity.setCorreo(u.getCorreo());
        entity.setContrasenia(u.getContrasenia());
        entity.setRol(u.getRol());
        entity.setActivo(u.isActivo());
        return entity;
    }

    private Usuario toDomain(UsuarioJpaEntity e) {
        return new Usuario(
                e.getId(),
                e.getNombre(),
                e.getCorreo(),
                e.getContrasenia(),
                e.getRol(),
                e.isActivo()
        );
    }
}
