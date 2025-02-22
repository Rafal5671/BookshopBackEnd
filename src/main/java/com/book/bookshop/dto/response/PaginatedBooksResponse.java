package com.book.bookshop.dto.response;

import com.book.bookshop.dto.product.BookDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PaginatedBooksResponse {
    private List<BookDTO> books;
    private int currentPage;
    private int totalPages;
    private long totalItems;

    public PaginatedBooksResponse(List<BookDTO> books, int currentPage, int totalPages, long totalItems) {
        this.books = books;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
    }
}