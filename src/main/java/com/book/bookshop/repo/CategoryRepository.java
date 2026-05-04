package com.book.bookshop.repo;

import com.book.bookshop.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByNameEn(String nameEn);
    Optional<Category> findByNamePl(String namePl);
}