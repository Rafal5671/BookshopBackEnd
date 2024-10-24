package com.book.bookshop.controllers;
import com.book.bookshop.models.Order;
import com.book.bookshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Integer id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderService.save(order);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Integer id, @RequestBody Order order) {
        if (orderService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        order.setOrderId(id);
        return ResponseEntity.ok(orderService.save(order));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer id) {
        if (orderService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        orderService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
