package com.book.bookshop.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Reviews")
@Getter
@Setter
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private Integer rating;
    private String commentPl;
    private String commentEn;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}