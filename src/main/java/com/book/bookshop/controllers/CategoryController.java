package com.book.bookshop.controllers;

import com.book.bookshop.dto.admin.category.CategoryDTO;
import com.book.bookshop.dto.GenreDTO;
import com.book.bookshop.models.Category;
import com.book.bookshop.models.Genre;
import com.book.bookshop.service.CategoryService;
import com.book.bookshop.service.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private GenreService genreService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories(
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "pl") String language
    ) {
        List<Category> categories = categoryService.findAll();
        List<CategoryDTO> dtos = categories.stream()
                .map(cat -> new CategoryDTO(cat, language))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(
            @PathVariable Integer id,
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "pl") String language
    ) {
        return categoryService.findById(id)
                .map(category -> ResponseEntity.ok(new CategoryDTO(category, language)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        return categoryService.save(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Integer id,
                                                   @RequestBody Category category) {
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

    @GetMapping("/genres/{categoryId}")
    public ResponseEntity<List<GenreDTO>> getGenresByCategoryId(
            @PathVariable Integer categoryId,
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "pl") String language
    ) {
        List<Genre> genres = genreService.findGenresByCategoryId(categoryId);

        List<GenreDTO> genreDTOs = genres.stream()
                .map(genre -> new GenreDTO(
                        genre.getGenreId(),
                        language.startsWith("en") ? genre.getNameEn() : genre.getName()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(genreDTOs);
    }
}
