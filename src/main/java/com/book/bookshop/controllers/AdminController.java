package com.book.bookshop.controllers;

import com.book.bookshop.dto.*;
import com.book.bookshop.dto.admin.DashboardStatsDTO;
import com.book.bookshop.dto.admin.authors.AuthorDTO;
import com.book.bookshop.dto.admin.category.CategoryAdminDTO;
import com.book.bookshop.dto.admin.customer.CustomerAdminDTO;
import com.book.bookshop.dto.admin.product.BookAdminDTO;
import com.book.bookshop.dto.admin.refreshToken.RefreshTokenAdminDTO;
import com.book.bookshop.dto.admin.requests.create.CreateAuthorRequest;
import com.book.bookshop.dto.admin.requests.create.CreateBookRequestDTO;
import com.book.bookshop.dto.admin.requests.create.CreateCategoryRequest;
import com.book.bookshop.dto.admin.requests.create.CreatePublisherRequest;
import com.book.bookshop.dto.admin.requests.update.UpdateBookRequest;
import com.book.bookshop.dto.admin.requests.update.UpdateCategoryRequest;
import com.book.bookshop.dto.admin.requests.update.UpdateOrderStatusRequest;
import com.book.bookshop.dto.response.PagedResponse;
import com.book.bookshop.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ------------------------
    // PRODUCTS (BOOKS)
    // ------------------------
    @GetMapping("/products")
    public PagedResponse<BookAdminDTO> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer genreId
    ) {
        return adminService.getBooks(page, size, title, categoryId, genreId);
    }

    @PostMapping("/products")
    public ResponseEntity<?> addProduct(@RequestBody CreateBookRequestDTO request) {
        Object result = adminService.addProduct(request);

        if (result instanceof java.util.Map && ((java.util.Map) result).containsKey("message")) {
            // Błąd
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
        // Sukces
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Integer id, @RequestBody UpdateBookRequest request) {
        Object result = adminService.updateProduct(id, request);

        // Możesz doprecyzować logikę (np. jeśli jest klucz "status" == NOT_FOUND)
        if (result instanceof java.util.Map map && map.containsKey("status")) {
            HttpStatus status = (HttpStatus) map.get("status");
            return new ResponseEntity<>(map, status);
        }
        if (result instanceof java.util.Map && ((java.util.Map) result).containsKey("message")) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        Object result = adminService.deleteProduct(id);

        if (result instanceof java.util.Map map && map.containsKey("status")) {
            HttpStatus status = (HttpStatus) map.get("status");
            return new ResponseEntity<>(map, status);
        }
        return ResponseEntity.ok(result);
    }

    // ------------------------
    // AUTHORS
    // ------------------------
    @GetMapping("/authors")
    public PagedResponse<AuthorDTO> getAuthors(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return adminService.getAuthors(query, page, size);
    }

    @GetMapping("/authors/search")
    public PagedResponse<AuthorDTO> searchAuthors(
            @RequestParam String query,
            @RequestParam int page,
            @RequestParam int size
    ) {
        return adminService.searchAuthors(query, page, size);
    }

    @PostMapping("/authors")
    public ResponseEntity<AuthorDTO> addAuthor(@RequestBody CreateAuthorRequest request) {
        try {
            AuthorDTO authorDTO = adminService.addAuthor(request);
            return new ResponseEntity<>(authorDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/authors/{id}")
    public ResponseEntity<?> deleteAuthor(@PathVariable Integer id) {
        Object result = adminService.deleteAuthor(id);

        if (result instanceof Map map && map.containsKey("status")) {
            HttpStatus status = (HttpStatus) map.get("status");
            return new ResponseEntity<>(map, status);
        }
        return ResponseEntity.ok(result);
    }

    // ------------------------
    // PUBLISHERS
    // ------------------------
    @PostMapping("/publishers")
    public ResponseEntity<?> addPublisher(@RequestBody CreatePublisherRequest request) {
        try {
            PublishersDTO publisherDTO = adminService.addPublisher(request);
            return new ResponseEntity<>(publisherDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/publishers")
    public PagedResponse<PublishersDTO> getPublishers(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminService.getPublishers(query, page, size);
    }

    @DeleteMapping("/publishers/{id}")
    public ResponseEntity<?> deletePublisher(@PathVariable Integer id) {
        Object result = adminService.deletePublisher(id);
        if (result instanceof java.util.Map map && map.containsKey("status")) {
            HttpStatus status = (HttpStatus) map.get("status");
            return new ResponseEntity<>(map, status);
        }
        return ResponseEntity.ok(result);
    }

    // ------------------------
    // STATISTICS
    // ------------------------
    @GetMapping("/statistics")
    public ResponseEntity<DashboardStatsDTO> getStatistics() {
        DashboardStatsDTO stats = adminService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    // ------------------------
    // ORDERS
    // ------------------------
    @GetMapping("/orders")
    public ResponseEntity<PagedResponse<GetOrderDTO>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String filterStatus
    ) {
        PagedResponse<GetOrderDTO> response =
                adminService.getOrders(page, size, sortBy, sortDir, searchTerm, filterStatus);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Integer id,
            @RequestBody UpdateOrderStatusRequest request
    ) {
        Object result = adminService.updateOrderStatus(id, request);

        // Gdy serwis zwraca string w stylu "Nie znaleziono...", może to być 404
        if (result instanceof String str && str.startsWith("Nie znaleziono")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        // Inne błędy
        if (result instanceof String str && str.startsWith("Wystąpił błąd")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
        return ResponseEntity.ok(result);
    }

    // ------------------------
    // USERS
    // ------------------------
    @GetMapping("/users")
    public PagedResponse<CustomerAdminDTO> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return adminService.getUsers(page, size);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        Object result = adminService.deleteUser(id);

        if (result instanceof String str && str.startsWith("Nie znaleziono")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        if (result instanceof String str && str.startsWith("Nie można usunąć")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    // ------------------------
    // CATEGORIES
    // ------------------------
    @PostMapping("/categories")
    public ResponseEntity<?> addCategory(@RequestBody CreateCategoryRequest request) {
        Object result = adminService.addCategory(request);

        if (result instanceof Map<?, ?> map && map.containsKey("message")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Integer id, @RequestBody UpdateCategoryRequest request) {
        Object result = adminService.updateCategory(id, request);

        if (result instanceof Map<?, ?> map && map.containsKey("status")) {
            HttpStatus status = (HttpStatus) map.get("status");
            return new ResponseEntity<>(map, status);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        Object result = adminService.deleteCategory(id);

        if (result instanceof Map<?, ?> map && map.containsKey("status")) {
            HttpStatus status = (HttpStatus) map.get("status");
            return new ResponseEntity<>(map, status);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/categories")
    public PagedResponse<CategoryAdminDTO> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminService.getCategories(page, size);
    }

    @GetMapping("/categories/all")
    public ResponseEntity<?> getAllCategories() {
        return ResponseEntity.ok(adminService.getAllCategories());
    }

    // ------------------------
    // GENRES
    // ------------------------
    @GetMapping("/genres/all")
    public ResponseEntity<?> getAllGenres() {
        return ResponseEntity.ok(adminService.getAllGenres());
    }

    // ------------------------
    // REFRESH TOKENS
    // ------------------------
    @GetMapping("/refresh-tokens")
    public PagedResponse<RefreshTokenAdminDTO> getRefreshTokensPaged(
            @RequestParam(required = false, defaultValue = "") String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        System.out.println("Email param: " + email);
        if (email != null && !email.trim().isEmpty()) {
            return adminService.getRefreshTokensByEmailPaged(email, page, size);
        } else {
            return adminService.getAllRefreshTokensPaged(page, size);
        }
    }


    @PutMapping("/refresh-tokens/revoke/{id}")
    public ResponseEntity<?> revokeToken(@PathVariable Long id) {
        Object result = adminService.revokeToken(id);

        if (result instanceof Map<?, ?> map && map.containsKey("status")) {
            HttpStatus status = (HttpStatus) map.get("status");
            return new ResponseEntity<>(map, status);
        }
        return ResponseEntity.ok(result);
    }
}
