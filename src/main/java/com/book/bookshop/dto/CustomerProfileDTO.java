package com.book.bookshop.dto;

import lombok.Data;
import org.springframework.data.domain.jaxb.SpringDataJaxb;

import java.util.List;

@Data
public class CustomerProfileDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String createdAt; // Zwracamy już jako sformatowany String
    private List<OrderDTO> orders;
    private List<ReviewProfileDTO> reviews;
}
