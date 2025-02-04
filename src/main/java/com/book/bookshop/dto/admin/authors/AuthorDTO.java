package com.book.bookshop.dto.admin.authors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorDTO {
    private Integer authorId;
    private String firstName;
    private String lastName;
}
