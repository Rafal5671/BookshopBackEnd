package com.book.bookshop.controllers;

import com.book.bookshop.models.Customer;
import com.book.bookshop.models.Review;
import com.book.bookshop.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Integer id) {
        return reviewService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Review createReview(@RequestBody Review review, @AuthenticationPrincipal Customer customer) {
        // Set the customer from JWT
        review.setCustomer(customer);
        review.setReviewDate(LocalDateTime.now());
        review.setCreatedAt(LocalDateTime.now());
        return reviewService.save(review);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Integer id, @RequestBody Review review, @AuthenticationPrincipal Customer customer) {
        if (reviewService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // Ensure the review is associated with the authenticated user
        review.setReviewId(id);
        review.setCustomer(customer);
        return ResponseEntity.ok(reviewService.save(review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Integer id, @AuthenticationPrincipal Customer customer) {
        if (reviewService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // Optionally check that the review belongs to the current customer
        reviewService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
