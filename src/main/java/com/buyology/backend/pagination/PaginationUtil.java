package com.buyology.backend.pagination;
import org.springframework.data.domain.Page;

import java.util.List;

public class PaginationUtil {

    public static <E,D,R extends PaginatedResponse<D>> R build (Page<E> page, List<D> dtoList, R response){
        response.setContent(dtoList);
        response.setPageNumber(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLastPage(page.isLast());
        return response;
    }
}
