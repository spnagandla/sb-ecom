package com.buyology.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {

    private Long productId;
    private String productName;
    private String imagePath;
    private String description;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal specialPrice;
    private BigDecimal discount;
    private Long version;

}
