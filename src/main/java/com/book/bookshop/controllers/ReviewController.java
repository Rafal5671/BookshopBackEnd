package com.book.bookshop.controllers;

import com.book.bookshop.models.Review;
import com.book.bookshop.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public Review createReview(@RequestBody Review review) {
        return reviewService.save(review);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Integer id, @RequestBody Review review) {
        if (reviewService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        review.setReviewId(id);
        return ResponseEntity.ok(reviewService.save(review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Integer id) {
        if (reviewService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        reviewService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
