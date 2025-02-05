package com.book.bookshop.models;

import com.book.bookshop.enums.CoverType;
import com.book.bookshop.enums.LanguageBook;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Books")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookId;

    private String titlePl;
    private String titleEn;
    private String originalTitle;
    private BigDecimal price;
    private BigDecimal discountPrice;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(columnDefinition = "TEXT")
    private String descriptionPl;
    @Column(columnDefinition = "TEXT")
    private String descriptionEn;
    private Integer stockQuantity;
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    @ManyToMany
    @JoinTable(
            name = "BookAuthor",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id"))
    private List<Author> authors =  new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    private Integer pagesCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "cover_type")
    private CoverType coverType;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_language")
    private LanguageBook language;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date release_date;

    @ManyToMany
    @JoinTable(
            name = "BookGenre",  // Tabela pośrednicząca
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private List<Genre> genres =  new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews =  new ArrayList<>();

    @Transient
    public Double getAverageRating() {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}