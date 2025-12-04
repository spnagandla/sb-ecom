package com.buyology.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name ="categories")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @NotBlank(message =" Category name can't be null")
    @Size(min = 5,message = "Category must contain at least 5 characters")
    private String categoryName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Adding this annotation will tell hibernate to run this method before saving the record so we will get the createdAt assigned with a value
    @PrePersist
    public void setCreatedDateTime(){
            this.createdAt = LocalDateTime.now();
    }

}
