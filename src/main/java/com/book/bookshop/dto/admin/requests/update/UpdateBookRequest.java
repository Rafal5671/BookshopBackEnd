package com.book.bookshop.dto.admin.requests.update;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class UpdateBookRequest {
    private String titlePL;
    private String titleEN;
    private String descriptionPL;
    private String descriptionEN;
    private String releaseDate;
    private String originalTitle;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer publisherId;
    private List<Integer> authorsIds;
    private String imageUrl;
}
