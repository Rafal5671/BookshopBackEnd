package com.book.bookshop.service;

import com.book.bookshop.dto.admin.category.CategoryDTO;
import com.book.bookshop.models.Category;
import com.book.bookshop.repo.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> findAll() { return categoryRepository.findAll(); }

    public Optional<Category> findById(Integer id) {
        return categoryRepository.findById(id);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteById(Integer id) {
        categoryRepository.deleteById(id);
    }

    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    public List<CategoryDTO> getAllCategoryDTOs(String language) {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(cat -> new CategoryDTO(cat, language))
                .collect(Collectors.toList());
    }

    public Optional<CategoryDTO> getCategoryDTOById(Integer id, String language) {
        return categoryRepository.findById(id)
                .map(category -> new CategoryDTO(category, language));
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Optional<Category> updateCategory(Integer id, Category category) {
        return categoryRepository.findById(id)
                .map(existing -> {

                    category.setCategoryId(id);
                    return categoryRepository.save(category);
                });
    }

    public boolean deleteCategory(Integer id) {
        return categoryRepository.findById(id)
                .map(category -> {
                    categoryRepository.deleteById(id);
                    return true;
                })
                .orElse(false);
    }
}
