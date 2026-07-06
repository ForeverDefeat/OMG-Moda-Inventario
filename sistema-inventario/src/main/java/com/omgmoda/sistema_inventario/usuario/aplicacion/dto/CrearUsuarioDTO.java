package com.omgmoda.sistema_inventario.usuario.aplicacion.dto;

import com.omgmoda.sistema_inventario.usuario.dominio.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearUsuarioDTO(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 80, message = "El nombre no puede superar 80 caracteres.")
        String nombre,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo debe tener un formato valido.")
        @Size(max = 100, message = "El correo no puede superar 100 caracteres.")
        String correo,

        @NotBlank(message = "La contrasenia es obligatoria.")
        @Size(min = 6, max = 72, message = "La contrasenia debe tener entre 6 y 72 caracteres.")
        String contrasenia,

        @NotNull(message = "El rol es obligatorio.")
        RolUsuario rol
) {}
