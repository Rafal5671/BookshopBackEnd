package com.book.bookshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.book.bookshop.security.JwtRequestFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    // Wstrzykujemy filtr JWT
    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .authorizeRequests()
                .requestMatchers("/api/customers/login", "/api/customers/register", "/api/books","/api/books/search", "/api/books/search/*","/api/books/*","/api/books/genre","/api/books/genre/**", "/api/orders").permitAll()
                .requestMatchers("/api/reviews/all/*").permitAll()
                .requestMatchers("/api/books/aggregated").permitAll()
                .requestMatchers("/api/reviews/all/").permitAll()
                .requestMatchers("/api/authors/**").permitAll()
                .requestMatchers("/api/authors/*").permitAll()
                .requestMatchers("/api/authors/").permitAll()
                .requestMatchers("/api/publishers/**").permitAll()
                .requestMatchers("/api/publishers/*").permitAll()
                .requestMatchers("/api/publishers/").permitAll()
                .requestMatchers("/api/reviews/all/**").permitAll()
                .requestMatchers("/api/reviews/*").permitAll()
                .requestMatchers("/api/reviews/").permitAll()
                .requestMatchers("/api/reviews/**").permitAll()
                .requestMatchers("/api/refresh/").permitAll()
                .requestMatchers("/api/refresh/**").permitAll()
                .requestMatchers("/api/refresh/refresh").permitAll()
                .requestMatchers("/api/reviews/book-reviews/*").permitAll()
                .requestMatchers("/api/reviews/book-reviews/").permitAll()
                .requestMatchers("/api/reviews/book-reviews/**").permitAll()
                .requestMatchers("/api/categories/").permitAll()
                .requestMatchers("/api/categories/**").permitAll()
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "EMPLOYEE")
                .requestMatchers("/api/admin/publishers/**").hasAnyRole("ADMIN", "EMPLOYEE")
                .requestMatchers("/api/admin/products/**").hasAnyRole("ADMIN", "EMPLOYEE")
                .requestMatchers("/api/admin/categories/**").hasAnyRole("ADMIN", "EMPLOYEE")
                .requestMatchers("/api/admin/statistics").hasAnyRole("ADMIN", "EMPLOYEE")
                .requestMatchers("/api/admin/refresh-tokens").hasAnyRole("ADMIN")
                .requestMatchers("/api/customers/me").authenticated() // Endpointy chronione wymagające autoryzacji
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class); // Dodanie filtra JWT przed standardowym filtrem

        return http.build();
    }
}
