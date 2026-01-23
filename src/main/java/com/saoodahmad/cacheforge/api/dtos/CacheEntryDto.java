package com.saoodahmad.cacheforge.api.dtos;

import com.saoodahmad.cacheforge.cache.model.CacheEntry;

public class CacheEntryDto {

    public String namespace;
    public String key;
    public String value;
    public long ttl;
    public boolean expired;

    public CacheEntryDto(String namespace, String key, CacheEntry entry, long nowNs) {
        this.namespace = namespace;
        this.key = key;
        this.value = entry.getVal();
        this.ttl = entry.getTtlInSecs();
        this.expired = entry.isKeyExpired(nowNs);
    }
}
