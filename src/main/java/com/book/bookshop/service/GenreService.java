package com.book.bookshop.service;

import com.book.bookshop.dto.GenreDTO;
import com.book.bookshop.models.Genre;
import com.book.bookshop.repo.GenreRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GenreService {

    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public List<Genre> findGenresByCategoryId(Integer categoryId) {
        return genreRepository.findByCategoryCategoryId(categoryId);
    }
    public List<GenreDTO> getGenreDTOsByCategoryId(Integer categoryId, String language) {
        List<Genre> genres = findGenresByCategoryId(categoryId);
        return genres.stream()
                .map(genre -> new GenreDTO(
                        genre.getGenreId(),
                        language.startsWith("en") ? genre.getNameEn() : genre.getName()
                ))
                .collect(Collectors.toList());
    }
}
