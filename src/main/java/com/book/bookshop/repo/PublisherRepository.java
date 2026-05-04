package com.book.bookshop.repo;

import com.book.bookshop.models.Publisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Integer> {
    Page<Publisher> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
