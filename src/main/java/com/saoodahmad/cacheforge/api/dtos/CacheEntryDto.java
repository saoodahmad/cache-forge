package com.saoodahmad.cacheforge.api.dtos;

import com.saoodahmad.cacheforge.cache.model.CacheEntry;


public class CacheEntryDto {

    public String key;
    public String value;
    public long ttl;
    public boolean expired;

    public CacheEntryDto(String key, CacheEntry entry, long nowMs) {
        this.key = key;
        this.value = entry.getVal();
        this.ttl = entry.getTtlInSecs();
        this.expired = entry.isKeyExpired(nowMs);
    }
}
