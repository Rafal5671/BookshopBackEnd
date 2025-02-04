package com.book.bookshop.dto.order;

import lombok.Data;

@Data
public class OrderItemDTO {
    private Integer itemId;
    private Integer quantity;
    private String bookTitle;
    private String createdAt;
}
