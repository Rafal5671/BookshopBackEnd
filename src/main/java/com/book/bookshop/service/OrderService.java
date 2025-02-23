package com.book.bookshop.service;

import com.book.bookshop.enums.*;
import com.book.bookshop.models.*;
import com.book.bookshop.repo.*;
import com.book.bookshop.security.JwtUtil;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private CustomerService customerService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    // ------------------------
    // PODSTAWOWE METODY (CRUD)
    // ------------------------
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Optional<Order> findById(Integer id) {
        return orderRepository.findById(id);
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public void deleteById(Integer id) {
        orderRepository.deleteById(id);
    }

    // ------------------------
    // LOGIKA: CREATE ORDER
    // ------------------------
    public Object createOrder(String authHeader, Map<String, Object> orderData) throws StripeException {

        Customer customer = null;
        OrderType orderType = OrderType.GUEST_USER;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);
            customer = customerService.findByEmail(email);
            if (customer != null) {
                orderType = OrderType.REGISTERED_USER;
            }
        }


        Map<String, String> addressData = (Map<String, String>) orderData.get("address");
        if (addressData == null) {
            return "Brak adresu w zamówieniu.";
        }
        Address address = new Address();
        address.setStreet(addressData.get("street"));
        address.setPostalCode(addressData.get("postalCode"));
        address.setCity(addressData.get("city"));
        address.setCountry(addressData.get("country"));
        Address savedAddress = addressRepository.save(address);


        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setAddress(savedAddress);
        order.setOrderType(orderType);

        if (orderData.containsKey("paymentMethod")) {
            String paymentMethodStr = orderData.get("paymentMethod").toString().toUpperCase();
            if ("ONLINE".equals(paymentMethodStr)) {
                order.setPaymentMethod(PaymentMethod.STRIPE);
            } else if ("CASH".equals(paymentMethodStr)) {
                order.setPaymentMethod(PaymentMethod.CASH);
            } else {
                order.setPaymentMethod(PaymentMethod.STRIPE);
            }
        } else {
            order.setPaymentMethod(PaymentMethod.STRIPE);
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");
        if (items == null || items.isEmpty()) {
            return "Brak pozycji w zamówieniu.";
        }

        BigDecimal computedTotal = BigDecimal.ZERO;
        for (Map<String, Object> itemData : items) {
            int bookId = (Integer) itemData.get("bookId");
            int quantity = (Integer) itemData.get("quantity");

            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Nie znaleziono książki o ID: " + bookId));
            BigDecimal unitPrice = (book.getDiscountPrice() != null) ? book.getDiscountPrice() : book.getPrice();
            computedTotal = computedTotal.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        }
        order.setAmount(computedTotal);

        Order savedOrder = orderRepository.save(order);

        for (Map<String, Object> itemData : items) {
            int bookId = (Integer) itemData.get("bookId");
            int quantity = (Integer) itemData.get("quantity");

            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Nie znaleziono książki o ID: " + bookId));

            OrderItem item = new OrderItem();
            item.setBook(book);
            item.setQuantity(quantity);
            item.setOrder(savedOrder);

            orderItemService.save(item);
        }

        if (savedOrder.getPaymentMethod() == PaymentMethod.STRIPE) {
            Stripe.apiKey = stripeSecretKey;
            long amountInCents = savedOrder.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
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
            orderRepository.save(savedOrder);

            return Map.of(
                    "orderId", String.valueOf(savedOrder.getOrderId()),
                    "url", session.getUrl()
            );
        } else {
            return Map.of("orderId", String.valueOf(savedOrder.getOrderId()));
        }
    }

    // ------------------------
    // LOGIKA: UPDATE ORDER
    // ------------------------
    public Optional<Order> updateOrder(Integer id, Order updatedData) {
        Optional<Order> existingOpt = orderRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }
        Order existingOrder = existingOpt.get();

        existingOrder.setStatus(updatedData.getStatus());
        existingOrder.setOrderType(updatedData.getOrderType());
        existingOrder.setPaymentStatus(updatedData.getPaymentStatus());
        existingOrder.setPaymentMethod(updatedData.getPaymentMethod());

        return Optional.of(orderRepository.save(existingOrder));
    }

    // ------------------------
    // LOGIKA: DELETE ORDER
    // ------------------------
    public boolean deleteOrder(Integer id) {
        Optional<Order> existing = orderRepository.findById(id);
        if (existing.isEmpty()) {
            return false;
        }
        orderRepository.deleteById(id);
        return true;
    }
}
