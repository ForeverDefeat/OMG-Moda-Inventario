package com.omgmoda.sistema_inventario.shared.infraestructura.exception;

import com.omgmoda.sistema_inventario.shared.aplicacion.dto.ErrorResponse;
import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import com.omgmoda.sistema_inventario.shared.dominio.exception.NotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Manejador global de excepciones para todos los controladores REST.
 * Intercepta excepciones del dominio y de infraestructura y las traduce
 * a respuestas HTTP estandarizadas usando ErrorResponse.
 *
 * Mapa de excepciones → códigos HTTP:
 *   DomainException                  → 400 Bad Request
 *   NotFoundException                → 404 Not Found
 *   MethodArgumentNotValidException  → 422 Unprocessable Entity
 *   AuthenticationException          → 401 Unauthorized
 *   AccessDeniedException            → 403 Forbidden
 *   Exception (fallback)             → 500 Internal Server Error
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Regla de negocio violada en el dominio.
     * Ejemplo: stock insuficiente, motivo vacío en ajuste.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            DomainException ex, HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    /**
     * Entidad no encontrada por ID.
     * Ejemplo: variante, venta o usuario inexistente.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            NotFoundException ex, HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "Not Found",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    /**
     * Fallo de validación de @Valid en el cuerpo del request.
     * Retorna la lista de campos inválidos con sus mensajes.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> detalles = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.ofValidation(
                        HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "Unprocessable Entity",
                        "La solicitud contiene campos inválidos.",
                        request.getRequestURI(),
                        detalles
                ));
    }

    /**
     * Token JWT ausente, expirado o inválido.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Unauthorized",
                        "Autenticación requerida: " + ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    /**
     * Usuario autenticado pero sin permisos para el recurso solicitado.
     * Ejemplo: un VENDEDOR intenta acceder a un endpoint solo para ADMIN.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(
                        HttpStatus.FORBIDDEN.value(),
                        "Forbidden",
                        "No tienes permisos para realizar esta acción.",
                        request.getRequestURI()
                ));
    }

    /**
     * Fallback para cualquier excepción no contemplada.
     * Evita exponer stack traces al cliente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "Ocurrió un error inesperado. Contacte al administrador.",
                        request.getRequestURI()
                ));
    }
}
