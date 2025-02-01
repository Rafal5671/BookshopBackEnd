package com.book.bookshop.controllers;

import com.book.bookshop.dto.*;
import com.book.bookshop.enums.CoverType;
import com.book.bookshop.enums.LanguageBook;
import com.book.bookshop.models.*;
import com.book.bookshop.repo.*;
import com.book.bookshop.service.AuthorService;
import com.book.bookshop.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

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
    // Konwersja encji Order na DTO
    private GetOrderDTO convertToOrderDTO(Order order) {
        String formattedAddress = formatAddress(order.getAddress());
        return new GetOrderDTO(
                order.getOrderId(),
                order.getOrderType(),
                order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName(),
                formattedAddress,
                order.getOrderDate(),
                order.getStatus(),
                order.getAmount(),
                order.getItems().size() // Ilość pozycji w zamówieniu
        );
    }

    // Formatowanie adresu w postaci jednego ciągu znaków
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

        List<AuthorCreateDTO> authorsDTO = book.getAuthors().stream()
                .map(author -> new AuthorCreateDTO(author.getAuthorId(), author.getFirstName(), author.getLastName()))
                .collect(Collectors.toList());

        List<ReviewProductDTO> reviewsDTO = book.getReviews().stream()
                .map(review -> new ReviewProductDTO(
                        review.getReviewId(),
                        review.getCustomer().getFirstName(),
                        review.getRating(),
                        review.getCommentPl()
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
                reviewsDTO
        );
    }

    @GetMapping("/products")
    public PagedResponse<BookAdminDTO> getBooks(@RequestParam int page, @RequestParam int size) {
        Page<Book> pageResult = bookRepository.findAll(PageRequest.of(page, size, Sort.by("bookId").ascending()));

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
    public PagedResponse<AuthorCreateDTO> getAuthorsPage(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("authorId").ascending());
        Page<Author> pageResult = authorService.findAll(pageable);

        // Mapowanie encji Author na AuthorDTO
        List<AuthorCreateDTO> authors = pageResult.getContent().stream()
                .map(author -> new AuthorCreateDTO(author.getAuthorId(), author.getFirstName(), author.getLastName()))
                .toList();

        // Zwróć dane w opakowaniu PagedResponse
        return new PagedResponse<>(
                authors,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }
    @GetMapping("/authors/search")
    public PagedResponse<AuthorCreateDTO> searchAuthors(
            @RequestParam String query,
            @RequestParam int page,
            @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("authorId").ascending());
        Page<Author> pageResult = authorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                query, query, pageable);

        List<AuthorCreateDTO> dtoList = pageResult.getContent().stream()
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
    private AuthorCreateDTO convertToAuthorDTO(Author author) {
        return new AuthorCreateDTO(
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
    public ResponseEntity<AuthorCreateDTO> addAuthor(@RequestBody CreateAuthorRequest request) {
        try {
            // 1. Utwórz nową encję Author
            Author author = new Author();
            author.setFirstName(request.getFirstName());
            author.setLastName(request.getLastName());
            author.setCreatedAt(LocalDateTime.now());

            // 2. Zapisz autora w bazie
            Author savedAuthor = authorRepository.save(author);

            // 3. Przygotuj AuthorDTO do zwrócenia
            AuthorCreateDTO authorDTO = new AuthorCreateDTO(
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

    @GetMapping("/publishers")
    public PagedResponse<PublishersDTO> getPublishers(@RequestParam int page, @RequestParam int size) {
        // Tworzymy pageable z sortowaniem po polu "publisherId" rosnąco
        Pageable pageable = PageRequest.of(page, size, Sort.by("publisherId").ascending());
        Page<Publisher> pageResult = publisherRepository.findAll(pageable);

        // Konwertujemy Publisher -> PublisherDTO
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
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        // Tworzymy Pageable z sortowaniem
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Pobieranie zamówień z bazy
        Page<Order> orderPage = orderRepository.findAll(pageable);

        // Mapowanie Order -> OrderDTO
        List<GetOrderDTO> orderDTOs = orderPage.getContent().stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());

        // Przygotowanie odpowiedzi w opakowaniu PagedResponse
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
            CategoryDTO categoryDTO = convertToCategoryDTO(savedCategory);

            return new ResponseEntity<>(categoryDTO, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Wystąpił błąd podczas dodawania kategorii.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
            CategoryDTO categoryDTO = convertToCategoryDTO(updatedCategory);

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
    public PagedResponse<CategoryDTO> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("categoryId").ascending());
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        List<CategoryDTO> categoryDTOs = categoryPage.getContent().stream()
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

            CategoryDTO categoryDTO = convertToCategoryDTO(categoryOpt.get());
            return new ResponseEntity<>(categoryDTO, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Wystąpił błąd podczas pobierania kategorii.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Metoda pomocnicza do konwersji encji Category -> CategoryDTO
    private CategoryDTO convertToCategoryDTO(Category category) {
        return new CategoryDTO(
                category.getCategoryId(),
                category.getNameEn(),
                category.getNamePl(),
                category.getCreatedAt()
        );
    }

}
