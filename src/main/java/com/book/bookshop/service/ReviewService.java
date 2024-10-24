package com.book.bookshop.service;

import com.book.bookshop.models.Review;
import com.book.bookshop.repo.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

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
}

