package com.omgmoda.sistema_inventario.usuario.infraestructura.entities;

import com.omgmoda.sistema_inventario.usuario.dominio.RolUsuario;
import jakarta.persistence.*;

/**
 * Entidad JPA que mapea la tabla USUARIO (3FN).
 * Completamente separada de la entidad de dominio Usuario.
 * Solo la capa de infraestructura conoce esta clase.
 */
@Entity
@Table(name = "USUARIO")
public class UsuarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "correo", nullable = false, unique = true, length = 100)
    private String correo;

    @Column(name = "contrasenia", nullable = false, length = 255)
    private String contrasenia;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 10)
    private RolUsuario rol;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    // Constructor vacío requerido por JPA
    public UsuarioJpaEntity() {}

    // ── Getters y Setters ──────────────────────────────────────────────────────

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }
    public String getNombre()                    { return nombre; }
    public void setNombre(String nombre)         { this.nombre = nombre; }
    public String getCorreo()                    { return correo; }
    public void setCorreo(String correo)         { this.correo = correo; }
    public String getContrasenia()               { return contrasenia; }
    public void setContrasenia(String c)         { this.contrasenia = c; }
    public RolUsuario getRol()                   { return rol; }
    public void setRol(RolUsuario rol)           { this.rol = rol; }
    public boolean isActivo()                    { return activo; }
    public void setActivo(boolean activo)        { this.activo = activo; }
}
