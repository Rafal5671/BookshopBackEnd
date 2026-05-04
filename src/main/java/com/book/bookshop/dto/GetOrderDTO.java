package com.book.bookshop.dto;

import com.book.bookshop.dto.order.OrderItemAdminDTO;
import com.book.bookshop.enums.OrderStatus;
import com.book.bookshop.enums.OrderType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class GetOrderDTO {
    private Integer orderId;
    private OrderType orderType;
    private String customerName;
    private String address;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private int itemCount;
    private List<OrderItemAdminDTO> orderItems;

    public GetOrderDTO(Integer orderId, OrderType orderType, String customerName, String address,
                       LocalDateTime orderDate, OrderStatus status, BigDecimal totalAmount,
                       int itemCount, List<OrderItemAdminDTO> orderItems) {
        this.orderId = orderId;
        this.orderType = orderType;
        this.customerName = customerName;
        this.address = address;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.itemCount = itemCount;
        this.orderItems = orderItems;
    }
}

