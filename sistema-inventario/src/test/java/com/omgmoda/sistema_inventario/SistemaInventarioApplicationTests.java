package com.omgmoda.sistema_inventario;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class SistemaInventarioApplicationTests {

	@Test
	void aplicacionDeclaraEntradaSpringBoot() {
		assertThat(SistemaInventarioApplication.class)
				.hasAnnotation(SpringBootApplication.class);
	}

}
