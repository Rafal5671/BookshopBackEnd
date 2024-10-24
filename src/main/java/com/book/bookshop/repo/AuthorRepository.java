package com.book.bookshop.repo;

import com.book.bookshop.models.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Integer> {}
