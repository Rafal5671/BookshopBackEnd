package com.book.bookshop.controllers;

import com.book.bookshop.dto.*;
import com.book.bookshop.dto.admin.DashboardStatsDTO;
import com.book.bookshop.dto.admin.authors.AuthorDTO;
import com.book.bookshop.dto.admin.category.CategoryAdminDTO;
import com.book.bookshop.dto.admin.customer.CustomerAdminDTO;
import com.book.bookshop.dto.admin.product.BookAdminDTO;
import com.book.bookshop.dto.admin.publisher.PublisherAdminDTO;
import com.book.bookshop.dto.admin.refreshToken.RefreshTokenAdminDTO;
import com.book.bookshop.dto.admin.requests.create.CreateAuthorRequest;
import com.book.bookshop.dto.admin.requests.create.CreateBookRequest;
import com.book.bookshop.dto.admin.requests.create.CreateCategoryRequest;
import com.book.bookshop.dto.admin.requests.create.CreatePublisherRequest;
import com.book.bookshop.dto.admin.requests.update.UpdateBookRequest;
import com.book.bookshop.dto.admin.requests.update.UpdateCategoryRequest;
import com.book.bookshop.dto.admin.requests.update.UpdateOrderStatusRequest;
import com.book.bookshop.dto.order.OrderItemAdminDTO;
import com.book.bookshop.dto.response.PagedResponse;
import com.book.bookshop.dto.review.ReviewProductDTO;
import com.book.bookshop.enums.CoverType;
import com.book.bookshop.enums.LanguageBook;
import com.book.bookshop.models.*;
import com.book.bookshop.repo.*;
import com.book.bookshop.service.AuthorService;
import com.book.bookshop.specifications.AuthorSpecification;
import com.book.bookshop.specifications.BookSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AdminController {

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private PublisherRepository publisherRepository;
    @Autowired
    private AuthorService authorService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CustomerRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private GenreRepository genreRepository;
    // Konwersja encji Order na DTO
    private GetOrderDTO convertToOrderDTO(Order order) {
        String formattedAddress = formatAddress(order.getAddress());

        // Mapujemy listę pozycji zamówienia do OrderItemDTO
        List<OrderItemAdminDTO> orderItems = order.getItems().stream().map(item -> {
            // Załóżmy, że cena jednostkowa to book.getPrice()
            BigDecimal unitPrice = item.getBook().getPrice();
            BigDecimal lineTotal = unitPrice.multiply(new BigDecimal(item.getQuantity()));
            return new OrderItemAdminDTO(
                    item.getBook().getBookId(),
                    item.getBook().getTitlePl(),  // lub titleEn, zależnie od potrzeb
                    item.getQuantity(),
                    lineTotal
            );
        }).collect(Collectors.toList());
        String customerName;
        if (order.getCustomer() != null) {
            customerName = order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName();
        } else {
            customerName = "Guest User"; // lub inny komunikat domyślny
        }

        return new GetOrderDTO(
                order.getOrderId(),
                order.getOrderType(),
                customerName,
                formattedAddress,
                order.getOrderDate(),
                order.getStatus(),
                order.getAmount(),
                orderItems.size(),
                orderItems
        );
    }

    private String formatAddress(Address address) {
        return String.format(
                "%s, %s, %s, %s",
                address.getStreet(),
                address.getCity(),
                address.getPostalCode(),
                address.getCountry()
        );
    }

    private BookAdminDTO convertToDTO(Book book) {
        List<String> genres = book.getGenres().stream()
                .map(genre -> genre.getName())
                .collect(Collectors.toList());

        PublisherAdminDTO publisherDTO = null;
        if (book.getPublisher() != null) {
            publisherDTO = new PublisherAdminDTO(book.getPublisher().getPublisherId(), book.getPublisher().getName());
        }

        List<AuthorDTO> authorsDTO = book.getAuthors().stream()
                .map(author -> new AuthorDTO(author.getAuthorId(), author.getFirstName(), author.getLastName()))
                .collect(Collectors.toList());
        String categoryName = book.getCategory() != null ? book.getCategory().getNamePl() : "Brak kategorii";
        List<ReviewProductDTO> reviewsDTO = book.getReviews().stream()
                .map(review -> new ReviewProductDTO(
                        review.getReviewId(),
                        review.getCustomer().getFirstName(),
                        review.getRating(),
                        review.getComment()
                ))
                .collect(Collectors.toList());

        return new BookAdminDTO(
                book.getBookId(),
                book.getTitlePl(),
                book.getTitleEn(),
                book.getOriginalTitle(),
                book.getPrice(),
                book.getDiscountPrice(),
                book.getDescriptionPl(),
                book.getDescriptionEn(),
                book.getStockQuantity(),
                book.getImageUrl(),
                book.getPagesCount(),
                book.getCoverType(),
                book.getLanguage(),
                genres,
                book.getRelease_date() != null
                        ? book.getRelease_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : null,
                publisherDTO,
                authorsDTO,
                reviewsDTO,
                categoryName
        );
    }

    @GetMapping("/products")
    public PagedResponse<BookAdminDTO> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer genreId) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("bookId").ascending());

        // Budujemy Specification – zaczynamy od "pustej" specyfikacji (conjunction)
        Specification<Book> spec = Specification.where(BookSpecification.titleContains(title))
                .and(BookSpecification.hasCategory(categoryId))
                .and(BookSpecification.hasGenre(genreId));

        Page<Book> pageResult = bookRepository.findAll(spec, pageable);

        List<BookAdminDTO> dtoList = pageResult.getContent().stream()
                .map(this::convertToDTO)
                .toList();

        return new PagedResponse<>(
                dtoList,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }


    @GetMapping("/authors")
    public PagedResponse<AuthorDTO> getAuthors(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("authorId").ascending());

        Specification<Author> spec = AuthorSpecification.nameContains(query);

        Page<Author> pageResult = authorRepository.findAll(spec, pageable);

        List<AuthorDTO> dtoList = pageResult.getContent().stream()
                .map(this::convertToAuthorDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                dtoList,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }
    @GetMapping("/authors/search")
    public PagedResponse<AuthorDTO> searchAuthors(
            @RequestParam String query,
            @RequestParam int page,
            @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("authorId").ascending());
        Page<Author> pageResult = authorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                query, query, pageable);

        List<AuthorDTO> dtoList = pageResult.getContent().stream()
                .map(this::convertToAuthorDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                dtoList,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }

    // Metoda pomocnicza do konwersji encji Author -> AuthorDTO
    private AuthorDTO convertToAuthorDTO(Author author) {
        return new AuthorDTO(
                author.getAuthorId(),
                author.getFirstName(),
                author.getLastName()
        );
    }

    @PostMapping("/products")
    public ResponseEntity<?> addProduct(@RequestBody CreateBookRequest request) {
        try {
            Book book = new Book();

            book.setTitlePl(request.getTitlePL());
            book.setTitleEn(request.getTitleEN());
            book.setDescriptionPl(request.getDescriptionPL());
            book.setDescriptionEn(request.getDescriptionEN());
            book.setOriginalTitle(request.getOriginalTitle());
            book.setPrice(request.getPrice());
            book.setDiscountPrice(request.getSalePrice());
            book.setStockQuantity(10);
            book.setCoverType(CoverType.HARD);
            book.setLanguage(LanguageBook.POLISH);

            // Ustawienie wydawcy
            if (request.getPublisherId() != null) {
                Optional<Publisher> publisherOpt = publisherRepository.findById(request.getPublisherId());
                if (publisherOpt.isPresent()) {
                    book.setPublisher(publisherOpt.get());
                } else {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("message", "Nie znaleziono wydawcy o podanym ID.");
                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Wydawca jest wymagany.");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            // Ustawienie autorów
            if (request.getAuthorsIds() != null && !request.getAuthorsIds().isEmpty()) {
                List<Author> authors = authorRepository.findAllById(request.getAuthorsIds());
                if (authors.size() != request.getAuthorsIds().size()) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("message", "Jeden lub więcej autorów nie zostało znalezionych.");
                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
                book.setAuthors(authors);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Przynajmniej jeden autor jest wymagany.");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            // Parsowanie daty wydania
            if (request.getReleaseDate() != null && !request.getReleaseDate().isEmpty()) {
                LocalDate date = LocalDate.parse(request.getReleaseDate());
                book.setRelease_date(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            if (request.getImageUrl() != null) {
                System.out.println(request.getImageUrl());
                book.setImageUrl(request.getImageUrl());
            }

            Book savedBook = bookRepository.save(book);
            return new ResponseEntity<>(convertToDTO(savedBook), HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Wystąpił błąd podczas dodawania produktu.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/authors")
    public ResponseEntity<AuthorDTO> addAuthor(@RequestBody CreateAuthorRequest request) {
        try {
            // 1. Utwórz nową encję Author
            Author author = new Author();
            author.setFirstName(request.getFirstName());
            author.setLastName(request.getLastName());
            author.setCreatedAt(LocalDateTime.now());

            // 2. Zapisz autora w bazie
            Author savedAuthor = authorRepository.save(author);

            // 3. Przygotuj AuthorDTO do zwrócenia
            AuthorDTO authorDTO = new AuthorDTO(
                    savedAuthor.getAuthorId(),
                    savedAuthor.getFirstName(),
                    savedAuthor.getLastName()
            );

            return new ResponseEntity<>(authorDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Integer id, @RequestBody UpdateBookRequest request) {
        try {
            Optional<Book> bookOpt = bookRepository.findById(id);
            if (!bookOpt.isPresent()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Nie znaleziono produktu o podanym ID.");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }

            Book book = bookOpt.get();

            // Aktualizacja pól, jeśli są dostarczone
            if (request.getTitlePL() != null && !request.getTitlePL().isEmpty()) {
                book.setTitlePl(request.getTitlePL());
            }

            if (request.getTitleEN() != null && !request.getTitleEN().isEmpty()) {
                book.setTitleEn(request.getTitleEN());
            }

            if (request.getDescriptionPL() != null && !request.getDescriptionPL().isEmpty()) {
                book.setDescriptionPl(request.getDescriptionPL());
            }

            if (request.getDescriptionEN() != null && !request.getDescriptionEN().isEmpty()) {
                book.setDescriptionEn(request.getDescriptionEN());
            }

            if (request.getOriginalTitle() != null && !request.getOriginalTitle().isEmpty()) {
                book.setOriginalTitle(request.getOriginalTitle());
            }

            if (request.getPrice() != null) {
                book.setPrice(request.getPrice());
            }

            if (request.getSalePrice() != null) {
                book.setDiscountPrice(request.getSalePrice());
            }

            if (request.getPublisherId() != null) {
                Optional<Publisher> publisherOpt = publisherRepository.findById(request.getPublisherId());
                if (publisherOpt.isPresent()) {
                    book.setPublisher(publisherOpt.get());
                } else {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("message", "Nie znaleziono wydawcy o podanym ID.");
                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            }

            if (request.getAuthorsIds() != null && !request.getAuthorsIds().isEmpty()) {
                List<Author> authors = authorRepository.findAllById(request.getAuthorsIds());
                if (authors.size() != request.getAuthorsIds().size()) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("message", "Jeden lub więcej autorów nie zostało znalezionych.");
                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
                book.setAuthors(authors);
            }

            if (request.getReleaseDate() != null && !request.getReleaseDate().isEmpty()) {
                LocalDate date = LocalDate.parse(request.getReleaseDate());
                book.setRelease_date(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }

            if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
                book.setImageUrl(request.getImageUrl());
            }

            Book updatedBook = bookRepository.save(book);
            return new ResponseEntity<>(convertToDTO(updatedBook), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Wystąpił błąd podczas aktualizacji produktu.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/authors/{id}")
    public ResponseEntity<?> deleteAuthor(@PathVariable Integer id) {
        try {
            Optional<Author> authorOpt = authorRepository.findById(id);

            if (authorOpt.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Nie znaleziono autora o podanym ID.");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }

            Author author = authorOpt.get();

            // Sprawdź, czy autor jest powiązany z książkami
            if (!author.getBooks().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Nie można usunąć autora, ponieważ jest powiązany z książkami.");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            authorRepository.delete(author);

            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Autor został pomyślnie usunięty.");
            return new ResponseEntity<>(successResponse, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Wystąpił błąd podczas usuwania autora.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/publishers")
    public ResponseEntity<PublishersDTO> addPublisher(@RequestBody CreatePublisherRequest request) {
        try {
            // 1. Utwórz nową encję Publisher
            Publisher publisher = new Publisher();
            publisher.setName(request.getName());

            // 2. Zapisz wydawcę w bazie
            Publisher savedPublisher = publisherRepository.save(publisher);

            // 3. Przygotuj PublishersDTO do zwrócenia
            PublishersDTO publisherDTO = convertToPublisherDTO(savedPublisher);

            return new ResponseEntity<>(publisherDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/statistics")
    public ResponseEntity<DashboardStatsDTO> getStatistics() {
        long ordersCount = orderRepository.count();
        long usersCount = userRepository.count();
        long productsCount = bookRepository.count();

        DashboardStatsDTO stats = new DashboardStatsDTO(ordersCount, usersCount, productsCount);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/publishers")
    public PagedResponse<PublishersDTO> getPublishers(
            @RequestParam(required = false) String query, // opcjonalny parametr wyszukiwania
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("publisherId").ascending());
        Page<Publisher> pageResult;

        if (query == null || query.isBlank()) {
            // Jeśli parametr query jest pusty, pobieramy wszystkich wydawców
            pageResult = publisherRepository.findAll(pageable);
        } else {
            // Jeśli parametr query jest podany, wyszukujemy wydawców po nazwie (ignorując wielkość liter)
            pageResult = publisherRepository.findByNameContainingIgnoreCase(query, pageable);
        }

        // Konwersja encji Publisher -> PublisherDTO
        List<PublishersDTO> dtoList = pageResult.getContent().stream()
                .map(this::convertToPublisherDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                dtoList,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }

    // Metoda pomocnicza do konwersji encji Publisher -> PublisherDTO
    private PublishersDTO convertToPublisherDTO(Publisher publisher) {
        return new PublishersDTO(
                publisher.getPublisherId(),
                publisher.getName()
        );
    }
    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        try {
            Optional<Book> bookOpt = bookRepository.findById(id);
            if (bookOpt.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Nie znaleziono produktu o podanym ID.");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }

            bookRepository.deleteById(id);

            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Produkt został pomyślnie usunięty.");
            return new ResponseEntity<>(successResponse, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Wystąpił błąd podczas usuwania produktu.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping("/publishers/{id}")
    public ResponseEntity<?> deletePublisher(@PathVariable Integer id) {
        try {
            Optional<Publisher> publisherOpt = publisherRepository.findById(id);

            if (publisherOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Nie znaleziono wydawcy o podanym ID."));
            }

            Publisher publisher = publisherOpt.get();

            // Sprawdź, czy wydawca jest powiązany z jakąkolwiek książką
            boolean hasBooks = bookRepository.existsByPublisher(publisher);
            if (hasBooks) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Nie można usunąć wydawcy, ponieważ jest powiązany z książkami."));
            }

            // Jeśli brak powiązań, usuń wydawcę
            publisherRepository.delete(publisher);

            return ResponseEntity.ok(Map.of("message", "Wydawca został pomyślnie usunięty."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Wystąpił błąd podczas usuwania wydawcy."));
        }
    }
    @GetMapping("/orders")
    public ResponseEntity<PagedResponse<GetOrderDTO>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String filterStatus
    ) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        String backendStatus = null;
        if (filterStatus != null && !filterStatus.isBlank() && !filterStatus.equalsIgnoreCase("Wszystkie")) {
            Map<String, String> statusMap = new HashMap<>();
            statusMap.put("Oczekujące", "PENDING");
            statusMap.put("Opłacone", "PAID");
            statusMap.put("Wysłane", "SHIPPED");
            statusMap.put("Dostarczone", "DELIVERED");
            statusMap.put("Anulowane", "CANCELED");
            statusMap.put("Zwrócone", "RETURNED");
            backendStatus = statusMap.get(filterStatus);
        }

        Page<Order> orderPage;
        if ((searchTerm == null || searchTerm.isBlank()) && backendStatus == null) {
            orderPage = orderRepository.findAll(pageable);
        } else {
            orderPage = orderRepository.findBySearchAndStatus(searchTerm, backendStatus, pageable);
        }

        List<GetOrderDTO> orderDTOs = orderPage.getContent().stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());

        PagedResponse<GetOrderDTO> response = new PagedResponse<>(
                orderDTOs,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Integer id,
            @RequestBody UpdateOrderStatusRequest request) {
        try {
            // Znajdź zamówienie w bazie
            Optional<Order> optionalOrder = orderRepository.findById(id);

            if (optionalOrder.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Nie znaleziono zamówienia o ID: " + id);
            }

            Order order = optionalOrder.get();

            // Zaktualizuj status zamówienia
            order.setStatus(request.getStatus());
            orderRepository.save(order);

            return ResponseEntity.ok("Status zamówienia został pomyślnie zaktualizowany.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Wystąpił błąd podczas aktualizacji statusu zamówienia.");
        }
    }
    @GetMapping("/users")
    public PagedResponse<CustomerAdminDTO> getUsers(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> customerPage = userRepository.findAll(pageable);

        List<CustomerAdminDTO> customerDTOs = customerPage.getContent().stream()
                .map(customer -> new CustomerAdminDTO(
                        customer.getCustomerId(),
                        customer.getFirstName(),
                        customer.getLastName(),
                        customer.getEmail(),
                        customer.getPhone(),
                        customer.getCreatedAt(),
                        customer.getRole()
                ))
                .collect(Collectors.toList());

        return new PagedResponse<>(
                customerDTOs,
                customerPage.getNumber(),
                customerPage.getSize(),
                customerPage.getTotalElements(),
                customerPage.getTotalPages()
        );
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        Optional<Customer> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Nie znaleziono użytkownika o ID " + id);
        }

        try {
            userRepository.delete(userOpt.get());
            return ResponseEntity.ok("Użytkownik został usunięty.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Nie można usunąć użytkownika, ponieważ jest powiązany z innymi danymi.");
        }
    }

    @PostMapping("/categories")
    public ResponseEntity<?> addCategory(@RequestBody CreateCategoryRequest request) {
        try {
            // Sprawdzenie, czy kategoria o podanej nazwie już istnieje
            if (categoryRepository.findByNameEn(request.getNameEn()).isPresent() ||
                    categoryRepository.findByNamePl(request.getNamePl()).isPresent()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Kategoria o podanej nazwie już istnieje.");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            // Tworzenie nowej kategorii
            Category category = new Category();
            category.setNameEn(request.getNameEn());
            category.setNamePl(request.getNamePl());
            // createdAt jest ustawiane w konstruktorze

            // Zapisanie kategorii w bazie
            Category savedCategory = categoryRepository.save(category);

            // Konwersja na DTO
            CategoryAdminDTO categoryDTO = convertToCategoryDTO(savedCategory);

            return new ResponseEntity<>(categoryDTO, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Wystąpił błąd podczas dodawania kategorii.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/refresh-tokens")
    public PagedResponse<RefreshTokenAdminDTO> getRefreshTokensByEmailPaged(
            @RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<RefreshToken> result;

        if (email.isBlank()) {
            // Jeśli email jest pusty, pobieramy wszystkie tokeny
            result = refreshTokenRepository.findAll(pageable);
        } else {
            // Jeśli email nie jest pusty, filtrujemy po emailu
            result = refreshTokenRepository.findByEmail(email, pageable);
        }

        // Ręczne mapowanie obiektów RefreshToken na RefreshTokenAdminDTO
        List<RefreshTokenAdminDTO> dtoList = result.getContent().stream()
                .map(token -> new RefreshTokenAdminDTO(
                        token.getId(),
                        token.getEmail(),
                        token.getToken(),
                        token.getExpiryDate(),
                        token.isRevoked()
                ))
                .collect(Collectors.toList());

        return new PagedResponse<>(dtoList, result.getNumber(), result.getSize(), result.getTotalElements(),result.getTotalPages());
    }
    @PutMapping("/refresh-tokens/revoke/{id}")
    public ResponseEntity<?> revokeToken(@PathVariable Long id) {
        Optional<RefreshToken> optToken = refreshTokenRepository.findById(id);

        if (optToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nie znaleziono tokenu o ID: " + id));
        }

        RefreshToken token = optToken.get();
        if (token.isRevoked()) {
            // Już jest unieważniony
            return ResponseEntity.ok(Map.of("message", "Token już jest unieważniony."));
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);

        return ResponseEntity.ok(Map.of("message", "Token o ID " + id + " został unieważniony."));
    }

    // 2. Edycja Kategorii
    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Integer id, @RequestBody UpdateCategoryRequest request) {
        try {
            Optional<Category> categoryOpt = categoryRepository.findById(id);
            if (!categoryOpt.isPresent()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Nie znaleziono kategorii o podanym ID.");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }

            Category category = categoryOpt.get();

            // Aktualizacja pól, jeśli są dostarczone
            if (request.getNameEn() != null && !request.getNameEn().isEmpty()) {
                // Sprawdzenie unikalności
                Optional<Category> existingEn = categoryRepository.findByNameEn(request.getNameEn());
                if (existingEn.isPresent() && !existingEn.get().getCategoryId().equals(id)) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("message", "Kategoria o podanej nazwie angielskiej już istnieje.");
                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
                category.setNameEn(request.getNameEn());
            }

            if (request.getNamePl() != null && !request.getNamePl().isEmpty()) {
                // Sprawdzenie unikalności
                Optional<Category> existingPl = categoryRepository.findByNamePl(request.getNamePl());
                if (existingPl.isPresent() && !existingPl.get().getCategoryId().equals(id)) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("message", "Kategoria o podanej nazwie polskiej już istnieje.");
                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
                category.setNamePl(request.getNamePl());
            }

            // Zapisanie zaktualizowanej kategorii
            Category updatedCategory = categoryRepository.save(category);

            // Konwersja na DTO
            CategoryAdminDTO categoryDTO = convertToCategoryDTO(updatedCategory);

            return new ResponseEntity<>(categoryDTO, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Wystąpił błąd podczas aktualizacji kategorii.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 3. Usuwanie Kategorii
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        try {
            Optional<Category> categoryOpt = categoryRepository.findById(id);
            if (categoryOpt.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Nie znaleziono kategorii o podanym ID.");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }

            Category category = categoryOpt.get();

            // Sprawdzenie, czy kategoria jest powiązana z jakimikolwiek książkami
            if (!category.getBooks().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Nie można usunąć kategorii, ponieważ jest powiązana z książkami.");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            // Usunięcie kategorii
            categoryRepository.delete(category);

            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Kategoria została pomyślnie usunięta.");
            return new ResponseEntity<>(successResponse, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Wystąpił błąd podczas usuwania kategorii.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 4. Pobieranie Listy Kategorii (opcjonalnie z paginacją)
    @GetMapping("/categories")
    public PagedResponse<CategoryAdminDTO> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("categoryId").ascending());
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        List<CategoryAdminDTO> categoryDTOs = categoryPage.getContent().stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                categoryDTOs,
                categoryPage.getNumber(),
                categoryPage.getSize(),
                categoryPage.getTotalElements(),
                categoryPage.getTotalPages()
        );
    }
    // Pobieranie wszystkich kategorii
    @GetMapping("/categories/all")
    public ResponseEntity<List<CategoryAdminDTO>> getAllCategories() {
        List<CategoryAdminDTO> categories = categoryRepository.findAll()
                .stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    // Pobieranie wszystkich gatunków
    @GetMapping("/genres/all")
    public ResponseEntity<List<String>> getAllGenres() {
        List<String> genres = genreRepository.findAll()
                .stream()
                .map(Genre::getName)
                .collect(Collectors.toList());
        return ResponseEntity.ok(genres);
    }

    // 5. Pobieranie Szczegółów Kategorii (opcjonalnie)
    @GetMapping("/categories/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Integer id) {
        try {
            Optional<Category> categoryOpt = categoryRepository.findById(id);
            if (categoryOpt.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Nie znaleziono kategorii o podanym ID.");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }

            CategoryAdminDTO categoryDTO = convertToCategoryDTO(categoryOpt.get());
            return new ResponseEntity<>(categoryDTO, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Wystąpił błąd podczas pobierania kategorii.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Metoda pomocnicza do konwersji encji Category -> CategoryDTO
    private CategoryAdminDTO convertToCategoryDTO(Category category) {
        return new CategoryAdminDTO(category);
    }

}
