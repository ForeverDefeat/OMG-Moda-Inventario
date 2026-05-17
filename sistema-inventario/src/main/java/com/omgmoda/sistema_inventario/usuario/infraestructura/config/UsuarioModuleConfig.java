package com.omgmoda.sistema_inventario.usuario.infraestructura.config;

import com.omgmoda.sistema_inventario.usuario.aplicacion.ports.IAutenticarUseCase;
import com.omgmoda.sistema_inventario.usuario.aplicacion.usecases.AutenticarUseCaseImpl;
import com.omgmoda.sistema_inventario.usuario.aplicacion.usecases.IPasswordEncoder;
import com.omgmoda.sistema_inventario.usuario.aplicacion.usecases.IJwtTokenProvider;
import com.omgmoda.sistema_inventario.usuario.dominio.ports.IUsuarioRepository;
import com.omgmoda.sistema_inventario.usuario.infraestructura.adapters.JpaUsuarioAdapter;
import com.omgmoda.sistema_inventario.usuario.infraestructura.adapters.UsuarioJpaRepository;
import com.omgmoda.sistema_inventario.usuario.infraestructura.security.JwtAuthenticationFilter;
import com.omgmoda.sistema_inventario.usuario.infraestructura.security.JwtTokenProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Punto de ensamblado del módulo Usuario.
 * Registra todos los beans necesarios para autenticación y seguridad.
 * Es la única clase que conoce qué implementación concreta
 * se inyecta en cada puerto.
 */
@Configuration
public class UsuarioModuleConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiracion-horas:8}")
    private long jwtExpiracionHoras;

    /**
     * Adaptador de salida: implementa IUsuarioRepository usando Spring Data JPA.
     */
    @Bean
    public IUsuarioRepository usuarioRepository(UsuarioJpaRepository jpaRepository) {
        return new JpaUsuarioAdapter(jpaRepository);
    }

    /**
     * Implementación del proveedor JWT registrada como bean.
     */
    @Bean
    public IJwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider(jwtSecret, jwtExpiracionHoras);
    }

    /**
     * Adaptador de IPasswordEncoder que delega en BCryptPasswordEncoder de Spring.
     * Desacopla el caso de uso de la implementación concreta de BCrypt.
     */
    @Bean
    public IPasswordEncoder passwordEncoderAdapter(PasswordEncoder bCryptEncoder) {
        return new IPasswordEncoder() {
            @Override
            public String encode(String raw) {
                return bCryptEncoder.encode(raw);
            }

            @Override
            public boolean matches(String raw, String hashed) {
                return bCryptEncoder.matches(raw, hashed);
            }
        };
    }

    /**
     * Caso de uso: autenticar usuario y generar token JWT.
     */
    @Bean
    public IAutenticarUseCase autenticarUseCase(IUsuarioRepository usuarioRepository,
                                                IPasswordEncoder passwordEncoder,
                                                IJwtTokenProvider jwtTokenProvider) {
        return new AutenticarUseCaseImpl(usuarioRepository, passwordEncoder, jwtTokenProvider);
    }

    /**
     * Filtro JWT inyectado en la cadena de Spring Security.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(IJwtTokenProvider jwtTokenProvider) {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }
}
