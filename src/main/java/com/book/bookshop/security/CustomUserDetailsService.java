package com.book.bookshop.security;

import com.book.bookshop.models.Customer;
import com.book.bookshop.repo.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepository;  // Twoje repo

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Szukamy klienta po emailu
        Customer customer = customerRepository.findByEmail(username);
        if (customer == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }


        // Zwracamy obiekt, który Spring Security rozpozna.
        // Można np. użyć buildera z klasy User (z Spring Security):
        return User
                .withUsername(customer.getEmail())
                .password(customer.getPassword())
                // getAuthorities() z encji Customer może zwracać np. listę ról/pojedynczą rolę
                .authorities(customer.getAuthorities())
                .build();
    }
}

