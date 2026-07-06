package com.omgmoda.sistema_inventario.usuario.aplicacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Credenciales de acceso al sistema.")
public record LoginDTO(

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato valido.")
        @Size(max = 100, message = "El correo no puede superar 100 caracteres.")
        @Schema(description = "Correo registrado del usuario.", example = "admin@omgmoda.com")
        String correo,

        @NotBlank(message = "La contrasenia es obligatoria.")
        @Size(min = 6, max = 72, message = "La contrasenia debe tener entre 6 y 72 caracteres.")
        @Schema(description = "Contrasenia en texto plano enviada por HTTPS.", example = "admin123")
        String contrasenia

) {}
