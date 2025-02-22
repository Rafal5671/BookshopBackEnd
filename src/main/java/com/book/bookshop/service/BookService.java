package com.book.bookshop.service;

import com.book.bookshop.dto.GenreDTO;
import com.book.bookshop.dto.admin.authors.AuthorDTO;
import com.book.bookshop.dto.admin.category.CategoryDTO;
import com.book.bookshop.dto.product.BookDTO;
import com.book.bookshop.dto.response.AggregatedFilterData;
import com.book.bookshop.dto.response.PaginatedBooksResponse;
import com.book.bookshop.models.Author;
import com.book.bookshop.models.Book;
import com.book.bookshop.models.Category;
import com.book.bookshop.models.Genre;
import com.book.bookshop.repo.*;
import com.book.bookshop.specifications.BookSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private PublisherRepository publisherRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private AuthorRepository authorRepository;

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Optional<Book> findById(Integer id) {
        return bookRepository.findById(id);
    }
    public void saveBook(Book book) {
        bookRepository.save(book);
    }
    public Book save(Book book) {
        // If the publisher exists, it will automatically be set via the publisher object in the book.
        // Ensure the publisher is saved before the book if needed.
        if (book.getPublisher() != null && book.getPublisher().getPublisherId() == null) {
            // Save publisher if it is new
            publisherRepository.save(book.getPublisher());
        }
        return bookRepository.save(book);
    }

    public void deleteById(Integer id) {
        bookRepository.deleteById(id);
    }
    public AggregatedFilterData buildAggregatedFilterData(Specification<Book> spec, String lang) {
        // 1. Pobieramy wszystkie książki na podstawie specyfikacji
        List<Book> allBooks = bookRepository.findAll(spec);

        // 2. Zbieramy unikalne gatunki
        Set<GenreDTO> genres = allBooks.stream()
                .flatMap(b -> b.getGenres().stream())
                .distinct()
                .map(genre -> new GenreDTO(
                        genre.getGenreId(),
                        (lang.equals("en") && genre.getNameEn() != null) ? genre.getNameEn() : genre.getName()
                ))
                .collect(Collectors.toSet());

        // 3. Zbieramy unikalne kategorie
        Set<CategoryDTO> categories = allBooks.stream()
                .map(Book::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .map(category -> new CategoryDTO(category, lang))
                .collect(Collectors.toSet());

        // 4. Zbieramy unikalnych autorów
        Set<AuthorDTO> authors = allBooks.stream()
                .flatMap(b -> b.getAuthors().stream())
                .distinct()
                .map(author -> new AuthorDTO(
                        author.getAuthorId(),
                        author.getFirstName(),
                        author.getLastName()
                ))
                .collect(Collectors.toSet());

        // 5. Wyznaczamy maksymalną cenę
        BigDecimal maxAvailablePrice = allBooks.stream()
                .map(Book::getPrice)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // 6. Tworzymy obiekt `AggregatedFilterData`
        return new AggregatedFilterData(
                new ArrayList<>(genres),
                new ArrayList<>(categories),
                new ArrayList<>(authors),
                maxAvailablePrice
        );
    }

    public Page<Book> getFilteredBooks(String search, List<Integer> genreIds, List<Integer> categoryIds,
                                       BigDecimal priceMin, BigDecimal priceMax, Boolean onSale,
                                       String sortBy, String order, int page, int limit) {
        Specification<Book> spec = Specification.where(BookSpecification.titleContains(search))
                .and(BookSpecification.hasGenres(genreIds))
                .and(BookSpecification.hasCategories(categoryIds))
                .and(BookSpecification.priceGreaterThanOrEqualTo(priceMin))
                .and(BookSpecification.priceLessThanOrEqualTo(priceMax))
                .and(BookSpecification.isOnSale(onSale));

        Sort.Direction sortDirection = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(sortDirection, sortBy);

        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        return bookRepository.findAll(spec, pageable);
    }

    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    public List<Book> findBooksByGenre(Integer genreId) {
        return bookRepository.findAll(BookSpecification.hasGenre(genreId));
    }
    public Optional<BookDTO> getBookByIdDTO(Integer id, String lang) {
        return bookRepository.findById(id)
                .map(book -> new BookDTO(book, lang));
    }

    public List<GenreDTO> getAllGenresDTO() {
        return genreRepository.findAll().stream()
                .map(genre -> new GenreDTO(genre.getGenreId(), genre.getName()))
                .collect(Collectors.toList());
    }

    public List<CategoryDTO> getAllCategoriesDTO(String lang) {
        return categoryRepository.findAll().stream()
                .map(category -> new CategoryDTO(category, lang))
                .collect(Collectors.toList());
    }

    public List<AuthorDTO> getAllAuthorsDTO() {
        return authorRepository.findAll().stream()
                .map(author -> new AuthorDTO(author.getAuthorId(), author.getFirstName(), author.getLastName()))
                .collect(Collectors.toList());
    }

    public PaginatedBooksResponse getBooks(
            Specification<Book> spec,
            String sortBy,
            String order,
            int page,
            int limit,
            String lang
    ) {
        // 1. Ustalenie pola sortowania (domyślnie "titlePl")
        String finalSortBy = "titlePl";
        String finalOrder = "asc";

        if (!sortBy.equals("default")) {
            if (sortBy.startsWith("price")) {
                finalSortBy = "price";
                finalOrder = sortBy.endsWith("Asc") ? "asc" : "desc";
            } else if (sortBy.startsWith("title")) {
                finalSortBy = "titlePl";
                finalOrder = sortBy.endsWith("Asc") ? "asc" : "desc";
            }
        }

        // 2. Budowa obiektu Sort
        Sort.Direction sortDirection = finalOrder.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Sort sort = Sort.by(sortDirection, finalSortBy);

        // 3. Pageable
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        // 4. Wykonanie zapytania do bazy
        Page<Book> bookPage = bookRepository.findAll(spec, pageable);

        // 5. Mapowanie każdej książki na BookDTO
        List<BookDTO> bookDTOs = bookPage.getContent().stream()
                .map(book -> new BookDTO(book, lang))
                .collect(Collectors.toList());

        // 6. Zbudowanie PaginatedResponse
        return new PaginatedBooksResponse(
                bookDTOs,
                bookPage.getNumber() + 1,
                bookPage.getTotalPages(),
                bookPage.getTotalElements()
        );
    }

    public List<BookDTO> getRandomBooksDTO(String lang) {
        // bookRepository.findRandomBooks() – np. @Query z LIMIT 12
        List<Book> randomBooks = bookRepository.findRandomBooks();
        return randomBooks.stream()
                .map(book -> new BookDTO(book, lang))
                .collect(Collectors.toList());
    }

    public List<BookDTO> findBooksByGenreDTO(Integer genreId, String lang) {
        return findBooksByGenre(genreId).stream()
                .map(book -> new BookDTO(book, lang))
                .collect(Collectors.toList());
    }
}
