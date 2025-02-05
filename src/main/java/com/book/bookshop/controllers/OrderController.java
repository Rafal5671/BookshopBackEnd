package com.book.bookshop.controllers;
import com.book.bookshop.enums.OrderStatus;
import com.book.bookshop.enums.OrderType;
import com.book.bookshop.enums.PaymentMethod;
import com.book.bookshop.enums.PaymentStatus;
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
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.stripe.model.checkout.Session;

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
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;
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
    public ResponseEntity<?> createOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> orderData) {
        try {
            Customer customer = null;
            OrderType orderType = OrderType.GUEST_USER; // domyślnie gość

            // Jeśli nagłówek Authorization jest podany, próbujemy pobrać użytkownika
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtUtil.extractUsername(token);
                customer = customerService.findByEmail(email);
                if (customer != null) {
                    orderType = OrderType.REGISTERED_USER;
                }
            }

            // Zapis adresu
            @SuppressWarnings("unchecked")
            Map<String, String> addressData = (Map<String, String>) orderData.get("address");
            Address address = new Address();
            address.setStreet(addressData.get("street"));
            address.setPostalCode(addressData.get("postalCode"));
            address.setCity(addressData.get("city"));
            Address savedAddress = addressRepository.save(address);

            // Utworzenie obiektu zamówienia
            Order order = new Order();
            order.setCustomer(customer); // Może być null przy zamówieniu gościa
            order.setAmount(new BigDecimal(orderData.get("amount").toString()));
            order.setStatus(OrderStatus.PENDING);
            order.setAddress(savedAddress);
            order.setOrderType(orderType);

            if (orderData.containsKey("paymentMethod")) {
                String paymentMethodStr = orderData.get("paymentMethod").toString().toUpperCase();
                order.setPaymentMethod(PaymentMethod.valueOf(paymentMethodStr));
            } else {
                order.setPaymentMethod(PaymentMethod.CREDIT_CARD); // domyślna metoda
            }
            // Ustawienie statusu płatności na PENDING
            order.setPaymentStatus(PaymentStatus.PENDING);

            Order savedOrder = orderService.save(order);

            // Zapis pozycji zamówienia
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");
            for (Map<String, Object> itemData : items) {
                OrderItem item = new OrderItem();
                int bookId = (Integer) itemData.get("bookId");
                int quantity = (Integer) itemData.get("quantity");

                item.setBook(bookRepository.findById(bookId)
                        .orElseThrow(() -> new RuntimeException("Nie znaleziono książki o ID: " + bookId)));
                item.setQuantity(quantity);
                item.setOrder(savedOrder);

                orderItemService.save(item);
            }

            // Konfiguracja Stripe
            Stripe.apiKey = stripeSecretKey;
            long amountInCents = savedOrder.getAmount()
                    .multiply(new BigDecimal(100))
                    .longValue();

            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency("pln")
                                            .setUnitAmount(amountInCents)
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName("Zamówienie nr " + savedOrder.getOrderId())
                                                            .build()
                                            )
                                            .build()
                            )
                            .build();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:3000/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:3000/cancel")
                    .addLineItem(lineItem)
                    .build();

            Session session = Session.create(params);

            savedOrder.setSessionId(session.getId());
            orderService.save(savedOrder);

            Map<String, String> responseData = Map.of(
                    "orderId", String.valueOf(savedOrder.getOrderId()),
                    "url", session.getUrl()
            );

            return ResponseEntity.ok(responseData);
        } catch (StripeException e) {
            throw new RuntimeException(e);
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
