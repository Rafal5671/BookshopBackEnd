package com.book.bookshop.repo;

import com.book.bookshop.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Integer> {}