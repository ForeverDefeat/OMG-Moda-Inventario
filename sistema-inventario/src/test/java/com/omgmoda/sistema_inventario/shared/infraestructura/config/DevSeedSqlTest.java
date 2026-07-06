package com.omgmoda.sistema_inventario.shared.infraestructura.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class DevSeedSqlTest {

    @Test
    void seedIncluyeVolumenMinimoParaDemoYReportes() throws Exception {
        String sql = new String(
                new ClassPathResource("db/dev-seed.sql").getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        assertThat(countRows(sql, "producto")).isBetween(35, 50);
        assertThat(countRows(sql, "variante_producto")).isBetween(70, 120);
        assertThat(countRows(sql, "cliente")).isBetween(20, 30);
        assertThat(countRows(sql, "venta")).isGreaterThanOrEqualTo(30);
        assertThat(countRows(sql, "detalle_venta")).isGreaterThanOrEqualTo(60);
        assertThat(sql).contains("CURRENT_DATE()", "INTERVAL 29 DAY", "INSERT INTO movimiento");
    }

    private int countRows(String sql, String table) {
        String insertPrefix = "INSERT INTO " + table;
        int start = sql.indexOf(insertPrefix);
        assertThat(start).as("No se encontro insert para " + table).isNotNegative();
        int values = sql.indexOf("VALUES", start);
        assertThat(values).as("No se encontro VALUES para " + table).isNotNegative();
        int end = sql.indexOf(";", values);
        assertThat(end).as("No se encontro fin de sentencia para " + table).isNotNegative();

        String valuesBlock = sql.substring(values, end);
        int rows = 0;
        for (String line : valuesBlock.split("\\R")) {
            if (line.stripLeading().startsWith("(")) {
                rows++;
            }
        }
        return rows;
    }
}
