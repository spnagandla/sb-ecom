package com.buyology.backend.security.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {

    @NotBlank
    @Size(min = 3, max=20)
    private String userName;

    @NotBlank
    @Email
    @Size(max=20)
    private String email;

    @NotBlank
    @Size(min=6,max=15)
    private String password;

    private Set<String> role;
}
