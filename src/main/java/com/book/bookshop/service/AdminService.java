package com.book.bookshop.service;

import com.book.bookshop.dto.*;
import com.book.bookshop.dto.admin.DashboardStatsDTO;
import com.book.bookshop.dto.admin.authors.AuthorDTO;
import com.book.bookshop.dto.admin.category.CategoryAdminDTO;
import com.book.bookshop.dto.admin.customer.CustomerAdminDTO;
import com.book.bookshop.dto.admin.product.BookAdminDTO;
import com.book.bookshop.dto.admin.publisher.PublisherAdminDTO;
import com.book.bookshop.dto.admin.refreshToken.RefreshTokenAdminDTO;
import com.book.bookshop.dto.admin.requests.create.CreateAuthorRequest;
import com.book.bookshop.dto.admin.requests.create.CreateBookRequestDTO;
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
import com.book.bookshop.specifications.AuthorSpecification;
import com.book.bookshop.specifications.BookSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

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

    // -------------------------------------------------------
    // 1. PRODUCTS (BOOKS)
    // -------------------------------------------------------

    // 1a) Pobieranie książek (z uwzględnieniem filtrów i paginacji)
    public PagedResponse<BookAdminDTO> getBooks(int page, int size, String title, Integer categoryId, Integer genreId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("bookId").ascending());

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

    // 1b) Dodawanie nowego produktu (CreateBookRequestDTO)
    public Object addProduct(CreateBookRequestDTO request) {
        try {
            Book book = new Book();
            book.setTitlePl(request.getTitlePL());
            book.setTitleEn(request.getTitleEN());
            book.setDescriptionPl(request.getDescriptionPL());
            book.setDescriptionEn(request.getDescriptionEN());
            book.setOriginalTitle(request.getOriginalTitle());
            book.setPrice(request.getPrice());
            book.setDiscountPrice(request.getSalePrice());
            book.setLanguage(LanguageBook.POLISH);
            book.setStockQuantity(request.getStockQuantity());
            book.setCoverType(request.getCoverType());
            // Ustawienie wydawcy
            Optional<Publisher> publisherOpt = publisherRepository.findById(request.getPublisherId());
            if (publisherOpt.isEmpty()) {
                return Map.of("message", "Nie znaleziono wydawcy o podanym ID.");
            }
            book.setPublisher(publisherOpt.get());

            // Ustawienie autorów
            if (request.getAuthorsIds() != null && !request.getAuthorsIds().isEmpty()) {
                List<Author> authors = authorRepository.findAllById(request.getAuthorsIds());
                if (authors.size() != request.getAuthorsIds().size()) {
                    return Map.of("message", "Jeden lub więcej autorów nie zostało znalezionych.");
                }
                book.setAuthors(authors);
            } else {
                return Map.of("message", "Przynajmniej jeden autor jest wymagany.");
            }

            // Parsowanie daty wydania
            if (request.getReleaseDate() != null && !request.getReleaseDate().isEmpty()) {
                LocalDate date = LocalDate.parse(request.getReleaseDate());
                book.setRelease_date(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            if (request.getImageUrl() != null) {
                book.setImageUrl(request.getImageUrl());
            }
            Optional<Category> categoryOpt = categoryRepository.findById(request.getCategory());
            if (categoryOpt.isEmpty()) {
                return Map.of("message", "Nie znaleziono kategorii o podanym ID.");
            }
            if (request.getGenres() != null && !request.getGenres().isEmpty()) {
                List<Genre> genres = genreRepository.findAllById(request.getGenres());
                if (genres.size() != request.getGenres().size()) {
                    return Map.of("message", "Jeden lub więcej gatunków nie zostało znalezionych.");
                }
                book.setGenres(genres);
            }
            book.setCategory(categoryOpt.get());
            Book savedBook = bookRepository.save(book);
            return convertToDTO(savedBook);

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("message", "Wystąpił błąd podczas dodawania produktu.");
        }
    }

    // 1c) Aktualizacja produktu
    public Object updateProduct(Integer id, UpdateBookRequest request) {
        try {
            Optional<Book> bookOpt = bookRepository.findById(id);
            if (bookOpt.isEmpty()) {
                return Map.of("message", "Nie znaleziono produktu o podanym ID.", "status", HttpStatus.NOT_FOUND);
            }
            Book book = bookOpt.get();

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
                if (publisherOpt.isEmpty()) {
                    return Map.of("message", "Nie znaleziono wydawcy o podanym ID.");
                }
                book.setPublisher(publisherOpt.get());
            }

            if (request.getAuthorsIds() != null && !request.getAuthorsIds().isEmpty()) {
                List<Author> authors = authorRepository.findAllById(request.getAuthorsIds());
                if (authors.size() != request.getAuthorsIds().size()) {
                    return Map.of("message", "Jeden lub więcej autorów nie zostało znalezionych.");
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

            if (request.getStockQuantity() != null) {
                book.setStockQuantity(request.getStockQuantity());
            }

            if (request.getPagesCount() != null) {
                book.setPagesCount(request.getPagesCount());
            }

            if (request.getCoverType() != null) {
                book.setCoverType(request.getCoverType());
            }

            if (request.getCategory() != null) {
                Optional<Category> categoryOpt = categoryRepository.findById(request.getCategory());
                if (categoryOpt.isEmpty()) {
                    return Map.of("message", "Nie znaleziono kategorii o podanym ID.");
                }
                book.setCategory(categoryOpt.get());
            }

            if (request.getGenres() != null && !request.getGenres().isEmpty()) {
                System.out.println(request.getGenres());
                List<Genre> genres = genreRepository.findAllById(request.getGenres());
                if (genres.size() != request.getGenres().size()) {
                    return Map.of("message", "Jeden lub więcej gatunków nie zostało znalezionych.");
                }
                book.setGenres(genres);
            }

            Book updatedBook = bookRepository.save(book);
            return convertToDTO(updatedBook);

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("message", "Wystąpił błąd podczas aktualizacji produktu.");
        }
    }


    // 1d) Usunięcie produktu
    public Object deleteProduct(Integer id) {
        try {
            Optional<Book> bookOpt = bookRepository.findById(id);
            if (bookOpt.isEmpty()) {
                return Map.of("message", "Nie znaleziono produktu o podanym ID.", "status", HttpStatus.NOT_FOUND);
            }

            bookRepository.deleteById(id);
            return Map.of("message", "Produkt został pomyślnie usunięty.");
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("message", "Wystąpił błąd podczas usuwania produktu.");
        }
    }

    // Metoda pomocnicza – konwersja Book -> BookAdminDTO
    private BookAdminDTO convertToDTO(Book book) {
        List<String> genres = book.getGenres().stream()
                .map(Genre::getName)
                .collect(Collectors.toList());

        PublisherAdminDTO publisherDTO = null;
        if (book.getPublisher() != null) {
            publisherDTO = new PublisherAdminDTO(book.getPublisher().getPublisherId(), book.getPublisher().getName());
        }

        List<AuthorDTO> authorsDTO = book.getAuthors().stream()
                .map(author -> new AuthorDTO(author.getAuthorId(), author.getFirstName(), author.getLastName()))
                .collect(Collectors.toList());

        String categoryName = (book.getCategory() != null)
                ? book.getCategory().getNamePl()
                : "Brak kategorii";

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
                (book.getRelease_date() != null)
                        ? book.getRelease_date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : null,
                publisherDTO,
                authorsDTO,
                reviewsDTO,
                categoryName
        );
    }

    // -------------------------------------------------------
    // 2. AUTHORS
    // -------------------------------------------------------

    // Pobieranie autorów z filtrem i paginacją
    public PagedResponse<AuthorDTO> getAuthors(String query, int page, int size) {
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

    // Wyszukiwanie autorów
    public PagedResponse<AuthorDTO> searchAuthors(String query, int page, int size) {
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

    // Dodawanie autora
    public AuthorDTO addAuthor(CreateAuthorRequest request) {
        Author author = new Author();
        author.setFirstName(request.getFirstName());
        author.setLastName(request.getLastName());
        author.setCreatedAt(LocalDateTime.now());

        Author savedAuthor = authorRepository.save(author);
        return convertToAuthorDTO(savedAuthor);
    }

    // Usuwanie autora
    public Object deleteAuthor(Integer id) {
        try {
            Optional<Author> authorOpt = authorRepository.findById(id);
            if (authorOpt.isEmpty()) {
                return Map.of("message", "Nie znaleziono autora o podanym ID.", "status", HttpStatus.NOT_FOUND);
            }
            Author author = authorOpt.get();

            if (!author.getBooks().isEmpty()) {
                return Map.of("message", "Nie można usunąć autora, ponieważ jest powiązany z książkami.");
            }

            authorRepository.delete(author);
            return Map.of("message", "Autor został pomyślnie usunięty.");

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("message", "Wystąpił błąd podczas usuwania autora.");
        }
    }

    // Metoda pomocnicza – konwersja Author -> AuthorDTO
    private AuthorDTO convertToAuthorDTO(Author author) {
        return new AuthorDTO(
                author.getAuthorId(),
                author.getFirstName(),
                author.getLastName()
        );
    }

    // -------------------------------------------------------
    // 3. PUBLISHERS
    // -------------------------------------------------------

    // Dodawanie wydawcy
    public PublishersDTO addPublisher(CreatePublisherRequest request) {
        Publisher publisher = new Publisher();
        publisher.setName(request.getName());

        Publisher savedPublisher = publisherRepository.save(publisher);
        return convertToPublisherDTO(savedPublisher);
    }

    // Pobieranie paginowane wydawców
    public PagedResponse<PublishersDTO> getPublishers(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publisherId").ascending());
        Page<Publisher> pageResult;

        if (query == null || query.isBlank()) {
            pageResult = publisherRepository.findAll(pageable);
        } else {
            pageResult = publisherRepository.findByNameContainingIgnoreCase(query, pageable);
        }

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

    // Usuwanie wydawcy
    public Object deletePublisher(Integer id) {
        try {
            Optional<Publisher> publisherOpt = publisherRepository.findById(id);
            if (publisherOpt.isEmpty()) {
                return Map.of("message", "Nie znaleziono wydawcy o podanym ID.", "status", HttpStatus.NOT_FOUND);
            }

            Publisher publisher = publisherOpt.get();

            boolean hasBooks = bookRepository.existsByPublisher(publisher);
            if (hasBooks) {
                return Map.of("message", "Nie można usunąć wydawcy, ponieważ jest powiązany z książkami.");
            }

            publisherRepository.delete(publisher);
            return Map.of("message", "Wydawca został pomyślnie usunięty.");
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("message", "Wystąpił błąd podczas usuwania wydawcy.");
        }
    }

    // Metoda pomocnicza – Publisher -> PublishersDTO
    private PublishersDTO convertToPublisherDTO(Publisher publisher) {
        return new PublishersDTO(
                publisher.getPublisherId(),
                publisher.getName()
        );
    }

    // -------------------------------------------------------
    // 4. ORDERS
    // -------------------------------------------------------

    public PagedResponse<GetOrderDTO> getOrders(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String searchTerm,
            String filterStatus
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

        return new PagedResponse<>(
                orderDTOs,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );
    }

    public Object updateOrderStatus(Integer id, UpdateOrderStatusRequest request) {
        try {
            Optional<Order> optionalOrder = orderRepository.findById(id);
            if (optionalOrder.isEmpty()) {
                return "Nie znaleziono zamówienia o ID: " + id; // 404
            }

            Order order = optionalOrder.get();
            order.setStatus(request.getStatus());
            orderRepository.save(order);

            return "Status zamówienia został pomyślnie zaktualizowany.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Wystąpił błąd podczas aktualizacji statusu zamówienia.";
        }
    }

    // Metoda pomocnicza – konwersja Order -> GetOrderDTO
    private GetOrderDTO convertToOrderDTO(Order order) {
        String formattedAddress = formatAddress(order.getAddress());
        List<OrderItemAdminDTO> orderItems = order.getItems().stream()
                .map(item -> {
                    BigDecimal unitPrice = item.getBook().getPrice();
                    BigDecimal lineTotal = unitPrice.multiply(new BigDecimal(item.getQuantity()));
                    return new OrderItemAdminDTO(
                            item.getBook().getBookId(),
                            item.getBook().getTitlePl(),
                            item.getQuantity(),
                            lineTotal
                    );
                })
                .collect(Collectors.toList());

        String customerName = (order.getCustomer() != null)
                ? (order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName())
                : "Guest User";

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

    // -------------------------------------------------------
    // 5. CUSTOMERS (USERS)
    // -------------------------------------------------------

    public PagedResponse<CustomerAdminDTO> getUsers(int page, int size) {
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

    public Object deleteUser(Integer id) {
        Optional<Customer> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return "Nie znaleziono użytkownika o ID " + id; // 404
        }

        try {
            userRepository.delete(userOpt.get());
            return "Użytkownik został usunięty.";
        } catch (Exception e) {
            return "Nie można usunąć użytkownika, ponieważ jest powiązany z innymi danymi.";
        }
    }

    // -------------------------------------------------------
    // 6. CATEGORIES
    // -------------------------------------------------------

    public Object addCategory(CreateCategoryRequest request) {
        try {
            // Sprawdzenie, czy kategoria o podanej nazwie już istnieje
            if (categoryRepository.findByNameEn(request.getNameEn()).isPresent() ||
                    categoryRepository.findByNamePl(request.getNamePl()).isPresent()) {
                return Map.of("message", "Kategoria o podanej nazwie już istnieje.");
            }

            Category category = new Category();
            category.setNameEn(request.getNameEn());
            category.setNamePl(request.getNamePl());

            Category savedCategory = categoryRepository.save(category);
            return convertToCategoryDTO(savedCategory);

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("message", "Wystąpił błąd podczas dodawania kategorii.");
        }
    }

    public Object updateCategory(Integer id, UpdateCategoryRequest request) {
        try {
            Optional<Category> categoryOpt = categoryRepository.findById(id);
            if (categoryOpt.isEmpty()) {
                return Map.of("message", "Nie znaleziono kategorii o podanym ID.", "status", HttpStatus.NOT_FOUND);
            }

            Category category = categoryOpt.get();

            if (request.getNameEn() != null && !request.getNameEn().isEmpty()) {
                Optional<Category> existingEn = categoryRepository.findByNameEn(request.getNameEn());
                if (existingEn.isPresent() && !existingEn.get().getCategoryId().equals(id)) {
                    return Map.of("message", "Kategoria o podanej nazwie angielskiej już istnieje.");
                }
                category.setNameEn(request.getNameEn());
            }

            if (request.getNamePl() != null && !request.getNamePl().isEmpty()) {
                Optional<Category> existingPl = categoryRepository.findByNamePl(request.getNamePl());
                if (existingPl.isPresent() && !existingPl.get().getCategoryId().equals(id)) {
                    return Map.of("message", "Kategoria o podanej nazwie polskiej już istnieje.");
                }
                category.setNamePl(request.getNamePl());
            }

            Category updatedCategory = categoryRepository.save(category);
            return convertToCategoryDTO(updatedCategory);

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("message", "Wystąpił błąd podczas aktualizacji kategorii.");
        }
    }

    public Object deleteCategory(Integer id) {
        try {
            Optional<Category> categoryOpt = categoryRepository.findById(id);
            if (categoryOpt.isEmpty()) {
                return Map.of("message", "Nie znaleziono kategorii o podanym ID.", "status", HttpStatus.NOT_FOUND);
            }

            Category category = categoryOpt.get();
            if (!category.getBooks().isEmpty()) {
                return Map.of("message", "Nie można usunąć kategorii, ponieważ jest powiązana z książkami.");
            }

            categoryRepository.delete(category);
            return Map.of("message", "Kategoria została pomyślnie usunięta.");

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("message", "Wystąpił błąd podczas usuwania kategorii.");
        }
    }

    public PagedResponse<CategoryAdminDTO> getCategories(int page, int size) {
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

    public List<CategoryAdminDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toList());
    }

    // Metoda pomocnicza – Category -> CategoryAdminDTO
    private CategoryAdminDTO convertToCategoryDTO(Category category) {
        return new CategoryAdminDTO(category);
    }

    // -------------------------------------------------------
    // 7. GENRES
    // -------------------------------------------------------
    public List<String> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(Genre::getName)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // 8. REFRESH TOKENS
    // -------------------------------------------------------

    public PagedResponse<RefreshTokenAdminDTO> getRefreshTokensByEmailPaged(String email, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RefreshToken> result;

        if (email.isBlank()) {
            result = refreshTokenRepository.findAll(pageable);
        } else {
            result = refreshTokenRepository.findByEmail(email, pageable);
        }

        List<RefreshTokenAdminDTO> dtoList = result.getContent().stream()
                .map(token -> new RefreshTokenAdminDTO(
                        token.getId(),
                        token.getEmail(),
                        token.getToken(),
                        token.getExpiryDate(),
                        token.isRevoked()
                ))
                .collect(Collectors.toList());

        return new PagedResponse<>(dtoList,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }
    private RefreshTokenAdminDTO mapToDto(RefreshToken refreshToken) {
        return new RefreshTokenAdminDTO(
                refreshToken.getId(),
                refreshToken.getEmail(), // zakładam, że token ma powiązanego usera, stąd getUser()
                refreshToken.getToken(),
                refreshToken.getExpiryDate(),
                refreshToken.isRevoked()
        );
    }
    public PagedResponse<RefreshTokenAdminDTO> getAllRefreshTokensPaged(int page, int size) {
        // Tworzymy obiekt PageRequest – aktualna strona i rozmiar strony
        PageRequest pageRequest = PageRequest.of(page, size);

        // Pobieramy dane z repozytorium w formie Page<RefreshToken>
        Page<RefreshToken> tokenPage = refreshTokenRepository.findAll(pageRequest);

        // Mappujemy encje na DTO
        List<RefreshTokenAdminDTO> tokensDto = tokenPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        // Zwracamy w naszym wspólnym formacie PagedResponse
        return new PagedResponse<>(
                tokensDto,
                tokenPage.getNumber(),
                tokenPage.getSize(),
                tokenPage.getTotalElements(),
                tokenPage.getTotalPages()
        );
    }
    public Object revokeToken(Long id) {
        Optional<RefreshToken> optToken = refreshTokenRepository.findById(id);
        if (optToken.isEmpty()) {
            return Map.of("message", "Nie znaleziono tokenu o ID: " + id, "status", HttpStatus.NOT_FOUND);
        }

        RefreshToken token = optToken.get();
        if (token.isRevoked()) {
            return Map.of("message", "Token już jest unieważniony.");
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);
        return Map.of("message", "Token o ID " + id + " został unieważniony.");
    }

    // -------------------------------------------------------
    // 9. DASHBOARD STATISTICS
    // -------------------------------------------------------
    public DashboardStatsDTO getStatistics() {
        long ordersCount = orderRepository.count();
        long usersCount = userRepository.count();
        long productsCount = bookRepository.count();

        return new DashboardStatsDTO(ordersCount, usersCount, productsCount);
    }
}

