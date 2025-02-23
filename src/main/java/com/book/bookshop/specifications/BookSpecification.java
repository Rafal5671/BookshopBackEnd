package com.book.bookshop.specifications;

import com.book.bookshop.models.Author;
import com.book.bookshop.models.Book;
import com.book.bookshop.models.Category;
import com.book.bookshop.models.Genre;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> titleContains(String search) {
        return (root, query, builder) -> {
            if (search == null || search.isEmpty()) {
                return builder.conjunction();
            }
            // Podział zapytania na słowa kluczowe
            String[] keywords = search.toLowerCase().split("\\s+");
            Predicate predicate = builder.conjunction();
            for (String keyword : keywords) {
                predicate = builder.and(predicate, builder.like(builder.lower(root.get("titlePl")), "%" + keyword + "%"));
            }
            return predicate;
        };
    }

    public static Specification<Book> authorContains(String search) {
        return (root, query, builder) -> {
            if (search == null || search.isEmpty()) {
                return builder.conjunction();
            }
            String[] keywords = search.toLowerCase().split("\\s+");
            Predicate predicate = builder.conjunction();
            Join<Book, Author> authors = root.join("authors", JoinType.LEFT);
            for (String keyword : keywords) {
                predicate = builder.and(predicate,
                        builder.or(
                                builder.like(builder.lower(authors.get("firstName")), "%" + keyword + "%"),
                                builder.like(builder.lower(authors.get("lastName")), "%" + keyword + "%")
                        )
                );
            }
            return predicate;
        };
    }

    public static Specification<Book> hasGenre(Integer genreId) {
        return (root, query, builder) -> {
            if (genreId == null) {
                return null;
            }
            return builder.equal(root.join("genres").get("genreId"), genreId);
        };
    }

    public static Specification<Book> hasCategory(Integer categoryId) {
        return (root, query, builder) -> {
            if (categoryId == null) {
                return null;
            }
            return builder.equal(root.join("category").get("categoryId"), categoryId);
        };
    }
    public static Specification<Book> hasGenres(List<Integer> genreIds) {
        return (root, query, builder) -> {
            if (genreIds == null || genreIds.isEmpty()) {
                return builder.conjunction();
            }
            query.distinct(true);
            Join<Book, Genre> genres = root.join("genres", JoinType.INNER);
            return genres.get("genreId").in(genreIds);
        };
    }


    public static Specification<Book> hasCategories(List<Integer> categoryIds) {
        return (root, query, builder) -> {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return builder.conjunction();
            }
            Join<Book, Category> categories = root.join("category", JoinType.LEFT);
            return categories.get("categoryId").in(categoryIds);
        };
    }
    public static Specification<Book> priceGreaterThanOrEqualTo(BigDecimal priceMin) {
        return (root, query, builder) -> {
            if (priceMin == null) {
                return null;
            }
            return builder.greaterThanOrEqualTo(root.get("price"), priceMin);
        };
    }

    public static Specification<Book> priceLessThanOrEqualTo(BigDecimal priceMax) {
        return (root, query, builder) -> {
            if (priceMax == null) {
                return null;
            }
            return builder.lessThanOrEqualTo(root.get("price"), priceMax);
        };
    }
    public static Specification<Book> hasAuthors(List<Integer> authorIds) {
        return (root, query, criteriaBuilder) -> {
            if (authorIds == null || authorIds.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            query.distinct(true);
            Join<Book, Author> authorJoin = root.join("authors", JoinType.INNER);
            return authorJoin.get("authorId").in(authorIds);
        };
    }

    public static Specification<Book> isOnSale(Boolean onSale) {
        if (onSale == null || !onSale) {
            return null;
        }
        return (root, query, builder) -> builder.isNotNull(root.get("discountPrice"));
    }
    public static Specification<Book> isNew(Boolean isNew) {
        if (isNew == null || !isNew) {
            return null;
        }

        LocalDate oneYearAgo = LocalDate.now().minusYears(1);

        return (root, query, builder) -> builder.greaterThanOrEqualTo(
                root.get("releaseDate"), java.sql.Date.valueOf(oneYearAgo)
        );
    }

}
