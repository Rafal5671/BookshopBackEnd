package com.book.bookshop.controllers;

import com.book.bookshop.models.Order;
import com.book.bookshop.service.OrderService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:3000")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 1. Pobranie wszystkich zamówień (dla celów administracyjnych?)
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.findAll();
    }

    // 2. Pobranie zamówienia po ID
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Integer id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Aktualizacja zamówienia
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Integer id, @RequestBody Order order) {
        return orderService.updateOrder(id, order)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. Tworzenie zamówienia
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> orderData
    ) {
        try {
            Object result = orderService.createOrder(authHeader, orderData);

            // Obsługa ewentualnych błędów (jeśli result jest Stringiem z błędem)
            if (result instanceof String errorMessage) {
                return ResponseEntity.badRequest().body(errorMessage);
            }
            // W innym przypadku zakładamy, że mamy Map z danymi (np. orderId / url)
            return ResponseEntity.ok(result);

        } catch (StripeException e) {
            // W razie błędu Stripe
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Błąd integracji ze Stripe: " + e.getMessage());
        } catch (RuntimeException e) {
            // W razie innego błędu (np. brak książki w bazie)
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. Usuwanie zamówienia
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer id) {
        boolean deleted = orderService.deleteOrder(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
