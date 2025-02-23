package com.book.bookshop.service;

import com.book.bookshop.enums.UserRole;
import com.book.bookshop.models.Customer;
import com.book.bookshop.repo.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private PasswordService passwordService;
    public Customer registerCustomer(Customer customer) {
        String hashedPassword = passwordService.hashPassword(customer.getPassword());
        customer.setPassword(hashedPassword);
        customer.setRole(UserRole.ROLE_USER);
        return customerRepository.save(customer);
    }
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Optional<Customer> findById(Integer id) {
        return customerRepository.findById(id);
    }

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    public void deleteById(Integer id) {
        customerRepository.deleteById(id);
    }

    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }
    @Transactional
    public Customer loadFullProfile(String email) {
        return customerRepository.findByEmailWithOrdersAndReviews(email);
    }
}