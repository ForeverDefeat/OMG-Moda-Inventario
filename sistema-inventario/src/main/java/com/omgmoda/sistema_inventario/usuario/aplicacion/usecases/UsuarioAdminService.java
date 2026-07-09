package com.omgmoda.sistema_inventario.usuario.aplicacion.usecases;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import com.omgmoda.sistema_inventario.shared.dominio.exception.NotFoundException;
import com.omgmoda.sistema_inventario.usuario.aplicacion.dto.CrearUsuarioDTO;
import com.omgmoda.sistema_inventario.usuario.aplicacion.dto.UsuarioResponseDTO;
import com.omgmoda.sistema_inventario.usuario.dominio.RolUsuario;
import com.omgmoda.sistema_inventario.usuario.dominio.Usuario;
import com.omgmoda.sistema_inventario.usuario.dominio.ports.IUsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de aplicacion que coordina reglas de negocio y dependencias para UsuarioAdminService.
 */
@Service
public class UsuarioAdminService {

    private final IUsuarioRepository usuarioRepository;
    private final IPasswordEncoder passwordEncoder;

    public UsuarioAdminService(IUsuarioRepository usuarioRepository, IPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UsuarioResponseDTO crear(CrearUsuarioDTO dto) {
        String correo = normalizarCorreo(dto.correo());
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new DomainException("Ya existe un usuario registrado con ese correo.");
        }

        Usuario usuario = new Usuario(
                dto.nombre().trim(),
                correo,
                passwordEncoder.encode(dto.contrasenia()),
                dto.rol(),
                true
        );
        return toResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO cambiarRol(Long id, RolUsuario rol, Long idUsuarioActual) {
        Usuario usuario = buscar(id);
        if (usuario.esAdmin() && rol != RolUsuario.ADMIN) {
            validarNoEsUltimoAdminActivo(usuario, idUsuarioActual);
        }
        usuario.cambiarRol(rol);
        return toResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO resetearContrasenia(Long id, String nuevaContrasenia) {
        Usuario usuario = buscar(id);
        usuario.actualizarContrasenia(passwordEncoder.encode(nuevaContrasenia));
        return toResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO desactivar(Long id, Long idUsuarioActual) {
        Usuario usuario = buscar(id);
        validarNoEsUltimoAdminActivo(usuario, idUsuarioActual);
        usuario.desactivar();
        return toResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO reactivar(Long id) {
        Usuario usuario = buscar(id);
        usuario.reactivar();
        return toResponse(usuarioRepository.save(usuario));
    }

    private void validarNoEsUltimoAdminActivo(Usuario usuario, Long idUsuarioActual) {
        if (!usuario.esAdmin() || !usuario.isActivo()) {
            return;
        }
        long adminsActivos = usuarioRepository.countByRolAndActivoTrue(RolUsuario.ADMIN);
        if (adminsActivos <= 1) {
            throw new DomainException("No se puede dejar el sistema sin administradores activos.");
        }
        if (idUsuarioActual != null && idUsuarioActual.equals(usuario.getId()) && adminsActivos <= 1) {
            throw new DomainException("No puedes quitar tus propias credenciales si eres el unico admin activo.");
        }
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + id));
    }

    private UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.isActivo()
        );
    }

    private String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase();
    }
}
