package com.uade.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Permite CORS en TODAS las rutas de tu API
            .allowedOrigins("http://localhost:5174") 
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // Permite todos los métodos
            .allowedHeaders("*") // Permite todos los headers (incluido Authorization)
            .allowCredentials(true) // Permite enviar cookies y headers de auth
            .maxAge(3600); 
    }
}