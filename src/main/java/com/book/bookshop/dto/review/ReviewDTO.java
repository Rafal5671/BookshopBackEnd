package com.book.bookshop.dto.review;

import com.book.bookshop.models.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Integer reviewId;
    private String commentPl;
    private String commentEn;
    private Integer rating;
    private LocalDateTime reviewDate;
    private Integer bookId;
    private String name;

    public ReviewDTO(Review review) {
        this.reviewId = review.getReviewId();
        this.commentPl = review.getCommentPl();
        this.commentEn = review.getCommentEn();
        this.rating = review.getRating();
        this.reviewDate = review.getReviewDate();
        this.bookId = review.getBook().getBookId();
        this.name = review.getCustomer().getFirstName();
    }
}
