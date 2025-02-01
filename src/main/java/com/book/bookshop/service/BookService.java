package com.book.bookshop.service;

import com.book.bookshop.models.Author;
import com.book.bookshop.models.Book;
import com.book.bookshop.models.Category;
import com.book.bookshop.models.Genre;
import com.book.bookshop.repo.*;
import com.book.bookshop.specifications.BookSpecification;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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

}
