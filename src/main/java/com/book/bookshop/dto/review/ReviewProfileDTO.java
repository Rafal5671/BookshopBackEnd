package com.book.bookshop.dto.review;

import lombok.Data;

@Data
public class ReviewProfileDTO {
    private Integer reviewId;
    private Integer rating;
    private String content;
    private String reviewDate;
    private String createdAt;
    private String bookTitle;
    private Integer bookId;
}
