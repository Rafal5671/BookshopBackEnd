package com.book.bookshop.service;

import com.book.bookshop.models.RefreshToken;
import com.book.bookshop.repo.RefreshTokenRepository;
import com.book.bookshop.security.JwtUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               JwtUtil jwtUtil) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Tworzy nowy Refresh Token (JWT) i zapisuje go w bazie.
     */
    public RefreshToken createRefreshToken(String email) {
        // 1) Generujemy JWT o dłuższym czasie życia
        String refreshJwt = jwtUtil.generateRefreshToken(email);
        // Wewnątrz JwtUtil masz np. REFRESH_EXPIRATION_TIME = 7 dni

        // 2) Zapisujemy do encji i bazy
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setEmail(email);
        refreshToken.setToken(refreshJwt);

        // Zapiszmy też expiryDate w bazie, żeby mieć kontrolę
        // (opcjonalnie wyciągamy datę ważności z samego JWT lub
        //  używamy tego samego refreshTokenDurationMs co w JwtUtil):
        Date dbExpiry = new Date(System.currentTimeMillis() + jwtUtil.getRefreshExpirationTime());
        refreshToken.setExpiryDate(dbExpiry);

        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Wyszukaj Refresh Token w bazie po jego wartości (czyli całym stringu JWT).
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Weryfikuje, czy token nie jest przeterminowany (wg JwtUtil) i nie jest revoked.
     * Jeśli wygasł, usuwa go z bazy i zwraca false.
     */
    public boolean verifyExpiration(RefreshToken token) {
        String refreshJwt = token.getToken();

        try {
            if (jwtUtil.isTokenExpired(refreshJwt)) {
                refreshTokenRepository.delete(token);
                return false;
            }

            if (token.getExpiryDate().before(new Date())) {
                refreshTokenRepository.delete(token);
                return false;
            }

            return !token.isRevoked();

        } catch (Exception e) {
            refreshTokenRepository.delete(token);
            return false;
        }
    }

    /**
     * Unieważnienie / usunięcie wszystkich tokenów użytkownika (np. przy wylogowaniu).
     */
    public long revokeTokensByEmail(String email) {
        return refreshTokenRepository.deleteByEmail(email);
    }

    /**
     * Unieważnienie pojedynczego tokena (np. przy refresh rotation).
     */
    public RefreshToken revoke(RefreshToken token) {
        token.setRevoked(true);
        return refreshTokenRepository.save(token);
    }
    @Scheduled(cron = "0 0 * * * ?")
    public void removeAllRevokedTokens() {
        long count = refreshTokenRepository.deleteByRevokedTrue();
        System.out.println("Usunięto " + count + " revoked tokenów.");
    }
}
