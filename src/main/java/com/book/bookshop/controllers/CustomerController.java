package com.book.bookshop.controllers;

import com.book.bookshop.dto.customer.CustomerDTO;
import com.book.bookshop.dto.customer.CustomerProfileDTO;
import com.book.bookshop.mapper.CustomerMapper;
import com.book.bookshop.mapper.CustomerProfileMapper;
import com.book.bookshop.models.Customer;
import com.book.bookshop.models.LoginRequest;
import com.book.bookshop.security.AuthResponse;
import com.book.bookshop.security.JwtUtil;
import com.book.bookshop.service.PasswordService;
import com.book.bookshop.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public ResponseEntity<Customer> registerCustomer(@RequestBody Customer customer) {
        String hashedPassword = passwordService.hashPassword(customer.getPassword());
        customer.setPassword(hashedPassword);
        //TODO dodaj role w rejestracji
        Customer newCustomer = customerService.save(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCustomer);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Customer customer = customerService.findByEmail(loginRequest.getEmail());

        if (customer == null || !passwordService.checkPassword(loginRequest.getPassword(), customer.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        String token = jwtUtil.generateToken(customer.getEmail());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @GetMapping("/me")
    public ResponseEntity<CustomerDTO> getCustomerProfile(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Customer customer = customerService.findByEmail(email);

        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

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

