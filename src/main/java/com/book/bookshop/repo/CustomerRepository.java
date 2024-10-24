package com.book.bookshop.repo;

import com.book.bookshop.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Customer findByEmail(String email);
}
