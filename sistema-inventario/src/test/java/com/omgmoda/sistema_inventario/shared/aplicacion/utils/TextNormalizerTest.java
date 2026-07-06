package com.omgmoda.sistema_inventario.shared.aplicacion.utils;

import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TextNormalizerTest {

    @Test
    void normalizeOptionalLimpiaEspaciosDuplicados() {
        assertThat(TextNormalizer.normalizeOptional("  Camisa   Oxford  "))
                .isEqualTo("Camisa Oxford");
    }

    @Test
    void normalizeRequiredRechazaTextoVacio() {
        assertThatThrownBy(() -> TextNormalizer.normalizeRequired("   ", "La categoria"))
                .isInstanceOf(DomainException.class)
                .hasMessage("La categoria es obligatorio.");
    }
}
