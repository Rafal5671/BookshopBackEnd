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
public class CategoryAdminDTO {
    private Integer id;
    private String nameEn;
    private String namePl;
    private LocalDateTime createdAt;

    public CategoryAdminDTO(Category category){
        this.id = category.getCategoryId();
        this.nameEn = category.getNameEn();;
        this.namePl = category.getNamePl();;
        this.createdAt = getCreatedAt();
    }
}
