package com.omgmoda.sistema_inventario.shared.aplicacion.validation;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;

import java.math.BigDecimal;

/**
 * Métodos auxiliares para validaciones técnicas reutilizables
 * en cualquier módulo de la capa de aplicación.
 *
 * No contiene lógica de negocio específica de un módulo.
 * Las validaciones de negocio viven en las entidades de dominio.
 */
public final class ValidationUtils {

    private ValidationUtils() {
        // Clase utilitaria — no instanciable
    }

    /**
     * Lanza DomainException si el texto es nulo o vacío.
     * @param valor  texto a validar.
     * @param campo  nombre del campo para el mensaje de error.
     */
    public static void requerirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank())
            throw new DomainException("El campo '" + campo + "' es obligatorio y no puede estar vacío.");
    }

    /**
     * Lanza DomainException si el valor es nulo.
     * @param valor  objeto a validar.
     * @param campo  nombre del campo para el mensaje de error.
     */
    public static void requerirNoNulo(Object valor, String campo) {
        if (valor == null)
            throw new DomainException("El campo '" + campo + "' es obligatorio.");
    }

    /**
     * Lanza DomainException si el decimal es nulo, negativo o cero.
     * @param valor  BigDecimal a validar.
     * @param campo  nombre del campo para el mensaje de error.
     */
    public static void requerirPositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0)
            throw new DomainException("El campo '" + campo + "' debe ser mayor a cero.");
    }

    /**
     * Lanza DomainException si el entero es negativo.
     * Permite cero (útil para ajustes de stock).
     * @param valor  entero a validar.
     * @param campo  nombre del campo para el mensaje de error.
     */
    public static void requerirNoNegativo(int valor, String campo) {
        if (valor < 0)
            throw new DomainException("El campo '" + campo + "' no puede ser negativo.");
    }

    /**
     * Lanza DomainException si el entero no es estrictamente mayor a cero.
     * @param valor  entero a validar.
     * @param campo  nombre del campo para el mensaje de error.
     */
    public static void requerirMayorACero(int valor, String campo) {
        if (valor <= 0)
            throw new DomainException("El campo '" + campo + "' debe ser mayor a cero.");
    }

    /**
     * Lanza DomainException si el texto supera la longitud máxima permitida.
     * @param valor     texto a validar.
     * @param campo     nombre del campo para el mensaje de error.
     * @param maxLength longitud máxima permitida.
     */
    public static void requerirLongitudMaxima(String valor, String campo, int maxLength) {
        if (valor != null && valor.length() > maxLength)
            throw new DomainException(
                    "El campo '" + campo + "' no puede superar " + maxLength + " caracteres."
            );
    }
}
