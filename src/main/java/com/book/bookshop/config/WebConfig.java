package com.book.bookshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // zezwalaj na wszystkie ścieżki
                .allowedOrigins("http://localhost:3000") // adres frontendowy
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // dozwolone metody
                .allowedHeaders("*") // zezwalaj na wszystkie nagłówki
                .allowCredentials(true); // zezwolenie na ciasteczka (opcjonalnie)
    }
}
