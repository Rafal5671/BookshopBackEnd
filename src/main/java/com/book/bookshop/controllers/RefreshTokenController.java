package com.book.bookshop.controllers;

import com.book.bookshop.models.Customer;
import com.book.bookshop.models.RefreshToken;
import com.book.bookshop.repo.CustomerRepository;
import com.book.bookshop.security.JwtUtil;
import com.book.bookshop.service.RefreshTokenService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/refresh")
@CrossOrigin(origins = "http://localhost:3000")
public class RefreshTokenController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String requestToken = request.getRefreshToken();

        // Najpierw szukamy RefreshToken w bazie (Optional)
        Optional<RefreshToken> optionalRefToken = refreshTokenService.findByToken(requestToken);

        if (optionalRefToken.isEmpty()) {
            // Brak takiego tokena w bazie → 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Nie znaleziono takiego Refresh Tokena. Zaloguj się ponownie.");
        }

        // Jeśli jest, to pobieramy
        RefreshToken refToken = optionalRefToken.get();

        // Wywołujemy metodę pomocniczą, która zwróci obiekt ResponseEntity (sukces lub błąd)
        return processRefreshToken(refToken);
    }

    private ResponseEntity<?> processRefreshToken(RefreshToken refToken) {
        // 1. Sprawdzamy, czy token wygasł
        if (!refreshTokenService.verifyExpiration(refToken)) {
            // Jeżeli wygasł, RefreshToken zostanie usunięty,
            // a my zwracamy 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token wygasł i został usunięty. Zaloguj się ponownie.");
        }

        // 2. Sprawdzenie, czy nie jest unieważniony (revoked)
        if (refToken.isRevoked()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token unieważniony. Zaloguj się ponownie.");
        }

        // 3. Pobieramy użytkownika po emailu z tokena
        Customer customer = customerRepository.findByEmail(refToken.getEmail());
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Nie znaleziono użytkownika. Zaloguj się ponownie.");
        }

        // 4. Generujemy nowy accessToken
        String role = customer.getRole().toString();
        String newAccessToken = jwtUtil.generateToken(refToken.getEmail(), role);

        // 5. Zwracamy 200 OK wraz z nowym tokenem
        return ResponseEntity.ok(new RefreshTokenResponse(newAccessToken));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
        long count = refreshTokenService.revokeTokensByEmail(request.getEmail());
        return ResponseEntity.ok("Unieważniono " + count + " refresh tokenów");
    }

    @Getter
    @Setter
    public static class LogoutRequest {
        private String email;
    }
    @Getter
    @Setter
    public static class RefreshTokenRequest {
        private String refreshToken;
    }

    @Getter
    @Setter
    public static class RefreshTokenResponse {
        private String accessToken;
        public RefreshTokenResponse(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}
