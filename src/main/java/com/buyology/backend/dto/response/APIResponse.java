package com.buyology.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class APIResponse {

    private String message;
    private boolean success;
    private int status;
    private LocalDateTime timestamp;
    private String path;
}
