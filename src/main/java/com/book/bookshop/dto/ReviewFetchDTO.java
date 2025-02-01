package com.book.bookshop.dto;

import com.book.bookshop.models.Review;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewFetchDTO {
    private Integer reviewId;
    private String content;
    private Integer rating;
    private String contentEN;
    private String user; // imię użytkownika

    // Konstruktor przyjmujący encję Review
    public ReviewFetchDTO(Review review) {
        this.reviewId = review.getReviewId();
        this.content = review.getCommentPl();
        this.contentEN = review.getCommentEn();
        this.rating = review.getRating();
        this.user = review.getCustomer().getFirstName();
    }
}
