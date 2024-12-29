package com.book.bookshop.controllers;

import com.book.bookshop.dto.AuthorDTO;
import com.book.bookshop.dto.BookDTO;
import com.book.bookshop.dto.PublisherDTO;
import com.book.bookshop.models.Book;
import com.book.bookshop.repo.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class BooksController {

    @Autowired
    private BookRepository bookRepository;

    private BookDTO convertToDTO(Book book) {
        List<String> genres = book.getGenres().stream()
                .map(genre -> genre.getName())  // Zakładając, że Genre ma metodę getName()
                .collect(Collectors.toList());

        // Mapowanie publishera
        PublisherDTO publisherDTO = null;
        if (book.getPublisher() != null) {
            publisherDTO = new PublisherDTO(book.getPublisher().getName());
        }

        // Mapowanie autorów
        List<AuthorDTO> authorsDTO = book.getAuthors().stream()
                .map(author -> new AuthorDTO(author.getFirstName(), author.getLastName()))
                .collect(Collectors.toList());

        return new BookDTO(
                book.getBookId(),
                book.getTitlePl(),
                book.getTitleEn(),
                book.getOriginalTitle(),
                book.getPrice(),
                book.getDiscountPrice(),
                book.getDescriptionPl(),
                book.getDescriptionEn(),
                book.getStockQuantity(),
                book.getImageUrl(),
                book.getPagesCount(),
                book.getCoverType(),
                book.getLanguage(),
                genres,
                book.getRelease_date() != null ? book.getRelease_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null,
                publisherDTO,
                authorsDTO  // Przekazanie listy autorów do DTO
        );
    }



    @GetMapping
    public List<BookDTO> getBooks(@RequestParam(defaultValue = "12") int limit) {
        return bookRepository.findAll().stream()
                .limit(limit)
                .map(this::convertToDTO)  // Przekształcamy Book na BookDTO
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Integer id) {
        return bookRepository.findById(id)
                .map(book -> ResponseEntity.ok(convertToDTO(book)))  // Konwersja Book na BookDTO
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

}
