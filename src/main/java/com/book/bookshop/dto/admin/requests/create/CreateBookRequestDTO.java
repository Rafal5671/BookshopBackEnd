package com.book.bookshop.dto.admin.requests.create;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
public class CreateBookRequestDTO {
    @NotBlank
    private String titlePL;
    @NotBlank
    private String titleEN;
    @NotBlank
    private String descriptionPL;
    @NotBlank
    private String descriptionEN;
    @NotBlank
    private String releaseDate;
    @NotBlank
    private String originalTitle;
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal salePrice;
    @NotNull
    private Integer publisherId;
    @NotEmpty
    private List<Integer> authorsIds;
    @NotBlank
    private String imageUrl;
}

