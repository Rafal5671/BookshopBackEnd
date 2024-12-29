package com.book.bookshop.controllers;

import com.book.bookshop.models.Customer;
import com.book.bookshop.models.LoginRequest;
import com.book.bookshop.security.AuthResponse;
import com.book.bookshop.security.JwtUtil;
import com.book.bookshop.service.PasswordService;
import com.book.bookshop.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:3000")
public class CustomerController {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private PasswordService passwordService;

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers(@RequestHeader("Authorization") String authorization) {
        if (isTokenValid(authorization)) {
            return ResponseEntity.ok(customerService.findAll());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // Pobierz klienta po ID
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Integer id, @RequestHeader("Authorization") String authorization) {
        if (isTokenValid(authorization)) {
            return customerService.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Customer> registerCustomer(@RequestBody Customer customer) {
        String hashedPassword = passwordService.hashPassword(customer.getPassword());
        customer.setPassword(hashedPassword);
        Customer newCustomer = customerService.save(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCustomer);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Customer customer = customerService.findByEmail(loginRequest.getEmail());

        if (customer == null || !passwordService.checkPassword(loginRequest.getPassword(), customer.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        // Generowanie tokenu JWT po udanej weryfikacji użytkownika
        String token = jwtUtil.generateToken(customer.getEmail());

        // Zwrócenie tokenu w odpowiedzi
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // Metoda pomocnicza do sprawdzania ważności tokenu
    private boolean isTokenValid(String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return false;
            }

            String token = authorization.substring(7); // Usuwamy "Bearer " z nagłówka
            String username = jwtUtil.extractUsername(token); // Pobieramy username z tokenu
            return jwtUtil.validateToken(token, username); // Sprawdzamy token z poprawnym username
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Customer> getCustomerProfile(@RequestHeader("Authorization") String authHeader) {
        // Pobieramy token JWT z nagłówka
        String token = authHeader.substring(7);

        // Pobieramy email z tokenu
        String email = jwtUtil.extractUsername(token);
        System.out.println(email);
        // Znajdujemy klienta na podstawie emaila
        Customer customer = customerService.findByEmail(email);

        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(customer);
    }
}
