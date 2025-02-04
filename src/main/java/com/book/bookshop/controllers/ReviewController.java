package com.book.bookshop.controllers;

import com.book.bookshop.dto.review.ReviewDTO;
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
    private BookService bookService;

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        List<Review> reviews = reviewService.findAll();
        List<ReviewDTO> result = reviews.stream()
                .map(ReviewDTO::new)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Integer id) {
        return reviewService.findById(id)
                .map(review -> ResponseEntity.ok(new ReviewDTO(review)))
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/{bookId}")
    public ResponseEntity<?> createReview(
            @PathVariable Integer bookId,
            @RequestBody Map<String, Object> reviewData,
            @AuthenticationPrincipal UserDetails userDetails)
    {
        String email = userDetails.getUsername();
        Customer customer = customerService.findByEmail(email);

        Book book = bookService.findById(bookId).orElse(null);
        if (book == null) {
            return ResponseEntity.badRequest().body("Book not found.");
        }

        String commentPl = (String) reviewData.get("commentPl");
        String commentEn = (String) reviewData.get("commentEn");
        Integer rating = (Integer) reviewData.get("rating");

        if (commentPl == null && commentEn == null) {
            return ResponseEntity.badRequest().body("Comment is required.");
        }
        if (rating == null) {
            return ResponseEntity.badRequest().body("Rating is required.");
        }

        Review review = new Review();
        review.setBook(book);
        review.setCustomer(customer);
        review.setRating(rating);
        review.setCommentPl(commentPl);
        review.setCommentEn(commentEn);
        review.setReviewDate(LocalDateTime.now());
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewService.save(review);

        return ResponseEntity.ok(Map.of("reviewId", savedReview.getReviewId()));
    }

    @GetMapping("/{bookId}/me")
    public ResponseEntity<ReviewDTO> getUserReview(
            @PathVariable Integer bookId,
            @AuthenticationPrincipal UserDetails userDetails)
    {
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

        return ResponseEntity.ok(new ReviewDTO(review));
    }

    @PutMapping("/{bookId}/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Integer bookId,
            @PathVariable Integer reviewId,
            @RequestBody Map<String, Object> reviewData,
            @AuthenticationPrincipal UserDetails userDetails)
    {
        String email = userDetails.getUsername();
        Customer customer = customerService.findByEmail(email);

        Review existingReview = reviewService.findById(reviewId).orElse(null);
        if (existingReview == null) {
            return ResponseEntity.notFound().build();
        }

        if (!existingReview.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            return ResponseEntity.status(403).body("You are not authorized to update this review.");
        }

        if (!existingReview.getBook().getBookId().equals(bookId)) {
            return ResponseEntity.badRequest().body("Review does not belong to the specified book.");
        }

        String commentPl = (String) reviewData.get("commentPl");
        String commentEn = (String) reviewData.get("commentEn");
        Integer rating = (Integer) reviewData.get("rating");

        if (commentPl != null) existingReview.setCommentPl(commentPl);
        if (commentEn != null) existingReview.setCommentEn(commentEn);
        if (rating != null) existingReview.setRating(rating);

        Review updatedReview = reviewService.save(existingReview);

        return ResponseEntity.ok(new ReviewDTO(updatedReview));
    }

    @GetMapping("/book-reviews/{bookId}")
    public ResponseEntity<List<ReviewDTO>> getBookReviews(@PathVariable Integer bookId) {
        Book book = bookService.findById(bookId).orElse(null);

        if (book == null) {
            return ResponseEntity.badRequest().body(null);
        }

        List<Review> reviews = reviewService.findByBook(book);

        List<ReviewDTO> reviewDTOs = reviews.stream()
                .map(ReviewDTO::new)
                .toList();
        return ResponseEntity.ok(reviewDTOs);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails)
    {
        String email = userDetails.getUsername();
        Customer customer = customerService.findByEmail(email);
        Review existingReview = reviewService.findById(id).orElse(null);

        if (existingReview == null) {
            return ResponseEntity.notFound().build();
        }

        if (!existingReview.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            return ResponseEntity.status(403).build();
        }

        reviewService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all/{bookId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByBookId(@PathVariable Integer bookId) {
        Book book = bookService.findById(bookId).orElse(null);

        if (book == null) {
            return ResponseEntity.badRequest().body(null);
        }

        List<Review> reviews = reviewService.findByBook(book);
        List<ReviewDTO> reviewDTOs = reviews.stream()
                .map(ReviewDTO::new)
                .toList();

        return ResponseEntity.ok(reviewDTOs);
    }
}
