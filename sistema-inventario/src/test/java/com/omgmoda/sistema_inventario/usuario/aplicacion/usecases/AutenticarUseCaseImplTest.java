package com.omgmoda.sistema_inventario.usuario.aplicacion.usecases;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import com.omgmoda.sistema_inventario.usuario.aplicacion.dto.LoginDTO;
import com.omgmoda.sistema_inventario.usuario.dominio.RolUsuario;
import com.omgmoda.sistema_inventario.usuario.dominio.Usuario;
import com.omgmoda.sistema_inventario.usuario.dominio.ports.IUsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutenticarUseCaseImplTest {

    private final IUsuarioRepository usuarioRepository = mock(IUsuarioRepository.class);
    private final IPasswordEncoder passwordEncoder = mock(IPasswordEncoder.class);
    private final IJwtTokenProvider jwtTokenProvider = mock(IJwtTokenProvider.class);
    private final AutenticarUseCaseImpl useCase = new AutenticarUseCaseImpl(usuarioRepository, passwordEncoder, jwtTokenProvider);

    @Test
    void rechazaLoginDeUsuarioInactivo() {
        Usuario usuario = new Usuario(5L, "Ana", "ana@omgmoda.com", "hash", RolUsuario.VENDEDOR, false);
        when(usuarioRepository.findByCorreo("ana@omgmoda.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> useCase.autenticar(new LoginDTO("ana@omgmoda.com", "secreto1")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("inactivo");

        verify(passwordEncoder, never()).matches(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }
}
