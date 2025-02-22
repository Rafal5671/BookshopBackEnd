package com.book.bookshop.service;

import com.book.bookshop.dto.review.ReviewDTO;
import com.book.bookshop.models.Book;
import com.book.bookshop.models.Customer;
import com.book.bookshop.models.Review;
import com.book.bookshop.repo.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookService bookService;

    @Autowired
    private CustomerService customerService;

    // --------------------------------------------------
    //        PODSTAWOWE METODY (CRUD)
    // --------------------------------------------------

    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    public Optional<Review> findById(Integer id) {
        return reviewRepository.findById(id);
    }

    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    public void deleteById(Integer id) {
        reviewRepository.deleteById(id);
    }

    // --------------------------------------------------
    //        METODY Z LOGIKĄ BIZNESOWĄ / DTO
    // --------------------------------------------------

    public List<ReviewDTO> findAllDTO() {
        return findAll().stream()
                .map(ReviewDTO::new)
                .toList();
    }

    public Optional<ReviewDTO> findDTOById(Integer id) {
        return findById(id).map(ReviewDTO::new);
    }

    public Review createReview(Integer bookId, Map<String, Object> reviewData, String userEmail) {
        // 1. Pobierz książkę
        Book book = bookService.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found."));

        // 2. Pobierz klienta
        Customer customer = customerService.findByEmail(userEmail);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found for email: " + userEmail);
        }

        // 3. Wyciągnij dane z requestu
        String commentPl = (String) reviewData.get("commentPl");
        Integer rating = (Integer) reviewData.get("rating");

        if (commentPl == null) {
            throw new IllegalArgumentException("Comment is required.");
        }
        if (rating == null) {
            throw new IllegalArgumentException("Rating is required.");
        }

        // 4. Zbuduj obiekt Review
        Review review = new Review();
        review.setBook(book);
        review.setCustomer(customer);
        review.setRating(rating);
        review.setComment(commentPl);
        review.setReviewDate(LocalDateTime.now());
        review.setCreatedAt(LocalDateTime.now());

        // 5. Zapisz i zwróć
        return save(review);
    }

    public Optional<ReviewDTO> getUserReviewDTO(Integer bookId, String userEmail) {
        // 1. Pobierz książkę
        Book book = bookService.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found."));

        // 2. Pobierz klienta
        Customer customer = customerService.findByEmail(userEmail);
        if (customer == null) {
            return Optional.empty();
        }

        // 3. Wyszukaj recenzję tej książki należącą do usera
        Review review = reviewRepository.findByBookAndCustomer(book, customer);
        if (review == null) {
            return Optional.empty();
        }
        return Optional.of(new ReviewDTO(review));
    }

    public Review updateReview(Integer bookId, Integer reviewId, Map<String, Object> reviewData, String userEmail) {
        // 1. Pobierz istniejącą recenzję
        Review existingReview = findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found."));

        // 2. Sprawdź właściciela recenzji
        Customer customer = customerService.findByEmail(userEmail);
        if (!existingReview.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            throw new SecurityException("You are not authorized to update this review.");
        }

        // 3. Sprawdź, czy recenzja należy do książki o podanym ID
        if (!existingReview.getBook().getBookId().equals(bookId)) {
            throw new IllegalArgumentException("Review does not belong to the specified book.");
        }

        // 4. Zaktualizuj dane
        String commentPl = (String) reviewData.get("commentPl");
        Integer rating = (Integer) reviewData.get("rating");

        if (commentPl != null) {
            existingReview.setComment(commentPl);
        }
        if (rating != null) {
            existingReview.setRating(rating);
        }

        // 5. Zapisz zmiany
        return save(existingReview);
    }

    public List<ReviewDTO> getReviewsDTOByBookId(Integer bookId) {
        // 1. Pobierz książkę
        Book book = bookService.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found."));

        // 2. Pobierz listę recenzji i zamień na DTO
        return reviewRepository.findByBook(book).stream()
                .map(ReviewDTO::new)
                .toList();
    }

    public void deleteReviewById(Integer reviewId, String userEmail) {
        // 1. Pobierz istniejącą recenzję
        Review existingReview = findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found."));

        // 2. Sprawdź właściciela recenzji
        Customer customer = customerService.findByEmail(userEmail);
        if (!existingReview.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            throw new SecurityException("You are not authorized to delete this review.");
        }

        // 3. Usuń recenzję
        deleteById(reviewId);
    }

    public void deleteReviewForBook(Integer bookId, Integer reviewId, String userEmail) {
        // 1. Znajdź istniejącą recenzję
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found."));

        // 2. Sprawdź, czy pasuje do danego bookId
        if (!existingReview.getBook().getBookId().equals(bookId)) {
            System.out.println(bookId);
            System.out.println(existingReview.getBook().getBookId());
            throw new IllegalArgumentException("Review does not belong to the specified book.");
        }

        // 3. Sprawdź, czy recenzja jest własnością zalogowanego użytkownika
        Customer customer = customerService.findByEmail(userEmail);
        if (!existingReview.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            throw new SecurityException("You are not authorized to delete this review.");
        }

        // 4. Usuń recenzję
        reviewRepository.deleteById(reviewId);
    }
}
