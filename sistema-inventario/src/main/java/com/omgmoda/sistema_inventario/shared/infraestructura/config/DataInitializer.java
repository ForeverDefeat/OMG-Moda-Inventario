package com.omgmoda.sistema_inventario.shared.infraestructura.config;

import com.omgmoda.sistema_inventario.usuario.dominio.RolUsuario;
import com.omgmoda.sistema_inventario.usuario.dominio.Usuario;
import com.omgmoda.sistema_inventario.usuario.dominio.ports.IUsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inserta datos de prueba al arrancar la aplicación.
 * Solo se ejecuta si la tabla USUARIO está vacía.
 *
 * Usuarios creados:
 *   admin@omgmoda.com  / admin123  → ROL ADMIN
 *   vendedor@omgmoda.com / venta123 → ROL VENDEDOR
 *
 * IMPORTANTE: solo para desarrollo (ddl-auto=create-drop).
 * En producción usar Flyway con scripts SQL versionados.
 */
@Component
public class DataInitializer implements ApplicationRunner {
 
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
 
    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
 
    public DataInitializer(IUsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }
 
    @Override
    public void run(ApplicationArguments args) {
        crearUsuarioSiNoExiste(
                "Administrador OMG MODA",
                "admin@omgmoda.com",
                "admin123",
                RolUsuario.ADMIN
        );
        crearUsuarioSiNoExiste(
                "Vendedor OMG MODA",
                "vendedor@omgmoda.com",
                "venta123",
                RolUsuario.VENDEDOR
        );
    }
 
    private void crearUsuarioSiNoExiste(String nombre,
                                         String correo,
                                         String contraseniaPlana,
                                         RolUsuario rol) {
        if (usuarioRepository.existsByCorreo(correo)) {
            log.info("Usuario ya existe, omitiendo: {}", correo);
            return;
        }
 
        String hash = passwordEncoder.encode(contraseniaPlana);
        Usuario usuario = new Usuario(nombre, correo, hash, rol);
        usuarioRepository.save(usuario);
        log.info("Usuario creado — correo: {} | rol: {}", correo, rol);
    }
}