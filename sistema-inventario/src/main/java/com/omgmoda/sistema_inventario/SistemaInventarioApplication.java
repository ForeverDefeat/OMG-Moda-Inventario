package com.omgmoda.sistema_inventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SistemaInventarioApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemaInventarioApplication.class, args);
		System.out.println("¡Bienvenido al Sistema de Inventario de OMG Moda!");
	}

}
