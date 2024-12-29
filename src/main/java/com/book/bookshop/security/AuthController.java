package com.book.bookshop.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostMapping("/authenticate")
    public String createAuthToken(@RequestParam String username, @RequestParam String password) {
        // Tu powinna być weryfikacja użytkownika, np. przez CustomUserDetailsService
        if (userDetailsService.authenticate(username, password)) {
            return jwtUtil.generateToken(username);
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }
}

