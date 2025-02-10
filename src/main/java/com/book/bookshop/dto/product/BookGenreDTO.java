package com.book.bookshop.dto.product;

import com.book.bookshop.models.Genre;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookGenreDTO {
    private Integer genreId;
    private String name;

    /**
     * Konstruktor tworzący DTO na podstawie obiektu domenowego Genre oraz podanego języka.
     * Jeśli język to "en", próbuje wykorzystać nazwę angielską, a w razie braku – polską.
     * W przeciwnym przypadku (np. "pl") używa nazwy polskiej.
     *
     * @param genre obiekt domenowy reprezentujący gatunek
     * @param lang  kod języka ("en", "pl", itp.)
     */
    public BookGenreDTO(Genre genre, String lang) {
        this.genreId = genre.getGenreId();
        if ("en".equalsIgnoreCase(lang)) {
            // Jeśli dostępna jest nazwa angielska, używamy jej; w przeciwnym razie fallback do nazwy polskiej.
            this.name = (genre.getNameEn() != null && !genre.getNameEn().isEmpty())
                    ? genre.getNameEn()
                    : genre.getName();
        } else {
            // Dla języka polskiego lub innego domyślnie używamy nazwy polskiej.
            this.name = (genre.getName() != null && !genre.getName().isEmpty())
                    ? genre.getName()
                    : genre.getNameEn();
        }
    }
}
