package com.book.bookshop.dto;

public class GenreDTO {
    private Integer genreId;
    private String name;

    // Constructor
    public GenreDTO(Integer genreId, String name) {
        this.genreId = genreId;
        this.name = name;
    }

    // Getters and Setters
    public Integer getGenreId() {
        return genreId;
    }

    public void setGenreId(Integer genreId) {
        this.genreId = genreId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

