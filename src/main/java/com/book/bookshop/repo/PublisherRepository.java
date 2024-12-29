package com.book.bookshop.repo;

import com.book.bookshop.models.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Integer> {
    // Add any custom queries here if needed
}
