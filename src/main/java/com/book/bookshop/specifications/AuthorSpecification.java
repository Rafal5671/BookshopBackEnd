package com.book.bookshop.specifications;

import com.book.bookshop.models.Author;
import org.springframework.data.jpa.domain.Specification;

public class AuthorSpecification {

    public static Specification<Author> nameContains(String query) {
        return (root, queryObj, builder) -> {
            if (query == null || query.isBlank()) {
                return builder.conjunction();
            }
            String pattern = "%" + query.toLowerCase() + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("firstName")), pattern),
                    builder.like(builder.lower(root.get("lastName")), pattern)
            );
        };
    }
}

