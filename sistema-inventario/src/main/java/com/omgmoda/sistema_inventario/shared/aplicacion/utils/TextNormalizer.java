package com.omgmoda.sistema_inventario.shared.aplicacion.utils;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import org.apache.commons.lang3.StringUtils;

/**
 * Utilidad central para normalizar textos de entrada antes de construir
 * entidades de dominio. Usa Apache Commons Lang para evitar trims dispersos.
 */
public final class TextNormalizer {

    private TextNormalizer() {
    }

    public static String normalizeRequired(String value, String fieldName) {
        String normalized = normalizeOptional(value);
        if (StringUtils.isBlank(normalized)) {
            throw new DomainException(fieldName + " es obligatorio.");
        }
        return normalized;
    }

    public static String normalizeOptional(String value) {
        return StringUtils.normalizeSpace(StringUtils.stripToEmpty(value));
    }
}
