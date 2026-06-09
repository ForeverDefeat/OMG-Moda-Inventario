package com.omgmoda.sistema_inventario.usuario.infraestructura.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración central de Spring Security.
 *
 * Reglas de acceso:
 *   POST /api/v1/auth/login     → público (sin token)
 *   GET  /api/v1/productos/**   → ADMIN y VENDEDOR
 *   POST /api/v1/productos/**   → solo ADMIN
 *   GET  /api/v1/movimientos/** → ADMIN y VENDEDOR
 *   POST /api/v1/movimientos/** → solo ADMIN
 *   POST /api/v1/ventas/**      → ADMIN y VENDEDOR
 *   GET  /api/v1/ventas/**      → ADMIN y VENDEDOR
 *   GET  /api/v1/reportes/**    → solo ADMIN
 *   Cualquier otra ruta         → requiere autenticación
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF (API REST stateless con JWT)
            .csrf(AbstractHttpConfigurer::disable)

            // Configurar CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Sin sesiones: cada request se autentica con el token
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Reglas de autorización por ruta
            .authorizeHttpRequests(auth -> auth
                    // Linea para testear el error 404 sin autenticación, se puede eliminar después
                    .requestMatchers("/error").permitAll()
                    .requestMatchers(
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/v3/api-docs",
                            "/v3/api-docs/**"
                    ).permitAll()

                    // Login público
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()

                    // Productos
                    .requestMatchers(HttpMethod.GET, "/api/v1/productos/**")
                        .hasAnyRole("ADMIN", "VENDEDOR")
                    .requestMatchers(HttpMethod.POST, "/api/v1/productos/**")
                        .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/productos/**")
                        .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/productos/**")
                        .hasRole("ADMIN")

                    // Movimientos de inventario
                    .requestMatchers(HttpMethod.GET, "/api/v1/movimientos/**")
                        .hasAnyRole("ADMIN", "VENDEDOR")
                    .requestMatchers(HttpMethod.POST, "/api/v1/movimientos/**")
                        .hasRole("ADMIN")

                    // Ventas
                    .requestMatchers("/api/v1/ventas/**")
                        .hasAnyRole("ADMIN", "VENDEDOR")

                    // Reportes
                    .requestMatchers("/api/v1/reportes/**")
                        .hasRole("ADMIN")

                    // Cualquier otra ruta requiere autenticación
                    .anyRequest().authenticated()
            )

            // Insertar el filtro JWT antes del filtro estándar de autenticación
            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:5173", "https://*.omgmoda.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
