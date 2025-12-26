package com.buyology.backend.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;
import com.buyology.backend.exception.BadRequestException;


public class CommonMethods {

    public static Sort sortByAndOrderBy(String sortBy, String orderBy) {
        return orderBy != null && orderBy.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

    }

    public static PageRequest getPageRequired(Integer pageNumber, Integer pageSize, Sort sortByAndOrder) {
        return PageRequest.of(pageNumber, pageSize, sortByAndOrder);
    }

    public static void validateImage(MultipartFile image) {

        if (image == null || image.isEmpty()) {
            throw new BadRequestException("Image File is Required");
        }

        String contentType = image.getContentType();
        if (contentType == null) {
            throw new BadRequestException("Invalid image content type");
        }

        if (!contentType.equals("image/jpeg")
                && !contentType.equals("image/png")
                && !contentType.equals("image/webp")) {
            throw new BadRequestException("Only JPG, PNG, and WEBP images are allowed");
        }
    }

    public static String extensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/jpeg" -> ".jpg";
            default -> throw new BadRequestException("Unsupported image type");
        };
    }
}
