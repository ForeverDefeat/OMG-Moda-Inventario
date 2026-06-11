package com.omgmoda.sistema_inventario.producto.infraestructura.controllers;

import com.omgmoda.sistema_inventario.producto.aplicacion.dto.CrearProductoDTO;
import com.omgmoda.sistema_inventario.producto.aplicacion.ports.IBuscarVariantesUseCase;
import com.omgmoda.sistema_inventario.producto.aplicacion.ports.IRegistrarProductoUseCase;
import com.omgmoda.sistema_inventario.producto.infraestructura.storage.ProductoImageStorageService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductoRestControllerTest {

    private final IRegistrarProductoUseCase registrarProductoUseCase = mock(IRegistrarProductoUseCase.class);
    private final IBuscarVariantesUseCase buscarVariantesUseCase = mock(IBuscarVariantesUseCase.class);
    private final ProductoImageStorageService imageStorageService = mock(ProductoImageStorageService.class);
    private final ProductoRestController controller = new ProductoRestController(
            registrarProductoUseCase,
            buscarVariantesUseCase,
            imageStorageService
    );

    @Test
    void crearProductoJsonConservaImageUrl() {
        CrearProductoDTO dto = dto("https://cdn.example.com/camisa.webp");
        when(registrarProductoUseCase.registrar(any(CrearProductoDTO.class))).thenReturn(List.of());

        controller.crearProducto(dto);

        ArgumentCaptor<CrearProductoDTO> captor = ArgumentCaptor.forClass(CrearProductoDTO.class);
        verify(registrarProductoUseCase).registrar(captor.capture());
        assertThat(captor.getValue().imageUrl()).isEqualTo("https://cdn.example.com/camisa.webp");
    }

    @Test
    void crearProductoMultipartGuardaImagenYUsaRutaPublica() {
        MockMultipartFile imagen = new MockMultipartFile(
                "imagen",
                "camisa.webp",
                "image/webp",
                new byte[] { 1, 2, 3 }
        );
        when(imageStorageService.store(imagen)).thenReturn("/uploads/productos/camisa.webp");
        when(registrarProductoUseCase.registrar(any(CrearProductoDTO.class))).thenReturn(List.of());

        controller.crearProductoConImagen(dto(null), imagen);

        ArgumentCaptor<CrearProductoDTO> captor = ArgumentCaptor.forClass(CrearProductoDTO.class);
        verify(registrarProductoUseCase).registrar(captor.capture());
        assertThat(captor.getValue().imageUrl()).isEqualTo("/uploads/productos/camisa.webp");
    }

    private CrearProductoDTO dto(String imageUrl) {
        return new CrearProductoDTO(
                "Camisa Oxford",
                "Camisas",
                "OMG MODA",
                imageUrl,
                List.of(new CrearProductoDTO.VarianteDTO(
                        "M",
                        "Azul",
                        "Algodon",
                        BigDecimal.valueOf(45),
                        BigDecimal.valueOf(89.90)
                ))
        );
    }
}
