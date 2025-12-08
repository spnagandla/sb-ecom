package com.buyology.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {

        private Long categoryId;
        private String categoryName;
        private LocalDateTime createdAt;
}
