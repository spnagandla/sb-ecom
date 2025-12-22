package com.buyology.backend.pagination;

import java.util.List;

public interface PaginatedResponse<T> {
    void setContent(List<T> content);
    void setPageNumber(Integer pageNumber);
    void setPageSize(Integer pageSize);
    void setTotalElements(Long totalElements);
    void setTotalPages(Integer totalPages);
    void setLastPage(Boolean lastPage);
}
