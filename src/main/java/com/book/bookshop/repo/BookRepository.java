package com.book.bookshop.repo;

import com.book.bookshop.models.Book;
import com.book.bookshop.models.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {
    @Query("SELECT b FROM Book b JOIN b.authors a WHERE LOWER(a.firstName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(b.titlePl) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(b.titleEn) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(b.originalTitle) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Book> searchBooksByTitleAndAuthor(String query);
    boolean existsByPublisher(Publisher publisher);
    List<Book> findByGenres_GenreId(Integer genreId);
}

