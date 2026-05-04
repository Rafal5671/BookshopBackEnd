package com.book.bookshop.service;

import com.book.bookshop.models.RefreshToken;
import com.book.bookshop.repo.RefreshTokenRepository;
import com.book.bookshop.security.JwtUtil;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public RefreshToken createRefreshToken(String email) {
        String refreshJwt = jwtUtil.generateRefreshToken(email);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setEmail(email);
        refreshToken.setToken(refreshJwt);

        Date dbExpiry = new Date(System.currentTimeMillis() + jwtUtil.getRefreshExpirationTime());
        refreshToken.setExpiryDate(dbExpiry);

        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

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

    @Transactional
    public int  revokeTokensByEmail(String email) {
        return refreshTokenRepository.revokeTokensByEmail(email);
    }

    public RefreshToken revoke(RefreshToken token) {
        token.setRevoked(true);
        return refreshTokenRepository.save(token);
    }

    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void removeAllRevokedTokens() {
        long count = refreshTokenRepository.deleteByRevokedTrue();
        System.out.println("Usunięto " + count + " revoked tokenów.");
    }

}
