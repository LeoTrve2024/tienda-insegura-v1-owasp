package com.tienda.insegura;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la version 1 (VULNERABLE) de la tienda.
 *
 * Ejecutar con: mvn spring-boot:run
 * (requiere Postgres arriba, ver /docker-compose.yml y
 * docs/guia-despliegue.md)
 */
@SpringBootApplication
public class TiendaInseguraApplication {

    public static void main(String[] args) {
        SpringApplication.run(TiendaInseguraApplication.class, args);
    }
}
