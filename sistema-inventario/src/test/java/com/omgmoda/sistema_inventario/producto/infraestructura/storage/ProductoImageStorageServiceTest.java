package com.omgmoda.sistema_inventario.producto.infraestructura.storage;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductoImageStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void guardaImagenValidaConRutaPublica() {
        ProductoImageStorageService service = new ProductoImageStorageService(tempDir.toString());
        MockMultipartFile image = new MockMultipartFile(
                "imagen",
                "camisa.PNG",
                "image/png",
                new byte[] { 1, 2, 3 }
        );

        String imageUrl = service.store(image);

        assertThat(imageUrl).startsWith("/uploads/productos/");
        assertThat(imageUrl).endsWith(".png");
        assertThat(Files.exists(tempDir.resolve(Path.of(imageUrl).getFileName()))).isTrue();
    }

    @Test
    void rechazaExtensionNoPermitida() {
        ProductoImageStorageService service = new ProductoImageStorageService(tempDir.toString());
        MockMultipartFile image = new MockMultipartFile(
                "imagen",
                "camisa.gif",
                "image/gif",
                new byte[] { 1, 2, 3 }
        );

        assertThatThrownBy(() -> service.store(image))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Formato");
    }

    @Test
    void rechazaImagenMayorA5Mb() {
        ProductoImageStorageService service = new ProductoImageStorageService(tempDir.toString());
        MockMultipartFile image = new MockMultipartFile(
                "imagen",
                "camisa.jpg",
                "image/jpeg",
                new byte[(5 * 1024 * 1024) + 1]
        );

        assertThatThrownBy(() -> service.store(image))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("5 MB");
    }
}
