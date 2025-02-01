package com.book.bookshop.dto;

import java.time.LocalDateTime;

public class CategoryDTO {
    private Integer id;
    private String nameEn;
    private String namePl;
    private LocalDateTime createdAt;

    // Konstruktor
    public CategoryDTO(Integer id, String nameEn, String namePl, LocalDateTime createdAt) {
        this.id = id;
        this.nameEn = nameEn;
        this.namePl = namePl;
        this.createdAt = createdAt;
    }

    // Gettery i Settery
    public Integer getId() {
        return id;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getNamePl() {
        return namePl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

