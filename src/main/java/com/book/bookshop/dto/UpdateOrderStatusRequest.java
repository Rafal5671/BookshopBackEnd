package com.book.bookshop.dto;

import com.book.bookshop.enums.OrderStatus;

public class UpdateOrderStatusRequest {
    private OrderStatus status;

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
