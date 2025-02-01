package com.book.bookshop.dto;

import lombok.Data;

@Data
public class OrderItemDTO {
    private Integer itemId;
    private Integer quantity;
    private String bookTitle;  // jeśli chcesz wyświetlić tytuł książki
    private String createdAt;
}
