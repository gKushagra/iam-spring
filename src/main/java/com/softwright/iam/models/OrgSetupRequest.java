package com.softwright.iam.models;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class OrgSetupRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String email;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
