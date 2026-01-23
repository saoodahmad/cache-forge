package com.saoodahmad.cacheforge.api.dtos;

import jakarta.validation.constraints.NotBlank;

public class RootResponse {
    public String message;

    public RootResponse(String message) {
        this.message = message;
    }
}
