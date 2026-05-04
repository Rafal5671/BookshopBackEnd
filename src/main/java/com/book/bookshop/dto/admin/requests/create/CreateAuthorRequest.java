package com.book.bookshop.dto.admin.requests.create;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAuthorRequest {
    private String firstName;
    private String lastName;
}

