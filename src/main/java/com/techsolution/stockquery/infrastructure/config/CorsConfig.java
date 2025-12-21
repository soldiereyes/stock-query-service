package com.techsolution.stockquery.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*") // Permite todas as origens (em produção, especificar domínios)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(false) // Se precisar de cookies/credentials, mudar para true e especificar origins
                .maxAge(3600); // Cache de preflight por 1 hora
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Permitir todas as origens (em produção, especificar domínios específicos)
        config.setAllowedOrigins(List.of("*"));
        
        // Métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Headers permitidos
        config.setAllowedHeaders(List.of("*"));
        
        // Headers expostos na resposta
        config.setExposedHeaders(List.of("*"));
        
        // Permitir credentials (cookies, auth headers)
        config.setAllowCredentials(false);
        
        // Tempo de cache para preflight requests
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}


