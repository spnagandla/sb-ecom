package com.buyology.backend.security.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {

    private String message;
    private Boolean success;
    private Instant timeStamp;

    public MessageResponse(String message) {
        this.message = message;
        this.success = true;
        this.timeStamp = Instant.now();
    }
}
