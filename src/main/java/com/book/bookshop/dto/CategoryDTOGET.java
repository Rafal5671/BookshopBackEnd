package com.book.bookshop.dto;

import java.time.LocalDateTime;

public class CategoryDTOGET {
    private Integer id;
    private String name;
    private LocalDateTime createdAt;

    // Konstruktor
    public CategoryDTOGET(Integer id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    // Gettery i Settery
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
