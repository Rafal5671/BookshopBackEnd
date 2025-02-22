package com.book.bookshop.dto.response;

import com.book.bookshop.dto.GenreDTO;
import com.book.bookshop.dto.admin.authors.AuthorDTO;
import com.book.bookshop.dto.admin.category.CategoryDTO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class AggregatedFilterData {
    private List<GenreDTO> genres;
    private List<CategoryDTO> categories;
    private List<AuthorDTO> authors;
    private BigDecimal maxAvailablePrice;

    public AggregatedFilterData(List<GenreDTO> genres, List<CategoryDTO> categories, List<AuthorDTO> authors, BigDecimal maxAvailablePrice) {
        this.genres = genres;
        this.categories = categories;
        this.authors = authors;
        this.maxAvailablePrice = maxAvailablePrice;
    }
}