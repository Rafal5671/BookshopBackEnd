package com.book.bookshop.controllers;

import com.book.bookshop.models.OrderItem;
import com.book.bookshop.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {
    @Autowired
    private OrderItemService orderItemService;

    @GetMapping
    public List<OrderItem> getAllOrderItems() {
        return orderItemService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderItem> getOrderItemById(@PathVariable Integer id) {
        return orderItemService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public OrderItem createOrderItem(@RequestBody OrderItem orderItem) {
        return orderItemService.save(orderItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderItem> updateOrderItem(@PathVariable Integer id, @RequestBody OrderItem orderItem) {
        if (orderItemService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        orderItem.setItemId(id);
        return ResponseEntity.ok(orderItemService.save(orderItem));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable Integer id) {
        if (orderItemService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        orderItemService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

