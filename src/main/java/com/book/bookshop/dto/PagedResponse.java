package com.book.bookshop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private int pageNumber;      // numer strony
    private int pageSize;        // ile elementów
    private long totalElements;  // łączna liczba elementów
    private int totalPages;      // liczba stron
}
