package com.omgmoda.sistema_inventario.usuario.aplicacion.usecases;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import com.omgmoda.sistema_inventario.shared.dominio.exception.NotFoundException;
import com.omgmoda.sistema_inventario.usuario.aplicacion.dto.AuthResponseDTO;
import com.omgmoda.sistema_inventario.usuario.aplicacion.dto.LoginDTO;
import com.omgmoda.sistema_inventario.usuario.aplicacion.ports.IAutenticarUseCase;
import com.omgmoda.sistema_inventario.usuario.dominio.Usuario;
import com.omgmoda.sistema_inventario.usuario.dominio.ports.IUsuarioRepository;

import java.time.LocalDateTime;

/**
 * Implementación del caso de uso: Autenticar Usuario (RNF01).
 *
 * Flujo:
 * 1. Busca el usuario por correo en IUsuarioRepository.
 * 2. Verifica la contraseña usando el PasswordEncoder inyectado.
 * 3. Genera el token JWT mediante IJwtTokenProvider.
 * 4. Retorna AuthResponseDTO con token, rol, nombre y expiración.
 *
 * No importa ninguna clase de Spring Security directamente;
 * usa interfaces para mantener el caso de uso libre de infraestructura.
 */
public class AutenticarUseCaseImpl implements IAutenticarUseCase {

    private final IUsuarioRepository usuarioRepository;
    private final IPasswordEncoder passwordEncoder;
    private final IJwtTokenProvider jwtTokenProvider;

    public AutenticarUseCaseImpl(IUsuarioRepository usuarioRepository,
                                 IPasswordEncoder passwordEncoder,
                                 IJwtTokenProvider jwtTokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AuthResponseDTO autenticar(LoginDTO dto) {

        // 1. Buscar usuario por correo
        Usuario usuario = usuarioRepository
                .findByCorreo(dto.correo())
                .orElseThrow(() -> new NotFoundException(
                        "No existe un usuario registrado con el correo: " + dto.correo()
                ));

        if (!usuario.isActivo()) {
            throw new DomainException("El usuario se encuentra inactivo. Solicita reactivacion a un administrador.");
        }

        // 2. Verificar contraseña contra el hash almacenado
        if (!passwordEncoder.matches(dto.contrasenia(), usuario.getContrasenia()))
            throw new DomainException("Las credenciales proporcionadas son incorrectas.");

        // 3. Generar token JWT
        String token = jwtTokenProvider.generarToken(
                usuario.getCorreo(),
                usuario.getRol().name()
        );

        LocalDateTime expiracion = jwtTokenProvider.obtenerExpiracion(token);

        // 4. Retornar respuesta
        return AuthResponseDTO.of(
                token,
                usuario.getRol(),
                usuario.getNombre(),
                usuario.getCorreo(),
                expiracion
        );
    }
}
