package com.buyology.backend.dto.response;

import com.buyology.backend.dto.CategoryDTO;
import com.buyology.backend.pagination.PaginatedResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO implements PaginatedResponse<CategoryDTO> {
    private List<CategoryDTO> content;
    private Integer pageNumber;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private Boolean lastPage;
}
