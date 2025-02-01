package com.book.bookshop.mapper;

import com.book.bookshop.dto.CustomerDTO;
import com.book.bookshop.models.Customer;

public class CustomerMapper {
    public static CustomerDTO toDto(Customer customer) {
        if (customer == null) {
            return null;
        }
        CustomerDTO dto = new CustomerDTO();
        dto.setEmail(customer.getEmail());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setPhone(customer.getPhone());
        dto.setCreatedAt(customer.getCreatedAt());
        return dto;
    }
}

