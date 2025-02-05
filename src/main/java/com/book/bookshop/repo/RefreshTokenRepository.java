package com.book.bookshop.repo;

import com.book.bookshop.models.RefreshToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    long deleteByEmail(String email);
    long deleteByRevokedTrue();
    Page<RefreshToken> findByEmail(String email, Pageable pageable);
}
