package com.book.bookshop.controllers;
import com.book.bookshop.enums.OrderStatus;
import com.book.bookshop.enums.OrderType;
import com.book.bookshop.models.Address;
import com.book.bookshop.models.Customer;
import com.book.bookshop.models.Order;
import com.book.bookshop.models.OrderItem;
import com.book.bookshop.repo.AddressRepository;
import com.book.bookshop.repo.BookRepository;
import com.book.bookshop.security.JwtUtil;
import com.book.bookshop.service.CustomerService;
import com.book.bookshop.service.OrderItemService;
import com.book.bookshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:3000")
public class OrderController {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private OrderService orderService;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private CustomerService customerService;
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

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Integer id, @RequestBody Order order) {
        if (orderService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        order.setOrderId(id);
        return ResponseEntity.ok(orderService.save(order));
    }
    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> orderData) {
        try {
            String token = authHeader.substring(7);

            String email = jwtUtil.extractUsername(token);

            Customer customer = customerService.findByEmail(email);

            if (customer == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Map<String, String> addressData = (Map<String, String>) orderData.get("address");
            Address address = new Address();
            address.setStreet(addressData.get("street"));
            address.setPostalCode(addressData.get("postalCode"));
            address.setCity(addressData.get("city"));

            Address savedAddress = addressRepository.save(address);

            Order order = new Order();
            order.setCustomer(customer);
            order.setAmount(new BigDecimal(orderData.get("amount").toString()));
            order.setStatus(OrderStatus.PENDING);
            order.setAddress(savedAddress);
            order.setOrderType(OrderType.REGISTERED_USER);
            Order savedOrder = orderService.save(order);

            // Dodawanie pozycji zamówienia
            List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");
            for (Map<String, Object> itemData : items) {
                OrderItem item = new OrderItem();
                item.setBook(bookRepository.findById((Integer) itemData.get("bookId")).orElseThrow());
                item.setQuantity((Integer) itemData.get("quantity"));
                item.setOrder(savedOrder);
                orderItemService.save(item);
            }

            return ResponseEntity.ok(savedOrder);
        } catch (Exception e) {
            System.err.println("Błąd podczas tworzenia zamówienia: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
