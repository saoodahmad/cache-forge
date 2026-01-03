package com.saoodahmad.cacheforge.cache;

public enum OperationType {
    SET("Set"),
    GET("Get"),
    DELETE("Del");

    private final String description;

    OperationType(String description) {
        this.description = description;
    }

    public String desc() {
        return this.description;
    }
}
