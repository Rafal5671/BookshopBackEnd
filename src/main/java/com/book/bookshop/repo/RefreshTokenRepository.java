package com.book.bookshop.repo;

import com.book.bookshop.models.RefreshToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    long deleteByRevokedTrue();
    Page<RefreshToken> findByEmail(String email, Pageable pageable);
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.email = :email AND r.revoked = false")
    int revokeTokensByEmail(@Param("email") String email);
}
