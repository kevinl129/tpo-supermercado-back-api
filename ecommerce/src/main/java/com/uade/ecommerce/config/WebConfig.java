package com.uade.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ✅ CORRECCIÓN 1: Configuración de CORS para permitir todos los métodos
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Permite CORS en TODAS las rutas
            .allowedOrigins("http://localhost:5174") 
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // VITAL: Permite todos los métodos necesarios
            .allowedHeaders("*") 
            .allowCredentials(true) 
            .maxAge(3600); 
    }

    // ✅ CORRECCIÓN 2: Configuración para servir archivos estáticos (imágenes)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapea la URL /uploads/** a la carpeta física 'uploads/' en la raíz del proyecto
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}