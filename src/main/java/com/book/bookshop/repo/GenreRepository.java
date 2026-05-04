package com.book.bookshop.repo;

import com.book.bookshop.models.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GenreRepository extends JpaRepository<Genre, Integer> {
    List<Genre> findByCategoryCategoryId(Integer categoryId);
}
