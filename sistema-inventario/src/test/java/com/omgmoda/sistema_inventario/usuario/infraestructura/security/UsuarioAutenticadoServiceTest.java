package com.omgmoda.sistema_inventario.usuario.infraestructura.security;

import com.omgmoda.sistema_inventario.usuario.dominio.RolUsuario;
import com.omgmoda.sistema_inventario.usuario.dominio.Usuario;
import com.omgmoda.sistema_inventario.usuario.dominio.ports.IUsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsuarioAutenticadoServiceTest {

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void obtieneUsuarioActualDesdeCorreoDelPrincipal() {
        IUsuarioRepository usuarioRepository = mock(IUsuarioRepository.class);
        Usuario usuario = new Usuario(
                7L,
                "Admin",
                "admin@omgmoda.com",
                "hash",
                RolUsuario.ADMIN
        );
        when(usuarioRepository.findByCorreo("admin@omgmoda.com")).thenReturn(Optional.of(usuario));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@omgmoda.com", null, List.of())
        );

        UsuarioAutenticadoService service = new UsuarioAutenticadoService(usuarioRepository);

        assertThat(service.obtenerIdUsuarioActual()).isEqualTo(7L);
        assertThat(service.obtenerUsuarioActual().esAdmin()).isTrue();
    }
}
