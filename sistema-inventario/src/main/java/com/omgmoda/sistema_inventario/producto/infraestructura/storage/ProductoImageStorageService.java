package com.omgmoda.sistema_inventario.producto.infraestructura.storage;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Servicio de aplicacion que coordina reglas de negocio y dependencias para ProductoImageStorageService.
 */
@Service
public class ProductoImageStorageService {

    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final Path uploadDirectory;

    public ProductoImageStorageService(
            @Value("${clothwise.uploads.productos-dir:uploads/productos}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public String store(MultipartFile image) {
        if (image == null || image.isEmpty()) return null;

        if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new DomainException("La imagen no puede superar 5 MB.");
        }

        String extension = extensionOf(image.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new DomainException("Formato de imagen no permitido. Usa JPG, PNG o WEBP.");
        }

        String filename = UUID.randomUUID() + "." + extension;
        Path destination = uploadDirectory.resolve(filename).normalize();

        try {
            Files.createDirectories(uploadDirectory);
            Files.copy(image.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new DomainException("No se pudo guardar la imagen del producto.");
        }

        return "/uploads/productos/" + filename;
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new DomainException("La imagen debe tener extension JPG, PNG o WEBP.");
        }

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            throw new DomainException("La imagen debe tener extension JPG, PNG o WEBP.");
        }

        return originalFilename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
