package com.book.bookshop.controllers;

import com.book.bookshop.dto.CategoryDTO;
import com.book.bookshop.dto.CategoryGetDTO;
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
    public List<CategoryDTO> getAllCategories() {
        return categoryService.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    private CategoryDTO convertToDTO(Category category) {
        return new CategoryDTO(
                category.getCategoryId(),
                category.getNameEn(),
                category.getNamePl(),
                category.getCreatedAt()
        );
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

    @GetMapping("/genres/{categoryId}")
    public ResponseEntity<List<GenreDTO>> getGenresByCategoryId(
            @PathVariable Integer categoryId,
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "pl") String language) {
        List<Genre> genres = genreService.findGenresByCategoryId(categoryId);

        // Określamy, które pole zwrócić na podstawie języka
        List<GenreDTO> genreDTOs = genres.stream()
                .map(genre -> new GenreDTO(
                        genre.getGenreId(),
                        language.startsWith("pl") ? genre.getName() : genre.getNameEn()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(genreDTOs);
    }
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryGetDTO>> getCategories(
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "pl") String language) {

        List<Category> categories = categoryService.findAllCategories();

        List<CategoryGetDTO> categoryDTOs = categories.stream()
                .map(category -> new CategoryGetDTO(
                        category.getCategoryId(),
                        language.startsWith("pl") ? category.getNamePl() : category.getNameEn()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(categoryDTOs);
    }
}
