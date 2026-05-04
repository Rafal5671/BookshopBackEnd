package com.book.bookshop.repo;

import com.book.bookshop.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Customer findByEmail(String email);

    @Query("""
       SELECT c 
       FROM Customer c
       LEFT JOIN FETCH c.orders o
       LEFT JOIN FETCH o.items oi
       LEFT JOIN FETCH oi.book b
       LEFT JOIN FETCH c.reviews r
       WHERE c.email = :email
    """)
    Customer findByEmailWithOrdersAndReviews(@Param("email") String email);
}
