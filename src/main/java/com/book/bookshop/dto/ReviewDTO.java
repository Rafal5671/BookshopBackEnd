package com.book.bookshop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private Integer reviewId;
    private Integer rating;
    private String commentPl;
    private String commentEn;
    private LocalDateTime reviewDate;


}
