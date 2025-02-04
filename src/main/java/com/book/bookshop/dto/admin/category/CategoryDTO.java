package com.book.bookshop.dto.admin.category;

import com.book.bookshop.models.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {
    private Integer id;
    private String name;
    private LocalDateTime createdAt;

    public CategoryDTO(Category category, String lang) {
        this.id = category.getCategoryId();
        this.createdAt = category.getCreatedAt();

        if ("en".equalsIgnoreCase(lang)) {
            this.name = category.getNameEn();
        } else {
            this.name = category.getNamePl();
        }
    }

}

