package com.softwright.iam.models;

import jakarta.validation.constraints.NotBlank;

public class AuthCallback {
    @NotBlank
    public String token;

    public AuthCallback(String token) {
        this.token = token;
    }
}
