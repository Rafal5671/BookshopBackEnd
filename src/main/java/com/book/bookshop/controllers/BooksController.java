package com.book.bookshop.controllers;

import com.book.bookshop.dto.GenreDTO;
import com.book.bookshop.dto.admin.authors.AuthorDTO;
import com.book.bookshop.dto.admin.category.CategoryDTO;
import com.book.bookshop.dto.product.BookDTO;
import com.book.bookshop.dto.response.AggregatedFilterData;
import com.book.bookshop.dto.response.PaginatedBooksResponse;
import com.book.bookshop.models.Book;
import com.book.bookshop.service.BookService;
import com.book.bookshop.specifications.BookSpecification;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class BooksController {

    @Autowired
    private BookService bookService; // Usunęliśmy bezpośrednie korzystanie z BookRepository

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "pl") String lang
    ) {
        // Cała logika jest w serwisie
        return bookService.getBookByIdDTO(id, lang)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @GetMapping("/genres")
    public List<GenreDTO> getAllGenres() {
        // W serwisie mamy getAllGenres()
        return bookService.getAllGenresDTO();
    }

    @GetMapping("/categories")
    public List<CategoryDTO> getAllCategories(
            @RequestParam(defaultValue = "pl") String lang
    ) {
        return bookService.getAllCategoriesDTO(lang);
    }

    @GetMapping("/authors")
    public List<AuthorDTO> getAllAuthors() {
        return bookService.getAllAuthorsDTO();
    }

    @GetMapping("/genre/{genreId}")
    public List<BookDTO> getBooksByGenre(
            @PathVariable Integer genreId,
            @RequestParam(defaultValue = "pl") String lang
    ) {
        return bookService.findBooksByGenreDTO(genreId, lang);
    }

    @GetMapping
    public ResponseEntity<?> getBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Integer> genreId,
            @RequestParam(required = false) List<Integer> categoryId,
            @RequestParam(required = false) List<Integer> authorId,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) Boolean onSale,
            @RequestParam(defaultValue = "titleAsc") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "pl") String lang
    ) {
        try {
            // 1. Zbudowanie Specification na podstawie parametrów
            Specification<Book> spec = Specification.where(BookSpecification.titleContains(search))
                    .or(BookSpecification.authorContains(search))
                    .and(BookSpecification.hasGenres(genreId))
                    .and(BookSpecification.hasCategories(categoryId))
                    .and(BookSpecification.hasAuthors(authorId))
                    .and(BookSpecification.priceGreaterThanOrEqualTo(priceMin))
                    .and(BookSpecification.priceLessThanOrEqualTo(priceMax))
                    .and(BookSpecification.isOnSale(onSale));

            // 2. Wywołanie serwisu, który zwróci PaginatedResponse
            PaginatedBooksResponse response = bookService.getBooks(spec, sortBy, order, page, limit, lang);

            // 3. Zwracamy wynik
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Błąd serwera: " + e.getMessage());
        }
    }

    @GetMapping("/aggregated")
    public ResponseEntity<?> getAggregatedFilterData(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Integer> genreId,
            @RequestParam(required = false) List<Integer> categoryId,
            @RequestParam(required = false) List<Integer> authorId,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) Boolean onSale,
            @RequestParam(defaultValue = "pl") String lang
    ) {
        try {
            Specification<Book> spec = Specification.where(BookSpecification.titleContains(search))
                    .or(BookSpecification.authorContains(search))
                    .and(BookSpecification.hasGenres(genreId))
                    .and(BookSpecification.hasCategories(categoryId))
                    .and(BookSpecification.hasAuthors(authorId))
                    .and(BookSpecification.priceGreaterThanOrEqualTo(priceMin))
                    .and(BookSpecification.priceLessThanOrEqualTo(priceMax))
                    .and(BookSpecification.isOnSale(onSale));

            AggregatedFilterData data = bookService.buildAggregatedFilterData(spec, lang);

            return ResponseEntity.ok(data);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Błąd serwera: " + e.getMessage());
        }
    }

    @GetMapping("/random")
    public ResponseEntity<?> getRandomBooks() {
        try {
            // W serwisie losujemy i zwracamy listę
            List<BookDTO> randomBooksDto = bookService.getRandomBooksDTO("pl");
            return ResponseEntity.ok(randomBooksDto);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Błąd serwera: " + e.getMessage());
        }
    }

}
