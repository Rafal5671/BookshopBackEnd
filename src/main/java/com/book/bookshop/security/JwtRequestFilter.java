package com.book.bookshop.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;

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
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        System.out.println("JwtRequestFilter triggered for: " + method + " " + requestURI);
        String token = null;
        String username = null;

        // Pobieranie tokenu z nagłówka
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(token);
            } catch (ExpiredJwtException e) {
                logger.warn("Token expired");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
                response.getWriter().write("Token expired");
                return;
            } catch (MalformedJwtException e) {
                logger.warn("Invalid token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid token");
                return;
            } catch (Exception e) {
                logger.warn("Token processing error: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token processing error");
                return;
            }
        }

        // Ustawiamy Authentication, jeśli token jest ważny
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Załaduj dane użytkownika
            UserDetails userDetails = userDetailsService.loadUserByUsername(username); // Tworzymy obiekt UserDetails z e-mailem jako username

            if (jwtUtil.validateToken(token, username)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }


        // Kontynuacja łańcucha filtrów
        chain.doFilter(request, response);
    }
}


