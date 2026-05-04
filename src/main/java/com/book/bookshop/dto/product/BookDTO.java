package com.book.bookshop.dto.product;

import com.book.bookshop.dto.GenreDTO;
import com.book.bookshop.dto.PublisherDTO;
import com.book.bookshop.dto.admin.authors.AuthorDTO;
import com.book.bookshop.dto.admin.category.CategoryDTO;
import com.book.bookshop.dto.review.ReviewProductDTO;
import com.book.bookshop.enums.CoverType;
import com.book.bookshop.enums.LanguageBook;
import com.book.bookshop.models.Book;
import com.book.bookshop.models.Genre;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
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
    private List<BookGenreDTO> genres;
    private LocalDateTime releaseDate;
    private PublisherDTO publisher;
    private List<AuthorDTO> authors;
    private List<ReviewProductDTO> reviews;
    private CategoryDTO category;
    private Double averageRating;
    public BookDTO(Book book, String lang) {
        this.bookId = book.getBookId();
        this.originalTitle = book.getOriginalTitle();
        this.price = book.getPrice();
        this.discountPrice = book.getDiscountPrice();
        this.stockQuantity = book.getStockQuantity();
        this.imageUrl = book.getImageUrl();
        this.pagesCount = book.getPagesCount();
        this.coverType = book.getCoverType();
        this.language = book.getLanguage();
        this.releaseDate = book.getRelease_date() != null
                ? book.getRelease_date().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                : null;

        if (book.getPublisher() != null) {
            this.publisher = new PublisherDTO(book.getPublisher().getName());
        }
        this.averageRating = book.getAverageRating();

        if (book.getGenres() != null) {
            this.genres = book.getGenres().stream()
                    .map(genre -> new BookGenreDTO(genre, lang))
                    .collect(Collectors.toList());
        }
        if(book.getCategory() != null) {
            this.category = new CategoryDTO(book.getCategory(),lang);
        }

        if (book.getAuthors() != null) {
            this.authors = book.getAuthors().stream()
                    .map(author -> new AuthorDTO(author.getAuthorId(),author.getFirstName(), author.getLastName()))
                    .collect(Collectors.toList());
        }

        // Reviews
        if (book.getReviews() != null) {
            this.reviews = book.getReviews().stream()
                    .map(review -> new ReviewProductDTO(
                            review.getReviewId(),
                            review.getCustomer().getFirstName(),
                            review.getRating(),
                            review.getComment()
                    ))
                    .collect(Collectors.toList());
        }

        if ("en".equalsIgnoreCase(lang)) {
            if (book.getTitleEn() != null && !book.getTitleEn().isEmpty()) {
                this.title = book.getTitleEn();
            } else if (book.getOriginalTitle() != null && !book.getOriginalTitle().isEmpty()) {
                this.title = book.getOriginalTitle();
            } else {
                this.title = book.getTitlePl();
            }
            this.description = book.getDescriptionEn() != null ? book.getDescriptionEn() : "";
        } else {
            if (book.getTitlePl() != null && !book.getTitlePl().isEmpty()) {
                this.title = book.getTitlePl();
            } else {
                this.title = book.getOriginalTitle();
            }
            this.description = book.getDescriptionPl() != null ? book.getDescriptionPl() : "";
        }
    }
}
