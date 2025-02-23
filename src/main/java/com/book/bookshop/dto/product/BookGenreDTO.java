package com.book.bookshop.dto.product;

import com.book.bookshop.models.Genre;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookGenreDTO {
    private Integer genreId;
    private String name;

    public BookGenreDTO(Genre genre, String lang) {
        this.genreId = genre.getGenreId();
        if ("en".equalsIgnoreCase(lang)) {
            this.name = (genre.getNameEn() != null && !genre.getNameEn().isEmpty())
                    ? genre.getNameEn()
                    : genre.getName();
        } else {
            this.name = (genre.getName() != null && !genre.getName().isEmpty())
                    ? genre.getName()
                    : genre.getNameEn();
        }
    }
}
