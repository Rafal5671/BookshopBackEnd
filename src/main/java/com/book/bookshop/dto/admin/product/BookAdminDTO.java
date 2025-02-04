package com.book.bookshop.dto.admin.product;

import com.book.bookshop.dto.admin.publisher.PublisherAdminDTO;
import com.book.bookshop.dto.admin.authors.AuthorDTO;
import com.book.bookshop.dto.review.ReviewProductDTO;
import com.book.bookshop.enums.CoverType;
import com.book.bookshop.enums.LanguageBook;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookAdminDTO {
    private Integer bookId;
    private String titlePl;
    private String titleEn;
    private String originalTitle;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String descriptionPl;
    private String descriptionEn;
    private Integer stockQuantity;
    private String imageUrl;
    private Integer pagesCount;
    private CoverType coverType;
    private LanguageBook language;
    private List<String> genres;
    private LocalDateTime releaseDate;
    private PublisherAdminDTO publisher;
    private List<AuthorDTO> authors;
    private List<ReviewProductDTO> reviews;
}

