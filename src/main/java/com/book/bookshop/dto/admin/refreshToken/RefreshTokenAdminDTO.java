package com.book.bookshop.dto.admin.refreshToken;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class RefreshTokenAdminDTO {
    private Long id;
    private String token;
    private String email;
    private Date expiryDate;
    private boolean revoked;

    public RefreshTokenAdminDTO(Long id, String token, String email, Date expiryDate, boolean revoked) {
        this.id = id;
        this.token = token;
        this.email = email;
        this.expiryDate = expiryDate;
        this.revoked = revoked;
    }
}

