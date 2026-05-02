package com.omgmoda.sistema_inventario.prueba;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class controller {
    @GetMapping("/")
        String holaMundo() {
            return "¡Hola, mundo!";
        }

}
