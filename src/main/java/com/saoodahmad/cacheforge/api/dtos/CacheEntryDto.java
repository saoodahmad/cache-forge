package com.saoodahmad.cacheforge.api.dtos;

import com.saoodahmad.cacheforge.cache.CacheEntry;

public class CacheEntryDto {

    public String key;
    public String value;
    public long ttl;
    public boolean expired;

    public CacheEntryDto(String key, CacheEntry entry) {
        this.key = key;
        this.value = entry.getVal();
        this.ttl = entry.getExpiry();
        this.expired = entry.isKeyExpired();
    }
}
