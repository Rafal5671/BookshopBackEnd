package com.book.bookshop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GenreFilterDTO {
    private Integer genreId;
    private String name;
}
