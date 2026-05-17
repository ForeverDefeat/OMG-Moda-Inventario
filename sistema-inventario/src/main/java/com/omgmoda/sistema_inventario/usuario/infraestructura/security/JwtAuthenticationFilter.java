package com.omgmoda.sistema_inventario.usuario.infraestructura.security;

import com.omgmoda.sistema_inventario.usuario.aplicacion.usecases.IJwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro JWT que intercepta cada request HTTP una sola vez.
 * Extrae el token del header Authorization, lo valida y,
 * si es correcto, establece el contexto de seguridad de Spring.
 *
 * Flujo:
 * 1. Extrae el token del header "Authorization: Bearer <token>".
 * 2. Valida la firma y expiración mediante IJwtTokenProvider.
 * 3. Extrae correo y rol del payload.
 * 4. Registra la autenticación en SecurityContextHolder.
 * 5. Continúa la cadena de filtros.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final IJwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(IJwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtTokenProvider.esValido(token)) {
                String correo = jwtTokenProvider.extraerCorreo(token);

                // Extraer el claim "rol" del token para construir la autoridad
                // El prefijo ROLE_ es requerido por Spring Security
                String rol = extraerRol(token);
                List<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_" + rol));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(correo, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    // ── Método interno ─────────────────────────────────────────────────────────

    /**
     * Extrae el claim "rol" parseando el payload del token.
     * Delegamos en JwtTokenProvider para no duplicar lógica de parsing.
     */
    private String extraerRol(String token) {
        try {
            return jwtTokenProvider.extraerRol(token);
        } catch (Exception e) {
            return "VENDEDOR"; // fallback seguro
        }
    }
}
