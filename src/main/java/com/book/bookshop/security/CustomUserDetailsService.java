package com.book.bookshop.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) {
        // Załaduj użytkownika z bazy danych (tutaj tylko przykładowe dane)
        return new User("user", "password", new ArrayList<>());
    }

    public boolean authenticate(String username, String password) {
        return "user".equals(username) && "password".equals(password);
    }
}
