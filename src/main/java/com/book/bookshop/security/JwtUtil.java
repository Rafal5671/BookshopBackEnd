package com.book.bookshop.security;
import io.jsonwebtoken.*;
import java.util.Date;

import lombok.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    private String secretKey = "75bf7fca6937610f1b5b7fddee261dcf707f3c3e4881a60ddb8fa5cc3796a8c4"; // Możesz zmienić na bardziej bezpieczny klucz

    // Metoda do generowania tokena
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 godzin
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // Metoda do pobierania nazwy użytkownika z tokena
    public String extractUsername(String token) {
        String email = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        System.out.println("Decoded email: " + email);
        return email;
    }

    // Metoda do walidacji tokena
    public boolean validateToken(String token, String username) {
        return (username.equals(extractUsername(token)) && !isTokenExpired(token));
    }

    // Metoda sprawdzająca, czy token wygasł
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Metoda do pobierania daty wygaśnięcia tokena
    private Date extractExpiration(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
}
