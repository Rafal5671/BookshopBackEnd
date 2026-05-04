package com.book.bookshop.mapper;

import com.book.bookshop.dto.customer.CustomerProfileDTO;
import com.book.bookshop.dto.order.OrderDTO;
import com.book.bookshop.dto.order.OrderItemDTO;
import com.book.bookshop.dto.review.ReviewProfileDTO;
import com.book.bookshop.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomerProfileMapper {
    private static final Logger logger = LoggerFactory.getLogger(CustomerProfileMapper.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public CustomerProfileDTO toUserProfileDto(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerProfileDTO dto = new CustomerProfileDTO();
        dto.setEmail(customer.getEmail());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setPhone(customer.getPhone());

        if (customer.getCreatedAt() != null) {
            dto.setCreatedAt(customer.getCreatedAt().format(DATE_FORMATTER));
        }

        // Zamówienia
        if (customer.getOrders() != null) {
            List<OrderDTO> orderDtos = customer.getOrders().stream()
                    .peek(order -> logger.info("Processing Order: {}", order))
                    .map(this::toOrderDto)
                    .collect(Collectors.toList());
            dto.setOrders(orderDtos);
        }

        // Recenzje
        if (customer.getReviews() != null) {
            List<ReviewProfileDTO> reviewDtos = customer.getReviews().stream()
                    .map(this::toReviewDto)
                    .collect(Collectors.toList());
            dto.setReviews(reviewDtos);
        }

        return dto;
    }

    private OrderDTO toOrderDto(Order order) {
        if (order == null) {
            return null;
        }
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setStatus(order.getStatus());
        dto.setOrderType(order.getOrderType() != null ? order.getOrderType().name() : null);
        dto.setAmount(order.getAmount() != null ? order.getAmount().toPlainString() : null);

        if (order.getOrderDate() != null) {
            dto.setOrderDate(order.getOrderDate().format(DATE_FORMATTER));
        }
        if (order.getCreatedAt() != null) {
            dto.setCreatedAt(order.getCreatedAt().format(DATE_FORMATTER));
        }

        if (order.getItems() != null) {
            logger.info("Mapping Order Items for Order ID {}: {}", order.getOrderId(), order.getItems());
            List<OrderItemDTO> itemDtos = order.getItems().stream()
                    .map(this::toOrderItemDto)
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);
        }

        return dto;
    }

    private OrderItemDTO toOrderItemDto(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        OrderItemDTO dto = new OrderItemDTO();
        dto.setItemId(orderItem.getItemId());
        dto.setQuantity(orderItem.getQuantity());

        if (orderItem.getCreatedAt() != null) {
            dto.setCreatedAt(orderItem.getCreatedAt().format(DATE_FORMATTER));
        }

        if (orderItem.getBook() != null) {
            dto.setBookTitle(orderItem.getBook().getTitlePl());
        }

        return dto;
    }
    private ReviewProfileDTO toReviewDto(Review review) {
        if (review == null) {
            return null;
        }
        ReviewProfileDTO dto = new ReviewProfileDTO();
        dto.setReviewId(review.getReviewId());
        dto.setRating(review.getRating());
        dto.setContent(review.getComment());
        dto.setBookId(review.getBook().getBookId());
        if (review.getReviewDate() != null) {
            dto.setReviewDate(review.getReviewDate().format(DATE_FORMATTER));
        }
        if (review.getCreatedAt() != null) {
            dto.setCreatedAt(review.getCreatedAt().format(DATE_FORMATTER));
        }

        if (review.getBook() != null) {
            dto.setBookTitle(review.getBook().getTitlePl());
        }

        return dto;
    }

}
