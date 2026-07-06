package com.omgmoda.sistema_inventario.usuario.aplicacion.usecases;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import com.omgmoda.sistema_inventario.usuario.aplicacion.dto.CrearUsuarioDTO;
import com.omgmoda.sistema_inventario.usuario.dominio.RolUsuario;
import com.omgmoda.sistema_inventario.usuario.dominio.Usuario;
import com.omgmoda.sistema_inventario.usuario.dominio.ports.IUsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioAdminServiceTest {

    private final IUsuarioRepository usuarioRepository = mock(IUsuarioRepository.class);
    private final IPasswordEncoder passwordEncoder = mock(IPasswordEncoder.class);
    private final UsuarioAdminService service = new UsuarioAdminService(usuarioRepository, passwordEncoder);

    @Test
    void crearUsuarioHasheaContraseniaYNormalizaCorreo() {
        CrearUsuarioDTO dto = new CrearUsuarioDTO("Ana", " ANA@OMGModa.com ", "secreto1", RolUsuario.VENDEDOR);
        when(usuarioRepository.existsByCorreo("ana@omgmoda.com")).thenReturn(false);
        when(passwordEncoder.encode("secreto1")).thenReturn("hash-bcrypt");
        when(usuarioRepository.save(org.mockito.ArgumentMatchers.any(Usuario.class)))
                .thenAnswer(invocation -> {
                    Usuario usuario = invocation.getArgument(0);
                    usuario.setId(9L);
                    return usuario;
                });

        var response = service.crear(dto);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getCorreo()).isEqualTo("ana@omgmoda.com");
        assertThat(captor.getValue().getContrasenia()).isEqualTo("hash-bcrypt");
        assertThat(captor.getValue().isActivo()).isTrue();
        assertThat(response.id()).isEqualTo(9L);
    }

    @Test
    void crearUsuarioRechazaCorreoDuplicado() {
        CrearUsuarioDTO dto = new CrearUsuarioDTO("Ana", "ana@omgmoda.com", "secreto1", RolUsuario.VENDEDOR);
        when(usuarioRepository.existsByCorreo("ana@omgmoda.com")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Ya existe");
    }

    @Test
    void listarUsuariosExponeEstadoActivo() {
        when(usuarioRepository.findAll()).thenReturn(List.of(
                new Usuario(1L, "Admin", "admin@omgmoda.com", "hash", RolUsuario.ADMIN, true),
                new Usuario(2L, "Vendedor", "vendedor@omgmoda.com", "hash", RolUsuario.VENDEDOR, false)
        ));

        var response = service.listar();

        assertThat(response).hasSize(2);
        assertThat(response.get(0).activo()).isTrue();
        assertThat(response.get(1).activo()).isFalse();
    }

    @Test
    void resetearContraseniaGuardaHashNuevo() {
        Usuario usuario = new Usuario(3L, "Ana", "ana@omgmoda.com", "hash-old", RolUsuario.VENDEDOR, true);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("nueva123")).thenReturn("hash-new");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        service.resetearContrasenia(3L, "nueva123");

        assertThat(usuario.getContrasenia()).isEqualTo("hash-new");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void desactivarNoPermiteDejarSinAdminActivo() {
        Usuario admin = new Usuario(1L, "Admin", "admin@omgmoda.com", "hash", RolUsuario.ADMIN, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByRolAndActivoTrue(RolUsuario.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.desactivar(1L, 1L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("administradores activos");
    }

    @Test
    void desactivarYReactivarActualizanEstado() {
        Usuario vendedor = new Usuario(2L, "Vendedor", "vendedor@omgmoda.com", "hash", RolUsuario.VENDEDOR, true);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(vendedor));
        when(usuarioRepository.save(vendedor)).thenReturn(vendedor);

        var desactivado = service.desactivar(2L, 1L);
        assertThat(desactivado.activo()).isFalse();

        var reactivado = service.reactivar(2L);
        assertThat(reactivado.activo()).isTrue();
    }
}
