package com.buyology.backend.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class CommonMethods {

    public static Sort sortByAndOrderBy (String sortBy, String orderBy) {
        return orderBy != null && orderBy.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

    }
    public static PageRequest getPageRequired (Integer pageNumber, Integer pageSize, Sort sortByAndOrder) {
        return PageRequest.of(pageNumber, pageSize, sortByAndOrder);
    }
}
