package com.softwright.iam.models;

import jakarta.validation.constraints.NotBlank;

public class ProcessResetRequest {
    @NotBlank
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
