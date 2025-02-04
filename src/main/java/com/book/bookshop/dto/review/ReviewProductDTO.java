package com.book.bookshop.dto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewProductDTO {
    private Integer reviewId;
    private String user;
    private Integer rating;
    private String content;
}
