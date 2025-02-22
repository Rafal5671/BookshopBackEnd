package com.book.bookshop.controllers;

import com.book.bookshop.dto.admin.category.CategoryDTO;
import com.book.bookshop.dto.GenreDTO;
import com.book.bookshop.models.Category;
import com.book.bookshop.service.CategoryService;
import com.book.bookshop.service.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private GenreService genreService;

    // ---------------------------------------
    // 1. Pobranie wszystkich kategorii (DTO)
    // ---------------------------------------
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories(
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "pl") String language
    ) {
        List<CategoryDTO> dtos = categoryService.getAllCategoryDTOs(language);
        return ResponseEntity.ok(dtos);
    }

    // ---------------------------------------
    // 2. Pobranie kategorii po ID (DTO)
    // ---------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(
            @PathVariable Integer id,
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "pl") String language
    ) {
        Optional<CategoryDTO> categoryDTO = categoryService.getCategoryDTOById(id, language);
        return categoryDTO
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------------------------------------
    // 3. Utworzenie nowej kategorii
    // ---------------------------------------
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category created = categoryService.createCategory(category);
        return ResponseEntity.ok(created); // lub .status(HttpStatus.CREATED).body(created);
    }

    // ---------------------------------------
    // 4. Aktualizacja kategorii
    // ---------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Integer id,
            @RequestBody Category category
    ) {
        Optional<Category> updated = categoryService.updateCategory(id, category);
        return updated
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------------------------------------
    // 5. Usunięcie kategorii
    // ---------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        boolean deleted = categoryService.deleteCategory(id);
        if (deleted) {
            return ResponseEntity.noContent().build(); // 204, brak treści
        } else {
            return ResponseEntity.notFound().build(); // 404, nie znaleziono
        }
    }

    // ---------------------------------------
    // 6. Pobranie wszystkich Genre w danej kategorii
    // ---------------------------------------
    @GetMapping("/genres/{categoryId}")
    public ResponseEntity<List<GenreDTO>> getGenresByCategoryId(
            @PathVariable Integer categoryId,
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "pl") String language
    ) {
        List<GenreDTO> genreDTOs = genreService.getGenreDTOsByCategoryId(categoryId, language);
        return ResponseEntity.ok(genreDTOs);
    }
}
