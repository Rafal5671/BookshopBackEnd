package com.book.bookshop.security;

public class AuthResponse {
    private String token;

    // Konstruktor
    public AuthResponse(String token) {
        this.token = token;
    }

    // Getter i setter
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
