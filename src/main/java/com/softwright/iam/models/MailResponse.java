package com.softwright.iam.models;

import jakarta.validation.constraints.NotBlank;

public class MailResponse {
    @NotBlank
    public String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
