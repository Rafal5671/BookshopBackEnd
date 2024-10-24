package com.book.bookshop.repo;

import com.book.bookshop.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Integer> {}