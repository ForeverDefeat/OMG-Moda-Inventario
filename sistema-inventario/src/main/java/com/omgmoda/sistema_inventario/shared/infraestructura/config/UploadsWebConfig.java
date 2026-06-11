package com.omgmoda.sistema_inventario.shared.infraestructura.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class UploadsWebConfig implements WebMvcConfigurer {

    private final String productosUploadDir;

    public UploadsWebConfig(
            @Value("${clothwise.uploads.productos-dir:uploads/productos}") String productosUploadDir) {
        this.productosUploadDir = productosUploadDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(productosUploadDir)
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();

        registry.addResourceHandler("/uploads/productos/**")
                .addResourceLocations(location);
    }
}
