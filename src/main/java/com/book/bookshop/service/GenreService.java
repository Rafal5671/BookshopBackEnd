package com.book.bookshop.service;

import com.book.bookshop.models.Genre;
import com.book.bookshop.repo.GenreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenreService {

    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public List<Genre> findGenresByCategoryId(Integer categoryId) {
        return genreRepository.findByCategoryCategoryId(categoryId);
    }
}
