package com.book.bookshop.controllers;

import com.book.bookshop.models.Customer;
import com.book.bookshop.models.LoginRequest;
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
    private CustomerService customerService;

    @Autowired
    private PasswordService passwordService;
    // Pobierz wszystkich klientów
    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerService.findAll();
    }

    // Pobierz klienta po ID
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Integer id) {
        return customerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Dodaj nowego klienta
    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer) {
        return customerService.save(customer);
    }

    // Aktualizuj istniejącego klienta
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Integer id, @RequestBody Customer customer) {
        if (customerService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        customer.setCustomerId(id);
        return ResponseEntity.ok(customerService.save(customer));
    }

    // Usuń klienta
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Integer id) {
        if (customerService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        customerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint do rejestracji nowego klienta
    @PostMapping("/register")
    public ResponseEntity<Customer> registerCustomer(@RequestBody Customer customer) {
        // Hashuj hasło przed zapisaniem do bazy
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

        // Możesz zwrócić dane klienta lub token JWT, jeśli używasz JWT do autoryzacji
        return ResponseEntity.ok(customer);
    }
}
