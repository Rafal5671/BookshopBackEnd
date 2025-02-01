package com.book.bookshop.repo;

import com.book.bookshop.models.Book;
import com.book.bookshop.models.Customer;
import com.book.bookshop.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Review findByBookAndCustomer(Book book, Customer customer);
    List<Review> findByBook(Book book);

}