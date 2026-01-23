package com.saoodahmad.cacheforge.api.dtos;

public class LRUEntryDto {

    public String namespace;
    public String key;

    public LRUEntryDto(String namespace, String key) {
        this.namespace = namespace;
        this.key = key;
    }
}
