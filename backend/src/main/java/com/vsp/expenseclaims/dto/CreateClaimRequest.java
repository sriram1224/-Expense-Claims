package com.vsp.expenseclaims.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateClaimRequest {

    @NotBlank(message = "Title is required")
    private String title;

    public CreateClaimRequest() {}

    public CreateClaimRequest(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
