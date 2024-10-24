package com.book.bookshop.controllers;

import com.book.bookshop.models.Author;
import com.book.bookshop.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {
    @Autowired
    private AuthorService authorService;

    @GetMapping
    public List<Author> getAllAuthors() {
        return authorService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable Integer id) {
        return authorService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Author createAuthor(@RequestBody Author author) {
        return authorService.save(author);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Author> updateAuthor(@PathVariable Integer id, @RequestBody Author author) {
        if (authorService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        author.setAuthorId(id);
        return ResponseEntity.ok(authorService.save(author));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Integer id) {
        if (authorService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        authorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

