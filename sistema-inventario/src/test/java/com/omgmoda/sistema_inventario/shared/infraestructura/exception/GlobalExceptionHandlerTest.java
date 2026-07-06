package com.omgmoda.sistema_inventario.shared.infraestructura.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    @Test
    void jsonMalformadoRetornaBadRequest() {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        var response = handler.handleHttpMessageNotReadableException(
                new HttpMessageNotReadableException("JSON invalido", mock(HttpInputMessage.class)),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("formato valido");
    }

    @Test
    void parametroInvalidoRetornaBadRequest() {
        when(request.getRequestURI()).thenReturn("/api/v1/ventas");
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "INVALIDO",
                null,
                "estado",
                null,
                new IllegalArgumentException("Estado invalido")
        );

        var response = handler.handleBadRequestException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("parametros invalidos");
    }
}
