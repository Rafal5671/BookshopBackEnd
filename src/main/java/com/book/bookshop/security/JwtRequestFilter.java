package com.book.bookshop.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String path = request.getRequestURI();
        System.out.println("Request URI: " + path);

        // Pomijamy publiczne endpointy
        if (path.startsWith("/api/books") || path.startsWith("/api/customers/login") || path.startsWith("/api/customers/register")) {
            System.out.println("Public endpoint, skipping authentication: " + path);
            chain.doFilter(request, response);
            return;
        }
        // Pobieramy token z nagłówka "Authorization"
        String token = request.getHeader("Authorization");
        String username = null;

        // Jeśli token zaczyna się od "Bearer ", wyciągamy token
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // Usuwamy "Bearer " z tokenu
            username = jwtUtil.extractUsername(token); // Pobieramy username (email) z tokenu
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var userDetails = userDetailsService.loadUserByUsername(username);

            // Jeśli token jest poprawny, ustawiamy autentykację
            if (jwtUtil.validateToken(token, username)) {
                var authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response); // Kontynuujemy przetwarzanie łańcucha filtrów
    }
}

