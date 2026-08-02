package com.tienda.insegura.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * VULNERABILIDAD A02:2025 - Security Misconfiguration
 *
 * CORS completamente abierto:
 *  - allowedOrigins("*") permite que CUALQUIER origen (cualquier pagina
 *    web, no solo nuestro /frontend) llame a esta API.
 *  - allowedMethods incluye todos los verbos, incluidos los de escritura.
 *  - No hay diferenciacion entre endpoints publicos (catalogo) y
 *    privados (admin, pedidos): la misma politica abierta aplica a todo
 *    bajo /api/**.
 *
 * En un entorno real, esto facilita ataques CSRF-like desde sitios de
 * terceros contra usuarios que tengan un token/sesion valida.
 *
 * En la v2: whitelist explicita del dominio real del frontend + metodos
 * minimos necesarios por recurso.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*");
    }

    /**
     * A08: publica directamente el directorio de uploads. Combinado con la
     * ausencia de validacion en UploadController, cualquier archivo aceptado
     * queda accesible por HTTP en /uploads/{nombre}.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }

}
