package com.book.bookshop.dto;

import com.book.bookshop.enums.CoverType;
import com.book.bookshop.enums.LanguageBook;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
public class BookLangDTO {
    private Integer bookId;
    private String title;
    private String originalTitle;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String description;
    private Integer stockQuantity;
    private String imageUrl;
    private Integer pagesCount;
    private CoverType coverType;
    private LanguageBook language;
    private List<String> genres;
    private LocalDateTime releaseDate;
    private PublisherDTO publisher;
    private List<AuthorDTO> authors;
    private List<ReviewProductDTO> reviews;

    // Konstruktor
    public BookLangDTO(Integer bookId, String title, String originalTitle, BigDecimal price, BigDecimal discountPrice,
                   String description, Integer stockQuantity, String imageUrl, Integer pagesCount, CoverType coverType,
                       LanguageBook language, List<String> genres, LocalDateTime releaseDate, PublisherDTO publisher,
                   List<AuthorDTO> authors, List<ReviewProductDTO> reviews) {
        this.bookId = bookId;
        this.title = title;
        this.originalTitle = originalTitle;
        this.price = price;
        this.discountPrice = discountPrice;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.pagesCount = pagesCount;
        this.coverType = coverType;
        this.language = language;
        this.genres = genres;
        this.releaseDate = releaseDate;
        this.publisher = publisher;
        this.authors = authors;
        this.reviews = reviews;
    }

}
