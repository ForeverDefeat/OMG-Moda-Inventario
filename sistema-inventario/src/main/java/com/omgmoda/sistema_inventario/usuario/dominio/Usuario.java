package com.omgmoda.sistema_inventario.usuario.dominio;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;

/**
 * Aggregate Root del modulo Usuario.
 * Gestiona credenciales, rol y estado de acceso al sistema.
 */
public class Usuario {

    private Long id;
    private String nombre;
    private String correo;
    private String contrasenia;
    private RolUsuario rol;
    private boolean activo;

    public Usuario(String nombre, String correo, String contrasenia, RolUsuario rol) {
        this(nombre, correo, contrasenia, rol, true);
    }

    public Usuario(String nombre, String correo, String contrasenia, RolUsuario rol, boolean activo) {
        validar(nombre, correo, contrasenia, rol);
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.rol = rol;
        this.activo = activo;
    }

    public Usuario(Long id,
                   String nombre,
                   String correo,
                   String contrasenia,
                   RolUsuario rol) {
        this(id, nombre, correo, contrasenia, rol, true);
    }

    public Usuario(Long id,
                   String nombre,
                   String correo,
                   String contrasenia,
                   RolUsuario rol,
                   boolean activo) {
        validar(nombre, correo, contrasenia, rol);
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.rol = rol;
        this.activo = activo;
    }

    public boolean esAdmin() {
        return RolUsuario.ADMIN.equals(this.rol);
    }

    public boolean esVendedor() {
        return RolUsuario.VENDEDOR.equals(this.rol);
    }

    public void actualizarContrasenia(String nuevaContraseniaHash) {
        if (nuevaContraseniaHash == null || nuevaContraseniaHash.isBlank()) {
            throw new DomainException("La nueva contrasenia no puede estar vacia.");
        }
        this.contrasenia = nuevaContraseniaHash;
    }

    public void cambiarRol(RolUsuario nuevoRol) {
        if (nuevoRol == null) {
            throw new DomainException("El rol del usuario es obligatorio.");
        }
        this.rol = nuevoRol;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void reactivar() {
        this.activo = true;
    }

    private void validar(String nombre, String correo, String contrasenia, RolUsuario rol) {
        if (nombre == null || nombre.isBlank()) {
            throw new DomainException("El nombre del usuario no puede estar vacio.");
        }
        if (correo == null || correo.isBlank()) {
            throw new DomainException("El correo del usuario no puede estar vacio.");
        }
        if (!correo.contains("@")) {
            throw new DomainException("El correo no tiene un formato valido.");
        }
        if (contrasenia == null || contrasenia.isBlank()) {
            throw new DomainException("La contrasenia no puede estar vacia.");
        }
        if (rol == null) {
            throw new DomainException("El rol del usuario es obligatorio.");
        }
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getContrasenia() { return contrasenia; }
    public RolUsuario getRol() { return rol; }
    public boolean isActivo() { return activo; }

    public void setId(Long id) { this.id = id; }
}
