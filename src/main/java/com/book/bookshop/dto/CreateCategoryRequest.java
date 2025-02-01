package com.book.bookshop.dto;

public class CreateCategoryRequest {
    private String nameEn;
    private String namePl;

    // Gettery i Settery
    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNamePl() {
        return namePl;
    }

    public void setNamePl(String namePl) {
        this.namePl = namePl;
    }
}
