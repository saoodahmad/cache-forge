package com.saoodahmad.cacheforge.api.dtos;

public class ErrorResponse {
    public String errorCode;
    public String message;

    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
