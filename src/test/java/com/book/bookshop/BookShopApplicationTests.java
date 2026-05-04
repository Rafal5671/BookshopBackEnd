package com.book.bookshop;

import com.book.bookshop.enums.CoverType;
import com.book.bookshop.models.Book;
import com.book.bookshop.models.Customer;
import com.book.bookshop.models.Review;
import com.book.bookshop.repo.BookRepository;
import com.book.bookshop.repo.CustomerRepository;
import jakarta.transaction.Transactional;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@Transactional
@Commit
class BookShopApplicationTests {

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CustomerRepository customerRepository;
    private final Faker faker = new Faker();

    @Test
    public void addAtLeastTwoReviewsToEachBook() {

    }

}
