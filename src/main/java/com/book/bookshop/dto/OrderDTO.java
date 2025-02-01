package com.book.bookshop.dto;

import com.book.bookshop.enums.OrderStatus;
import lombok.Data;

import java.util.List;

@Data
public class OrderDTO {
    private Integer orderId;
    private OrderStatus status;
    private String orderType;
    private String amount;
    private String orderDate;
    private String createdAt;
    private List<OrderItemDTO> items;
}
