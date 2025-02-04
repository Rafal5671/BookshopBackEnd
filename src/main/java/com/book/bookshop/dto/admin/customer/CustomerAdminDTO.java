package com.book.bookshop.dto.admin.customer;

import com.book.bookshop.models.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
public class CustomerAdminDTO {
    private Integer customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private UserRole role;
}
