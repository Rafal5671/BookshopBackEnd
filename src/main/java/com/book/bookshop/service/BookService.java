package com.book.bookshop.service;

import com.book.bookshop.models.Book;
import com.book.bookshop.repo.BookRepository;
import com.book.bookshop.repo.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Optional<Book> findById(Integer id) {
        return bookRepository.findById(id);
    }
    public void saveBook(Book book) {
        bookRepository.save(book);
    }
    public Book save(Book book) {
        // If the publisher exists, it will automatically be set via the publisher object in the book.
        // Ensure the publisher is saved before the book if needed.
        if (book.getPublisher() != null && book.getPublisher().getPublisherId() == null) {
            // Save publisher if it is new
            publisherRepository.save(book.getPublisher());
        }
        return bookRepository.save(book);
    }

    public void deleteById(Integer id) {
        bookRepository.deleteById(id);
    }
}
