package com.book.bookshop.controllers;

import com.book.bookshop.dto.review.ReviewDTO;
import com.book.bookshop.models.Review;
import com.book.bookshop.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:3000")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        List<ReviewDTO> result = reviewService.findAllDTO();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Integer id) {
        return reviewService.findDTOById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{bookId}")
    public ResponseEntity<?> createReview(
            @PathVariable Integer bookId,
            @RequestBody Map<String, Object> reviewData,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            Review savedReview = reviewService.createReview(bookId, reviewData, userDetails.getUsername());
            return ResponseEntity.ok(Map.of("reviewId", savedReview.getReviewId()));
        } catch (IllegalArgumentException e) {
            // np. jeśli brak ratingu lub commentPl, lub nie ma takiej książki
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{bookId}/me")
    public ResponseEntity<ReviewDTO> getUserReview(
            @PathVariable Integer bookId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return reviewService.getUserReviewDTO(bookId, userDetails.getUsername())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{bookId}/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Integer bookId,
            @PathVariable Integer reviewId,
            @RequestBody Map<String, Object> reviewData,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            Review updatedReview = reviewService.updateReview(bookId, reviewId, reviewData, userDetails.getUsername());
            return ResponseEntity.ok(new ReviewDTO(updatedReview));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/book-reviews/{bookId}")
    public ResponseEntity<List<ReviewDTO>> getBookReviews(@PathVariable Integer bookId) {
        try {
            List<ReviewDTO> reviewDTOs = reviewService.getReviewsDTOByBookId(bookId);
            return ResponseEntity.ok(reviewDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            reviewService.deleteReviewById(id, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{bookId}/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Integer bookId,
            @PathVariable Integer reviewId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            reviewService.deleteReviewForBook(bookId, reviewId, userDetails.getUsername());
            return ResponseEntity.noContent().build(); // 204
        } catch (SecurityException e) {
            // Gdy recenzja nie należy do zalogowanego użytkownika
            return ResponseEntity.status(403).build(); // 403 Forbidden
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // 400 Bad Request
        }
    }


    @GetMapping("/all/{bookId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByBookId(@PathVariable Integer bookId) {
        try {
            List<ReviewDTO> reviewDTOs = reviewService.getReviewsDTOByBookId(bookId);
            return ResponseEntity.ok(reviewDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
