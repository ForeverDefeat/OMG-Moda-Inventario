package com.omgmoda.sistema_inventario.shared.infraestructura.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuración centralizada de Jackson para toda la aplicación.
 *
 * Garantiza que:
 * - Las fechas LocalDateTime se serialicen como ISO-8601 (ej. "2024-11-15T10:30:00")
 *   y no como timestamps numéricos.
 * - Los enums (TipoMovimiento, StockStatus, EstadoVenta) se serialicen
 *   como String legible, no como índice numérico.
 * - Los campos nulos se incluyan en la respuesta JSON para consistencia del frontend.
 */
@Configuration
public class ObjectMapperConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Soporte para java.time (LocalDateTime, LocalDate, etc.)
        mapper.registerModule(new JavaTimeModule());

        // Serializar fechas como ISO-8601 string, no como epoch
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // No fallar si hay propiedades desconocidas en el JSON entrante
        mapper.configure(
                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false
        );

        return mapper;
    }
}
