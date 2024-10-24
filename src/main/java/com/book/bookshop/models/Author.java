package com.book.bookshop.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Authors")
@Getter
@Setter
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer authorId;

    private String firstName;
    private String lastName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}