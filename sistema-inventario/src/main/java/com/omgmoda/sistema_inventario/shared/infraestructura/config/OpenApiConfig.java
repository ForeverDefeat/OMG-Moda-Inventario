package com.omgmoda.sistema_inventario.shared.infraestructura.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String JWT_SECURITY_SCHEME = "bearer-jwt";

    @Bean
    public OpenAPI clothWiseOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ClothWise API")
                        .version("1.0.0")
                        .description("API REST del sistema de gestion de inventario OMG MODA.")
                        .contact(new Contact()
                                .name("OMG MODA")
                                .email("admin@omgmoda.com")))
                .addSecurityItem(new SecurityRequirement().addList(JWT_SECURITY_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(
                                JWT_SECURITY_SCHEME,
                                new SecurityScheme()
                                        .name(JWT_SECURITY_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }
}
