package com.omgmoda.sistema_inventario.usuario.aplicacion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Record de entrada para el flujo de login.
 * Usado en el cuerpo del POST /api/v1/auth/login.
 * La contraseña viaja en texto plano sobre HTTPS;
 * el hasheo y la comparación se realizan en AutenticarUseCaseImpl.
 */
public record LoginDTO(

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria.")
        String contrasenia

) {}
