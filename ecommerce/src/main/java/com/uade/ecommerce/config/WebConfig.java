package com.uade.ecommerce.config;

// ✅ IMPORT THESE NEW CLASSES
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.Arrays;
// ---
import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.CorsRegistry; // No longer needed
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ✅ THIS IS THE NEW GLOBAL CORS FILTER
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5174"); // Your frontend origin
        config.addAllowedHeader("*"); // Allow ALL headers
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")); // Allow ALL methods

        source.registerCorsConfiguration("/**", config); // Apply to ALL paths
        return new CorsFilter(source);
    }

    // ✅ THIS METHOD STAYS THE SAME (For viewing images)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/" + uploadPath + "/");
    }

    // ✅ THE addCorsMappings METHOD IS REMOVED/COMMENTED OUT
    /*
    @Override
    public void addCorsMappings(CorsRegistry registry) { ... }
    */
}