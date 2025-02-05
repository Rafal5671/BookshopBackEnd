package com.book.bookshop.controllers;

import com.book.bookshop.dto.*;
import com.book.bookshop.dto.admin.authors.AuthorDTO;
import com.book.bookshop.dto.admin.category.CategoryDTO;
import com.book.bookshop.dto.product.BookDTO;
import com.book.bookshop.models.Book;
import com.book.bookshop.repo.BookRepository;
import com.book.bookshop.service.BookService;
import com.book.bookshop.specifications.BookSpecification;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class BooksController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Integer id,
                                               @RequestParam(defaultValue = "pl") String lang) {
        return bookRepository.findById(id)
                .map(book -> ResponseEntity.ok(new BookDTO(book, lang)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @GetMapping("/genres")
    public List<GenreDTO> getAllGenres() {
        return bookService.getAllGenres().stream()
                .map(genre -> new GenreDTO(genre.getGenreId(), genre.getName()))
                .collect(Collectors.toList());
    }

    @GetMapping("/categories")
    public List<CategoryDTO> getAllCategories(
            @RequestParam(defaultValue = "pl") String lang
    ) {
        return bookService.getAllCategories().stream()
                .map(category -> new CategoryDTO(category, lang))
                .collect(Collectors.toList());
    }

    @GetMapping("/authors")
    public List<AuthorDTO> getAllAuthors() {
        return bookService.getAllAuthors().stream()
                .map(author -> new AuthorDTO(
                        author.getAuthorId(),
                        author.getFirstName(),
                        author.getLastName()
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/genre/{genreId}")
    public List<BookDTO> getBooksByGenre(@PathVariable Integer genreId,
                                         @RequestParam(defaultValue = "pl") String lang) {
        return bookService.findBooksByGenre(genreId)
                .stream()
                .map(book -> new BookDTO(book, lang))
                .collect(Collectors.toList());
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
            @RequestParam(defaultValue = "titlePl") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
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

            Sort.Direction sortDirection = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(sortDirection, sortBy);

            Pageable pageable = PageRequest.of(page - 1, limit, sort);

            Page<Book> bookPage = bookRepository.findAll(spec, pageable);

            List<BookDTO> bookDTOs = bookPage.getContent().stream()
                    .map(book -> new BookDTO(book, lang))
                    .collect(Collectors.toList());

            Set<GenreDTO> genres = bookPage.getContent().stream()
                    .flatMap(b -> b.getGenres().stream())
                    .distinct()
                    .map(genre -> new GenreDTO(
                            genre.getGenreId(),
                            (lang.equals("en") && genre.getNameEn() != null)
                                    ? genre.getNameEn()
                                    : genre.getName()
                    ))
                    .collect(Collectors.toSet());

            Set<CategoryDTO> categories = bookPage.getContent().stream()
                    .map(Book::getCategory)
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(category -> new CategoryDTO(category, lang)) // <-- używamy nowego konstruktora
                    .collect(Collectors.toSet());

            Set<AuthorDTO> authors = bookPage.getContent().stream()
                    .flatMap(b -> b.getAuthors().stream())
                    .map(author -> new AuthorDTO(
                            author.getAuthorId(),
                            author.getFirstName(),
                            author.getLastName()
                    ))
                    .collect(Collectors.toSet());

            BigDecimal maxAvailablePrice = bookPage.getContent().stream()
                    .map(Book::getPrice)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            return ResponseEntity.ok(new PaginatedResponse(
                    bookDTOs,
                    bookPage.getNumber() + 1,
                    bookPage.getTotalPages(),
                    bookPage.getTotalElements(),
                    new ArrayList<>(genres),
                    new ArrayList<>(categories),
                    new ArrayList<>(authors),
                    maxAvailablePrice
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Błąd serwera: " + e.getMessage());
        }
    }
    @Getter
    @Setter
    public static class PaginatedResponse {
        private List<BookDTO> books;
        private int currentPage;
        private int totalPages;
        private long totalItems;
        private List<GenreDTO> availableGenres;
        private List<CategoryDTO> availableCategories;
        private List<AuthorDTO> availableAuthors;
        private BigDecimal maxAvailablePrice;

        public PaginatedResponse(List<BookDTO> books,
                                 int currentPage,
                                 int totalPages,
                                 long totalItems,
                                 List<GenreDTO> availableGenres,
                                 List<CategoryDTO> availableCategories,
                                 List<AuthorDTO> availableAuthors,
                                 BigDecimal maxAvailablePrice) {
            this.books = books;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.totalItems = totalItems;
            this.availableGenres = availableGenres;
            this.availableCategories = availableCategories;
            this.availableAuthors = availableAuthors;
            this.maxAvailablePrice = maxAvailablePrice;
        }
    }
    @GetMapping("/random")
    public ResponseEntity<?> getRandomBooks() {
        try {
            // Pobieramy 12 losowych książek
            List<Book> randomBooks = bookRepository.findRandomBooks();

            // Mapowanie na DTO, zakładamy, że BookDTO przyjmuje encję Book oraz język (np. "pl")
            List<BookDTO> randomBooksDto = randomBooks.stream()
                    .map(book -> new BookDTO(book, "pl"))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(randomBooksDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Błąd serwera: " + e.getMessage());
        }
    }
}