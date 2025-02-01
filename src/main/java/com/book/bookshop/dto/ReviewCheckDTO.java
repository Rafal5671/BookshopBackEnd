package com.book.bookshop.dto;

import com.book.bookshop.models.Review;

import java.time.LocalDateTime;

public class ReviewCheckDTO {
    private Integer reviewId;
    private String commentPl;
    private String commentEn;
    private Integer rating;
    private LocalDateTime reviewDate;
    private Integer bookId;
    private String customerName;

    // Constructor to map Review entity to ReviewDTO
    public ReviewCheckDTO(Review review) {
        this.reviewId = review.getReviewId();
        this.commentPl = review.getCommentPl();
        this.commentEn = review.getCommentEn();
        this.rating = review.getRating();
        this.reviewDate = review.getReviewDate();
        this.bookId = review.getBook().getBookId();
        this.customerName = review.getCustomer().getFirstName() + " " + review.getCustomer().getLastName();
    }

    // Getters and Setters
    public Integer getReviewId() {
        return reviewId;
    }

    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
    }

    public String getCommentPl() {
        return commentPl;
    }

    public void setCommentPl(String commentPl) {
        this.commentPl = commentPl;
    }

    public String getCommentEn() {
        return commentEn;
    }

    public void setCommentEn(String commentEn) {
        this.commentEn = commentEn;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
