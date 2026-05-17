package com.omgmoda.sistema_inventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal de la aplicación ClothWise.
 *
 * @SpringBootApplication activa:
 *   - @Configuration       : registra esta clase como fuente de beans.
 *   - @EnableAutoConfiguration : configura automáticamente Spring según
 *                               las dependencias presentes (JPA, Security, Web).
 *   - @ComponentScan       : escanea todos los paquetes bajo com.omgmoda,
 *                            encontrando automáticamente todos los
 *                            @Configuration, @RestController y @Repository
 *                            de los módulos producto, inventario, venta y usuario.
 *
 * No se necesita ninguna configuración adicional aquí:
 * cada módulo tiene su propio *ModuleConfig que registra sus beans.
 */

@SpringBootApplication
public class SistemaInventarioApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemaInventarioApplication.class, args);


		System.out.println("		 _______                   _____                    _____                                    _____                   _______                   _____                    _____          \r\n" + //
						"        /::\\    \\                 /\\    \\                  /\\    \\                                  /\\    \\                 /::\\    \\                 /\\    \\                  /\\    \\         \r\n" + //
						"       /::::\\    \\               /::\\____\\                /::\\    \\                                /::\\____\\               /::::\\    \\               /::\\    \\                /::\\    \\        \r\n" + //
						"      /::::::\\    \\             /::::|   |               /::::\\    \\                              /::::|   |              /::::::\\    \\             /::::\\    \\              /::::\\    \\       \r\n" + //
						"     /::::::::\\    \\           /:::::|   |              /::::::\\    \\                            /:::::|   |             /::::::::\\    \\           /::::::\\    \\            /::::::\\    \\      \r\n" + //
						"    /:::/~~\\:::\\    \\         /::::::|   |             /:::/\\:::\\    \\                          /::::::|   |            /:::/~~\\:::\\    \\         /:::/\\:::\\    \\          /:::/\\:::\\    \\     \r\n" + //
						"   /:::/    \\:::\\    \\       /:::/|::|   |            /:::/  \\:::\\    \\                        /:::/|::|   |           /:::/    \\:::\\    \\       /:::/  \\:::\\    \\        /:::/__\\:::\\    \\    \r\n" + //
						"  /:::/    / \\:::\\    \\     /:::/ |::|   |           /:::/    \\:::\\    \\                      /:::/ |::|   |          /:::/    / \\:::\\    \\     /:::/    \\:::\\    \\      /::::\\   \\:::\\    \\   \r\n" + //
						" /:::/____/   \\:::\\____\\   /:::/  |::|___|______    /:::/    / \\:::\\    \\                    /:::/  |::|___|______   /:::/____/   \\:::\\____\\   /:::/    / \\:::\\    \\    /::::::\\   \\:::\\    \\  \r\n" + //
						"|:::|    |     |:::|    | /:::/   |::::::::\\    \\  /:::/    /   \\:::\\ ___\\                  /:::/   |::::::::\\    \\ |:::|    |     |:::|    | /:::/    /   \\:::\\ ___\\  /:::/\\:::\\   \\:::\\    \\ \r\n" + //
						"|:::|____|     |:::|    |/:::/    |:::::::::\\____\\/:::/____/  ___\\:::|    |                /:::/    |:::::::::\\____\\|:::|____|     |:::|    |/:::/____/     \\:::|    |/:::/  \\:::\\   \\:::\\____\\\r\n" + //
						" \\:::\\    \\   /:::/    / \\::/    / ~~~~~/:::/    /\\:::\\    \\ /\\  /:::|____|                \\::/    / ~~~~~/:::/    / \\:::\\    \\   /:::/    / \\:::\\    \\     /:::|____|\\::/    \\:::\\  /:::/    /\r\n" + //
						"  \\:::\\    \\ /:::/    /   \\/____/      /:::/    /  \\:::\\    /::\\ \\::/    /                  \\/____/      /:::/    /   \\:::\\    \\ /:::/    /   \\:::\\    \\   /:::/    /  \\/____/ \\:::\\/:::/    / \r\n" + //
						"   \\:::\\    /:::/    /                /:::/    /    \\:::\\   \\:::\\ \\/____/                               /:::/    /     \\:::\\    /:::/    /     \\:::\\    \\ /:::/    /            \\::::::/    /  \r\n" + //
						"    \\:::\\__/:::/    /                /:::/    /      \\:::\\   \\:::\\____\\                                /:::/    /       \\:::\\__/:::/    /       \\:::\\    /:::/    /              \\::::/    /   \r\n" + //
						"     \\::::::::/    /                /:::/    /        \\:::\\  /:::/    /                               /:::/    /         \\::::::::/    /         \\:::\\  /:::/    /               /:::/    /    \r\n" + //
						"      \\::::::/    /                /:::/    /          \\:::\\/:::/    /                               /:::/    /           \\::::::/    /           \\:::\\/:::/    /               /:::/    /     \r\n" + //
						"       \\::::/    /                /:::/    /            \\::::::/    /                               /:::/    /             \\::::/    /             \\::::::/    /               /:::/    /      \r\n" + //
						"        \\::/____/                /:::/    /              \\::::/    /                               /:::/    /               \\::/____/               \\::::/    /               /:::/    /       \r\n" + //
						"         ~~                      \\::/    /                \\::/____/                                \\::/    /                 ~~                      \\::/____/                \\::/    /        \r\n" + //
						"                                  \\/____/                                                           \\/____/                                           ~~                       \\/____/         \r\n" + //
						"                                                                                                                                                                                               ");
        System.out.println("\n\n\n\t\t\t\t\t\t\t\t  ClothWise Inventory & POS Management v1.0.0");
        System.out.println("\t\t\t\t\t\t\t\t       Running on: http://localhost:8080 \n\n\n");

		System.out.println(	"\t\t\t\t\t\t _____ _       _   _     _    _ _              _____           _                 \r\n" + //
							"\t\t\t\t\t\t/  __ \\ |     | | | |   | |  | (_)            /  ___|         | |                \r\n" + //
							"\t\t\t\t\t\t| /  \\/ | ___ | |_| |__ | |  | |_ ___  ___    \\ `--. _   _ ___| |_ ___ _ __ ___  \r\n" + //
							"\t\t\t\t\t\t| |   | |/ _ \\| __| '_ \\| |/\\| | / __|/ _ \\    `--. \\ | | / __| __/ _ \\ '_ ` _ \\ \r\n" + //
							"\t\t\t\t\t\t| \\__/\\ | (_) | |_| | | \\  /\\  / \\__ \\  __/   /\\__/ / |_| \\__ \\ ||  __/ | | | | |\r\n" + //
							"\t\t\t\t\t\t \\____/_|\\___/ \\__|_| |_|\\/  \\/|_|___/\\___|   \\____/ \\__, |___/\\__\\___|_| |_| |_|\r\n" + //
							"\t\t\t\t\t\t                                                      __/ |                      \r\n" + //
							"\t\t\t\t\t\t                                                     |___/                       ");
    
		/*
		url: 	http://localhost:8080/api/v1/auth/login
		json:	{ "correo": "admin@omgmoda.com", "contrasenia": "admin123" }
		*/
	}

}
