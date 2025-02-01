package com.book.bookshop.controllers;

import com.book.bookshop.dto.*;
import com.book.bookshop.models.Book;
import com.book.bookshop.models.Genre;
import com.book.bookshop.repo.BookRepository;
import com.book.bookshop.service.BookService;
import com.book.bookshop.specifications.BookSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.ZoneId;
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

    private BookDTO convertToDTO(Book book) {
        List<String> genres = book.getGenres().stream()
                .map(genre -> genre.getName())
                .collect(Collectors.toList());

        PublisherDTO publisherDTO = null;
        if (book.getPublisher() != null) {
            publisherDTO = new PublisherDTO(book.getPublisher().getName());
        }

        List<AuthorDTO> authorsDTO = book.getAuthors().stream()
                .map(author -> new AuthorDTO(author.getFirstName(), author.getLastName()))
                .collect(Collectors.toList());

        List<ReviewProductDTO> reviewsDTO = book.getReviews().stream()
                .map(review -> new ReviewProductDTO(review.getReviewId(),
                        review.getCustomer().getFirstName(), review.getRating(),
                        review.getCommentPl()))
                .collect(Collectors.toList());

        return new BookDTO(
                book.getBookId(),
                book.getTitlePl(),
                book.getTitleEn(),
                book.getOriginalTitle(),
                book.getPrice(),
                book.getDiscountPrice(),
                book.getDescriptionPl(),
                book.getDescriptionEn(),
                book.getStockQuantity(),
                book.getImageUrl(),
                book.getPagesCount(),
                book.getCoverType(),
                book.getLanguage(),
                genres,
                book.getRelease_date() != null ? book.getRelease_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null,
                publisherDTO,
                authorsDTO,
                reviewsDTO
        );
    }

    @GetMapping("/genres")
    public List<GenreDTO> getAllGenres() {
        return bookService.getAllGenres().stream()
                .map(genre -> new GenreDTO(genre.getGenreId(), genre.getName()))
                .collect(Collectors.toList());
    }

    @GetMapping("/categories")
    public List<CategoryDTO> getAllCategories() {
        return bookService.getAllCategories().stream()
                .map(category -> new CategoryDTO(category.getCategoryId(), category.getNamePl(), category.getNameEn(), category.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @GetMapping("/authors")
    public List<AuthorCreateDTO> getAllAuthors() {
        return bookService.getAllAuthors().stream()
                .map(author -> new AuthorCreateDTO(
                        author.getAuthorId(),
                        author.getFirstName(),
                        author.getLastName()
                ))
                .collect(Collectors.toList());
    }
    private BookLangDTO convertToDTOLang(Book book, String lang) {
        List<String> genres = book.getGenres().stream()
                .map(Genre::getName)
                .collect(Collectors.toList());

        PublisherDTO publisherDTO = (book.getPublisher() != null)
                ? new PublisherDTO(book.getPublisher().getName())
                : null;

        List<AuthorDTO> authorsDTO = book.getAuthors().stream()
                .map(author -> new AuthorDTO(author.getFirstName(), author.getLastName()))
                .collect(Collectors.toList());

        List<ReviewProductDTO> reviewsDTO = book.getReviews().stream()
                .map(review -> new ReviewProductDTO(
                        review.getReviewId(),
                        review.getCustomer().getFirstName(),
                        review.getRating(),
                        review.getCommentPl())) // Możesz również dynamicznie obsłużyć język w komentarzach, jeśli są wielojęzyczne
                .collect(Collectors.toList());

        // Dynamiczne przypisanie tytułu i opisu w zależności od języka
        String title;
        String description;

        if ("en".equalsIgnoreCase(lang)) {
            if (book.getTitleEn() != null && !book.getTitleEn().isEmpty()) {
                title = book.getTitleEn();
            } else if (book.getOriginalTitle() != null && !book.getOriginalTitle().isEmpty()) {
                title = book.getOriginalTitle();
            } else {
                title = book.getTitlePl(); // Zwróć tytuł PL, jeśli brak tytułu EN i oryginalnego
            }
            description = book.getDescriptionEn();
        } else {
            if (book.getTitlePl() != null && !book.getTitlePl().isEmpty()) {
                title = book.getTitlePl();
            } else {
                title = book.getOriginalTitle(); // Jeśli brak tytułu PL, zwracamy tytuł oryginalny
            }
            description = book.getDescriptionPl();
        }
        return new BookLangDTO(
                book.getBookId(),
                title,
                book.getOriginalTitle(),
                book.getPrice(),
                book.getDiscountPrice(),
                description,
                book.getStockQuantity(),
                book.getImageUrl(),
                book.getPagesCount(),
                book.getCoverType(),
                book.getLanguage(),
                genres,
                book.getRelease_date() != null ? book.getRelease_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null,
                publisherDTO,
                authorsDTO,
                reviewsDTO
        );
    }
    @GetMapping("/{id}")
    public ResponseEntity<BookLangDTO> getBookById(@PathVariable Integer id,
                                               @RequestParam(defaultValue = "pl") String lang) {
        return bookRepository.findById(id)
                .map(book -> ResponseEntity.ok(convertToDTOLang(book, lang)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    // Usunięto endpoint /search

    @GetMapping("/genre/{genreId}")
    public List<BookDTO> getBooksByGenre(@PathVariable Integer genreId) {
        return bookService.findBooksByGenre(genreId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping
    public ResponseEntity<?> getBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Integer> genreId, // Zmienione na List<Integer>
            @RequestParam(required = false) List<Integer> categoryId, // Zmienione na List<Integer>
            @RequestParam(required = false) List<Integer> authorId, // Jeśli obsługujesz listę autorów, zmień na List<Integer>
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
            // Budowanie specyfikacji na podstawie parametrów filtrów
            Specification<Book> spec = Specification.where(BookSpecification.titleContains(search))
                    .or(BookSpecification.authorContains(search))
                    .and(BookSpecification.hasGenres(genreId)) // Zmienione na hasGenres
                    .and(BookSpecification.hasCategories(categoryId)) // Zmienione na hasCategories
                    .and(BookSpecification.hasAuthors(authorId))
                    .and(BookSpecification.priceGreaterThanOrEqualTo(priceMin))
                    .and(BookSpecification.priceLessThanOrEqualTo(priceMax))
                    .and(BookSpecification.isOnSale(onSale));

            // Ustalanie sortowania
            Sort.Direction sortDirection = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(sortDirection, sortBy);

            // Ustalanie paginacji
            Pageable pageable = PageRequest.of(page - 1, limit, sort);

            Page<Book> bookPage = bookRepository.findAll(spec, pageable);
            List<BookLangDTO> bookDTOs = bookPage.getContent().stream()
                    .map(book -> convertToDTOLang(book, lang)) // Dynamiczna konwersja na podstawie języka
                    .collect(Collectors.toList());

            // Wyciąganie unikalnych gatunków, kategorii i autorów z wyników wyszukiwania
            Set<GenreDTO> genres = bookPage.getContent().stream()
                    .flatMap(book -> book.getGenres().stream())
                    .distinct()
                    .map(genre -> new GenreDTO(
                            genre.getGenreId(),
                            lang.equals("en") ? genre.getNameEn() : genre.getName()  // Dynamiczny wybór języka
                    ))
                    .collect(Collectors.toSet());

            Set<CategoryDTOGET> categories = bookPage.getContent().stream()
                    .map(Book::getCategory) // Zakładam, że masz pole `category` w Book
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(category -> new CategoryDTOGET(
                            category.getCategoryId(),
                            lang.equals("en") ? category.getNameEn() : category.getNamePl(),  // Dynamiczny wybór języka
                            category.getCreatedAt()
                    )).collect(Collectors.toSet());

            Set<AuthorCreateDTO> authors = bookPage.getContent().stream()
                    .flatMap(book -> book.getAuthors().stream())
                    .map(author -> new AuthorCreateDTO(author.getAuthorId(), author.getFirstName(), author.getLastName()))
                    .collect(Collectors.toSet());
            BigDecimal maxAvailablePrice = bookPage.getContent().stream()
                    .map(Book::getPrice)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            // Zwracanie wyników wraz z informacjami o paginacji i dostępnych filtrach
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

    public static class PaginatedResponse {
        private List<BookLangDTO> books;
        private int currentPage;
        private int totalPages;
        private long totalItems;
        private List<GenreDTO> availableGenres;
        private List<CategoryDTOGET> availableCategories;
        private List<AuthorCreateDTO> availableAuthors;
        private BigDecimal maxAvailablePrice;

        public PaginatedResponse(List<BookLangDTO> books, int currentPage, int totalPages, long totalItems,
                                 List<GenreDTO> availableGenres, List<CategoryDTOGET> availableCategories,
                                 List<AuthorCreateDTO> availableAuthors, BigDecimal maxAvailablePrice) {
            this.books = books;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.totalItems = totalItems;
            this.availableGenres = availableGenres;
            this.availableCategories = availableCategories;
            this.availableAuthors = availableAuthors;
            this.maxAvailablePrice = maxAvailablePrice;
        }

        // Gettery i Settery
        public List<BookLangDTO> getBooks() { return books; }
        public void setBooks(List<BookLangDTO> books) { this.books = books; }

        public int getCurrentPage() { return currentPage; }
        public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

        public long getTotalItems() { return totalItems; }
        public void setTotalItems(long totalItems) { this.totalItems = totalItems; }

        public List<GenreDTO> getAvailableGenres() { return availableGenres; }
        public void setAvailableGenres(List<GenreDTO> availableGenres) { this.availableGenres = availableGenres; }

        public List<CategoryDTOGET> getAvailableCategories() { return availableCategories; }
        public void setAvailableCategories(List<CategoryDTOGET> availableCategories) { this.availableCategories = availableCategories; }

        public List<AuthorCreateDTO> getAvailableAuthors() { return availableAuthors; }
        public void setAvailableAuthors(List<AuthorCreateDTO> availableAuthors) { this.availableAuthors = availableAuthors; }
        public BigDecimal getMaxAvailablePrice() { return maxAvailablePrice; }
        public void setMaxAvailablePrice(BigDecimal maxPrice) { this.maxAvailablePrice = maxPrice; }
    }

}
