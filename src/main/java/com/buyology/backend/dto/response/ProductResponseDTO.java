package com.buyology.backend.dto.response;

import com.buyology.backend.dto.ProductDTO;
import com.buyology.backend.pagination.PaginatedResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDTO implements PaginatedResponse<ProductDTO> {
    private List<ProductDTO> content;
    private Integer pageNumber;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private Boolean lastPage;
}
