package com.book.bookshop.controllers;

import com.book.bookshop.dto.customer.CustomerDTO;
import com.book.bookshop.dto.customer.CustomerProfileDTO;
import com.book.bookshop.enums.UserRole;
import com.book.bookshop.mapper.CustomerMapper;
import com.book.bookshop.mapper.CustomerProfileMapper;
import com.book.bookshop.models.Customer;
import com.book.bookshop.models.LoginRequest;
import com.book.bookshop.models.RefreshToken;
import com.book.bookshop.security.AuthResponse;
import com.book.bookshop.security.JwtUtil;
import com.book.bookshop.service.PasswordService;
import com.book.bookshop.service.CustomerService;
import com.book.bookshop.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

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
    @Autowired
    private CustomerProfileMapper userProfileMapper;
    @Autowired
    private RefreshTokenService refreshTokenService;

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return ResponseEntity.ok(customerService.findAll());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Integer id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return customerService.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerCustomer(@RequestBody Customer customer) {
        // Hashujemy hasło i ustawiamy domyślną rolę
        String hashedPassword = passwordService.hashPassword(customer.getPassword());
        customer.setPassword(hashedPassword);
        customer.setRole(UserRole.ROLE_USER);

        // Zapisujemy nowego klienta
        Customer newCustomer = customerService.save(customer);

        // Generujemy Access Token na podstawie email i roli
        String accessToken = jwtUtil.generateToken(newCustomer.getEmail(), newCustomer.getRole().toString());

        // Tworzymy Refresh Token i zapisujemy go w bazie
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(newCustomer.getEmail());

        // Przygotowujemy obiekt odpowiedzi zawierający oba tokeny
        AuthResponse response = new AuthResponse(accessToken, refreshToken.getToken());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Customer customer = customerService.findByEmail(loginRequest.getEmail());

        if (customer == null || !passwordService.checkPassword(loginRequest.getPassword(), customer.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        // 1) Generujemy Access Token (krótki)
        String accessToken = jwtUtil.generateToken(customer.getEmail(),customer.getRole().toString());

        // 2) Tworzymy Refresh Token w bazie (jeśli używamy JWT, tam w środku się wygeneruje).
        //    Alternatywnie można generować JWT i tu, a w bazie tylko zapisać klucz:
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(customer.getEmail());

        // 3) Zwracamy oba tokeny klientowi
        AuthResponse response = new AuthResponse(accessToken, refreshToken.getToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<CustomerDTO> getCustomerProfile(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Customer customer = customerService.findByEmail(email);

        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        System.out.println(customer.getFirstName());
        CustomerDTO customerDto = CustomerMapper.toDto(customer);
        return ResponseEntity.ok(customerDto);
    }
    @GetMapping("/profile/me")
    public ResponseEntity<CustomerProfileDTO> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = userDetails.getUsername();
        Customer customer = customerService.findByEmail(email);

        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        CustomerProfileDTO dto = userProfileMapper.toUserProfileDto(customer);
        return ResponseEntity.ok(dto);
    }
}

