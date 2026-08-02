package com.tienda.insegura.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * VULNERABILIDAD A02:2025 - Security Misconfiguration
 *
 * Esta clase, sumada a application.properties
 * (management.endpoints.web.exposure.include=*), deja disponibles SIN
 * autenticacion endpoints como:
 *   GET /actuator/env      -> variables de entorno (incluye password de BD)
 *   GET /actuator/beans    -> arbol completo de beans de Spring
 *   GET /actuator/heapdump -> volcado de memoria del proceso
 *   GET /actuator/info     -> info "custom" que agregamos aqui abajo
 *
 * Para hacerlo aun mas evidente en el pentest, este InfoContributor
 * publica a proposito datos sensibles (host y usuario de BD) en
 * GET /actuator/info, algo que jamas deberia exponerse publicamente.
 *
 * En la v2: management.endpoints.web.exposure.include=health,info
 * (minimo indispensable) + autenticacion dedicada para actuator.
 */
@Configuration
public class ActuatorConfig {

    @Value("${spring.datasource.url:desconocida}")
    private String dbUrl;

    @Value("${spring.datasource.username:desconocido}")
    private String dbUser;

    @Bean
    public InfoContributor infoContributorInseguro() {
        return (Info.Builder builder) -> builder.withDetail("infraestructura", Map.of(
                "db_url", dbUrl,
                "db_user", dbUser,
                "nota", "Este detalle NO deberia ser publico (A02)"
        ));
    }
}
