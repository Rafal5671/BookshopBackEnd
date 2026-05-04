package com.book.bookshop.controllers;

import com.book.bookshop.models.Author;
import com.book.bookshop.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authors")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    @GetMapping
    public ResponseEntity<Page<Author>> getAuthorsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Page<Author> authorsPage = authorService.getAuthorsPage(page, size);
        return ResponseEntity.ok(authorsPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable Integer id) {
        return authorService.getAuthorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Author> createAuthor(@RequestBody Author author) {
        Author savedAuthor = authorService.createAuthor(author);
        // Zwracamy 201 Created lub 200 OK
        return ResponseEntity.ok(savedAuthor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Integer id) {
        boolean deleted = authorService.deleteAuthor(id);
        if (!deleted) {
            // Autor nie istniał
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
