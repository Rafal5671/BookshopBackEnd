package com.book.bookshop.models;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Getter
public enum UserRole implements GrantedAuthority {
    ROLE_USER,
    ROLE_EMPLOYEE,
    ROLE_ADMIN;

    @Override
    public String getAuthority() {
        return this.name();
    }
}
