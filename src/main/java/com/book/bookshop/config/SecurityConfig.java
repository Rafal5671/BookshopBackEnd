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

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .authorizeRequests()
                .requestMatchers(
                        "/api/customers/login",
                        "/api/customers/register",
                        "/api/books/**",
                        "/api/authors/**",
                        "/api/publishers/**",
                        "/api/categories/**",
                        "/api/reviews/**",
                        "/api/refresh/**",
                        "/api/orders/**"
                ).permitAll()
                .requestMatchers("/api/admin/refresh-tokens").hasAnyRole("ADMIN")
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "EMPLOYEE")
                .requestMatchers("/api/customers/me").authenticated()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
