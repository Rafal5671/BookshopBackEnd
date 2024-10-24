package com.book.bookshop.repo;

import com.book.bookshop.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Integer> {}
