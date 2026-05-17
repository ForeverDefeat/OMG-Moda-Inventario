package com.omgmoda.sistema_inventario.usuario.dominio;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;

/**
 * Aggregate Root del módulo Usuario.
 * Gestiona las credenciales y el rol de acceso al sistema.
 * Corresponde a la tabla USUARIO (3FN).
 *
 * La contraseña siempre se almacena como hash BCrypt;
 * nunca en texto plano. La validación de credenciales
 * se delega a AutenticarUseCaseImpl mediante el puerto IUsuarioRepository.
 */
public class Usuario {

    private Long id;
    private String nombre;
    private String correo;
    private String contrasenia; // hash BCrypt
    private RolUsuario rol;

    // Constructor para creación nueva
    public Usuario(String nombre, String correo, String contrasenia, RolUsuario rol) {
        validar(nombre, correo, contrasenia, rol);
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.rol = rol;
    }

    // Constructor para reconstitución desde persistencia
    public Usuario(Long id,
                   String nombre,
                   String correo,
                   String contrasenia,
                   RolUsuario rol) {
        validar(nombre, correo, contrasenia, rol);
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.rol = rol;
    }

    // ── Reglas de negocio ──────────────────────────────────────────────────────

    /**
     * Verifica si el usuario tiene el rol de administrador.
     */
    public boolean esAdmin() {
        return RolUsuario.ADMIN.equals(this.rol);
    }

    /**
     * Verifica si el usuario tiene el rol de vendedor.
     */
    public boolean esVendedor() {
        return RolUsuario.VENDEDOR.equals(this.rol);
    }

    /**
     * Actualiza la contraseña ya hasheada.
     * Nunca recibe texto plano: el hasheo ocurre en la capa de aplicación.
     */
    public void actualizarContrasenia(String nuevaContraseniaHash) {
        if (nuevaContraseniaHash == null || nuevaContraseniaHash.isBlank())
            throw new DomainException("La nueva contraseña no puede estar vacía.");
        this.contrasenia = nuevaContraseniaHash;
    }

    // ── Validación interna ─────────────────────────────────────────────────────

    private void validar(String nombre, String correo, String contrasenia, RolUsuario rol) {
        if (nombre == null || nombre.isBlank())
            throw new DomainException("El nombre del usuario no puede estar vacío.");
        if (correo == null || correo.isBlank())
            throw new DomainException("El correo del usuario no puede estar vacío.");
        if (!correo.contains("@"))
            throw new DomainException("El correo no tiene un formato válido.");
        if (contrasenia == null || contrasenia.isBlank())
            throw new DomainException("La contraseña no puede estar vacía.");
        if (rol == null)
            throw new DomainException("El rol del usuario es obligatorio.");
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public Long getId()             { return id; }
    public String getNombre()       { return nombre; }
    public String getCorreo()       { return correo; }
    public String getContrasenia()  { return contrasenia; }
    public RolUsuario getRol()      { return rol; }

    public void setId(Long id)      { this.id = id; }
}
