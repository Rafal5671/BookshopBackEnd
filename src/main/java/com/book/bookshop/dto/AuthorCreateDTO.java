package com.book.bookshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorCreateDTO {
    private Integer authorId;
    private String firstName;
    private String lastName;
}
