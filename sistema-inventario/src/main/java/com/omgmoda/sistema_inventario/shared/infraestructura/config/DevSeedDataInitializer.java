package com.omgmoda.sistema_inventario.shared.infraestructura.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Order(2)
public class DevSeedDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevSeedDataInitializer.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final boolean seedEnabled;

    public DevSeedDataInitializer(DataSource dataSource,
                                  JdbcTemplate jdbcTemplate,
                                  @Value("${clothwise.seed.enabled:true}") boolean seedEnabled) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.seedEnabled = seedEnabled;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            log.info("Seed de desarrollo deshabilitado por configuracion.");
            return;
        }

        Integer productos = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM producto", Integer.class);
        if (productos != null && productos > 0) {
            log.info("Seed de desarrollo omitido: ya existen {} productos.", productos);
            return;
        }

        ResourceDatabasePopulator populator =
                new ResourceDatabasePopulator(new ClassPathResource("db/dev-seed.sql"));
        populator.execute(dataSource);
        log.info("Seed de desarrollo cargado desde db/dev-seed.sql.");
    }
}
