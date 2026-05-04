package com.book.bookshop.repo;

import com.book.bookshop.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query("SELECT o FROM Order o " +
            "WHERE ( lower(str(o.orderId)) LIKE lower(concat('%', :searchTerm, '%')) " +
            "        OR lower(str(o.orderDate)) LIKE lower(concat('%', :searchTerm, '%')) ) " +
            "AND ( :status IS NULL OR o.status = :status )")
    Page<Order> findBySearchAndStatus(@Param("searchTerm") String searchTerm,
                                      @Param("status") String status,
                                      Pageable pageable);
}
