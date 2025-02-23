package com.book.bookshop.service;

import com.book.bookshop.models.Author;
import com.book.bookshop.repo.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthorService {
    @Autowired
    private AuthorRepository authorRepository;

    public Page<Author> findAll(Pageable pageable) {
        return authorRepository.findAll(pageable);
    }

    public Optional<Author> findById(Integer id) {
        return authorRepository.findById(id);
    }

    public Author save(Author author) {
        return authorRepository.save(author);
    }

    public void deleteById(Integer id) {
        authorRepository.deleteById(id);
    }
    public Page<Author> getAuthorsPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("authorId").ascending());
        return authorRepository.findAll(pageable);
    }

    public Optional<Author> getAuthorById(Integer id) {
        return authorRepository.findById(id);
    }

    public Author createAuthor(Author author) {
        return authorRepository.save(author);
    }

    public boolean deleteAuthor(Integer id) {
        Optional<Author> authorOpt = authorRepository.findById(id);
        if (authorOpt.isEmpty()) {
            return false;
        }
        authorRepository.deleteById(id);
        return true;
    }
}
