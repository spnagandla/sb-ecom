package com.buyology.backend.exception;

public class ResourceNotFoundException extends RuntimeException{

    String resourceName;
    String fieldName;
    String field;
    Long fieldId;

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String field, String resourceName, String fieldName) {
        super(String.format("%s not found with %s: %s", resourceName, field, fieldName));
        this.field = field;
        this.resourceName = resourceName;
        this.fieldName = fieldName;
    }

    public ResourceNotFoundException(String resourceName, String field, Long fieldId) {
        super(String.format("%s not found with %s: %d", resourceName, field, fieldId));
        this.resourceName = resourceName;
        this.field = field;
        this.fieldId = fieldId;
    }
}
