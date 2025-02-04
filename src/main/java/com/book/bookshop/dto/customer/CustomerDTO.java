package com.book.bookshop.dto.customer;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomerDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDateTime createdAt;
}

