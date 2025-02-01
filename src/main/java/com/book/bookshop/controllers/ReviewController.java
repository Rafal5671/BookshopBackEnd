package com.book.bookshop.controllers;

import com.book.bookshop.dto.ReviewCheckDTO;
import com.book.bookshop.dto.ReviewDTO;
import com.book.bookshop.dto.ReviewFetchDTO;
import com.book.bookshop.models.Book;
import com.book.bookshop.models.Customer;
import com.book.bookshop.models.Review;
import com.book.bookshop.service.BookService;
import com.book.bookshop.service.CustomerService;
import com.book.bookshop.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:3000")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BookService bookService; // Dodanie serwisu książek
    @Autowired
    private CustomerService customerService;
    // Pobierz wszystkie recenzje
    @GetMapping
    public List<Review> getAllReviews() {
        return reviewService.findAll();
    }

    // Pobierz recenzję po ID
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Integer id) {
        return reviewService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Dodaj recenzję do książki
    @PostMapping("/{bookId}")
    public ResponseEntity<?> createReview(
            @PathVariable Integer bookId,
            @RequestBody Map<String, Object> reviewData,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        System.out.println(email);
        Customer customer = customerService.findByEmail(email);
        // Znajdź książkę po ID
        Book book = bookService.findById(bookId).orElse(null);
        if (book == null) {
            return ResponseEntity.badRequest().body("Book not found.");
        }

        // Pobierz dane z żądania
        String commentPl = (String) reviewData.get("commentPl");
        String commentEn = (String) reviewData.get("commentEn");
        Integer rating = (Integer) reviewData.get("rating");

        if (commentPl == null && commentEn == null) {
            return ResponseEntity.badRequest().body("Comment is required.");
        }
        if (rating == null) {
            return ResponseEntity.badRequest().body("Rating is required.");
        }

        // Utwórz nową recenzję
        Review review = new Review();
        review.setBook(book);
        review.setCustomer(customer); // Przypisanie zalogowanego użytkownika
        review.setRating(rating);
        review.setCommentPl(commentPl);
        review.setCommentEn(commentEn);
        review.setReviewDate(LocalDateTime.now());
        review.setCreatedAt(LocalDateTime.now());

        // Zapisz recenzję
        Review savedReview = reviewService.save(review);

        // Zwróć odpowiedź
        return ResponseEntity.ok(Map.of("reviewId", savedReview.getReviewId()));
    }
    @GetMapping("/{bookId}/me")
    public ResponseEntity<ReviewCheckDTO> getUserReview(
            @PathVariable Integer bookId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Customer customer = customerService.findByEmail(email);
        Book book = bookService.findById(bookId).orElse(null);

        if (book == null) {
            return ResponseEntity.badRequest().body(null);
        }

        Review review = reviewService.findByBookAndCustomer(book, customer);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new ReviewCheckDTO(review));
    }



    // Aktualizuj recenzję
    @PutMapping("/{bookId}/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Integer bookId,
            @PathVariable Integer reviewId,
            @RequestBody Map<String, Object> reviewData,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Pobierz zalogowanego użytkownika
        String email = userDetails.getUsername();
        Customer customer = customerService.findByEmail(email);

        // Znajdź recenzję po ID
        Review existingReview = reviewService.findById(reviewId).orElse(null);
        if (existingReview == null) {
            return ResponseEntity.notFound().build();
        }

        // Sprawdź, czy recenzja należy do zalogowanego użytkownika
        if (!existingReview.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            return ResponseEntity.status(403).body("You are not authorized to update this review.");
        }

        // Sprawdź, czy recenzja dotyczy przekazanej książki
        if (!existingReview.getBook().getBookId().equals(bookId)) {
            return ResponseEntity.badRequest().body("Review does not belong to the specified book.");
        }

        // Pobierz dane wejściowe z żądania
        String commentPl = (String) reviewData.get("commentPl");
        String commentEn = (String) reviewData.get("commentEn");
        Integer rating = (Integer) reviewData.get("rating");

        // Aktualizuj pola recenzji, jeśli są obecne w żądaniu
        if (commentPl != null) existingReview.setCommentPl(commentPl);
        if (commentEn != null) existingReview.setCommentEn(commentEn);
        if (rating != null) existingReview.setRating(rating);

        // Zapisz zmienioną recenzję
        Review updatedReview = reviewService.save(existingReview);

        // Zwróć odpowiedź
        return ResponseEntity.ok(new ReviewCheckDTO(updatedReview));
    }
    @GetMapping("/book-reviews/{bookId}")
    public ResponseEntity<List<ReviewFetchDTO>> getBookReviews(@PathVariable Integer bookId) {
        // 1. Wyszukujemy książkę w bazie na podstawie przekazanego ID.
        Book book = bookService.findById(bookId).orElse(null);

        // 2. Jeśli książka nie została znaleziona, zwracamy błąd (400).
        if (book == null) {
            return ResponseEntity.badRequest().body(null);
        }

        // 3. Pobieramy listę recenzji dla tej książki.
        List<Review> reviews = reviewService.findByBook(book);

        // 4. Mapujemy obiekty Review na obiekty DTO (ReviewCheckDTO).
        List<ReviewFetchDTO> reviewDTOs = reviews.stream()
                .map(ReviewFetchDTO::new)
                .toList();

        // 5. Zwracamy listę recenzji w odpowiedzi z kodem 200 (OK).
        return ResponseEntity.ok(reviewDTOs);
    }
    // Usuń recenzję
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Customer customer = customerService.findByEmail(email);
        Review existingReview = reviewService.findById(id).orElse(null);
        if (existingReview == null) {
            return ResponseEntity.notFound().build();
        }

        // Sprawdź, czy recenzja należy do zalogowanego użytkownika
        if (!existingReview.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            return ResponseEntity.status(403).build();
        }

        reviewService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/all/{bookId}")
    public ResponseEntity<List<ReviewCheckDTO>> getReviewsByBookId(@PathVariable Integer bookId) {
        Book book = bookService.findById(bookId).orElse(null);

        if (book == null) {
            return ResponseEntity.badRequest().body(null);
        }

        List<Review> reviews = reviewService.findByBook(book);

        // Przekonwertuj listę recenzji na DTO
        List<ReviewCheckDTO> reviewDTOs = reviews.stream()
                .map(ReviewCheckDTO::new) // Konwersja na DTO
                .toList();

        return ResponseEntity.ok(reviewDTOs);
    }
}
