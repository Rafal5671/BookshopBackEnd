package com.book.bookshop.controllers;

import com.book.bookshop.models.Category;
import com.book.bookshop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Integer id) {
        return categoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        return categoryService.save(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        if (categoryService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        category.setCategoryId(id);
        return ResponseEntity.ok(categoryService.save(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        if (categoryService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
