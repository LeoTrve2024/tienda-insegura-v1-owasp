package com.tienda.insegura.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOTA DE ARQUITECTURA (a proposito, para el informe):
 *
 * Este proyecto NO incluye spring-boot-starter-security. Toda la
 * "autenticacion" vive en AuthService (JWT casero) y la "autorizacion"
 * se resuelve (mal) a mano dentro de cada controller
 * (ver AdminController.estaAutenticado(), que solo valida que el token
 * exista, nunca el rol).
 *
 * Esto es intencional para el laboratorio: al no usar un framework de
 * seguridad maduro, cada endpoint es responsable de "acordarse" de
 * validar sesion y rol -- y varios simplemente lo olvidan, que es
 * exactamente el patron real detras de A01 Broken Access Control y
 * A07 Authentication Failures en aplicaciones legacy.
 *
 * VULNERABILIDAD ADICIONAL A02/A09 a proposito:
 * Al iniciar la aplicacion se imprimen en consola las credenciales
 * del usuario admin semilla (pensado originalmente como ayuda para
 * desarrollo) -- un log que jamas deberia llegar a un ambiente real,
 * y mucho menos versionarse o quedar en un sistema centralizado de logs
 * sin control de acceso.
 */
@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public CommandLineRunner mostrarCredencialesSemilla() {
        return args -> {
            log.info("==================================================");
            log.info(" Usuario admin semilla -> username: admin / password: admin123");
            log.info(" (Ver src/main/resources/db/data.sql)");
            log.info("==================================================");
        };
    }
}
