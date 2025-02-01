package com.book.bookshop.dto;

import com.book.bookshop.enums.OrderStatus;
import com.book.bookshop.enums.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GetOrderDTO {
    private Integer orderId;
    private OrderType orderType;
    private String customerName;
    private String address; // Połączony adres w formie jednego ciągu
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal amount;
    private int itemsCount;

    public GetOrderDTO(Integer orderId, OrderType orderType, String customerName, String address,
                    LocalDateTime orderDate, OrderStatus status, BigDecimal amount, int itemsCount) {
        this.orderId = orderId;
        this.orderType = orderType;
        this.customerName = customerName;
        this.address = address;
        this.orderDate = orderDate;
        this.status = status;
        this.amount = amount;
        this.itemsCount = itemsCount;
    }

    // Gettery i settery

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(int itemsCount) {
        this.itemsCount = itemsCount;
    }
}

