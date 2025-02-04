package com.book.bookshop.dto.review;

import lombok.Data;

@Data
public class ReviewProfileDTO {
    private Integer reviewId;
    private Integer rating;
    private String commentPl;
    private String commentEn;
    private String reviewDate;
    private String createdAt;
    private String bookTitle;   // jeżeli chcesz wyświetlić tytuł książki
}
