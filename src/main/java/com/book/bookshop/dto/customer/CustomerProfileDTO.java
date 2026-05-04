package com.book.bookshop.dto.customer;

import com.book.bookshop.dto.order.OrderDTO;
import com.book.bookshop.dto.review.ReviewProfileDTO;
import lombok.Data;

import java.util.List;

@Data
public class CustomerProfileDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String createdAt;
    private List<OrderDTO> orders;
    private List<ReviewProfileDTO> reviews;
}
