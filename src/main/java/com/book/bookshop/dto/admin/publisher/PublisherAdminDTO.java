package com.book.bookshop.dto.admin.publisher;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PublisherAdminDTO {
    private Integer publisherId;
    private String name;
}
