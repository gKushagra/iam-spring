package com.softwright.iam.models;

import jakarta.validation.constraints.NotBlank;

public class MailResponse {
    @NotBlank
    public String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String text) {
        this.message = text;
    }
}
