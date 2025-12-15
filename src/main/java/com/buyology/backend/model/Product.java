package com.buyology.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @SequenceGenerator(
            name = "product_seq",
            sequenceName = "product_seq",
            allocationSize = 50
    )
    private Long productId;
    private String productName;
    private String description;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal specialPrice;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(updatable = false)
    private LocalDateTime createdAt;


    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @PrePersist
    public void setCreatedDateTime(){
        this.createdAt = LocalDateTime.now();
    }

}
