package com.book.bookshop.repo;

import com.book.bookshop.models.Book;
import com.book.bookshop.models.Publisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {
    boolean existsByPublisher(Publisher publisher);
    @Query(value = "SELECT * FROM books ORDER BY RANDOM() LIMIT 12", nativeQuery = true)
    List<Book> findRandomBooks();

}

