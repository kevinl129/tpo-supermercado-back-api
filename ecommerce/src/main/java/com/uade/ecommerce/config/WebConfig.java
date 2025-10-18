package com.uade.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") 
            .allowedOrigins("http://localhost:5174") 
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") 
            .allowedHeaders("*") 
            .allowCredentials(true) 
            .maxAge(3600); 
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        
        // 1. Obtiene la ruta de la carpeta 'uploads' relativa al directorio actual
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        // 2. Mapea la URL /uploads/** a la carpeta física 'uploads/'
        // Usamos "file:/" + la ruta absoluta. 
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/" + uploadPath + "/");
    }
}