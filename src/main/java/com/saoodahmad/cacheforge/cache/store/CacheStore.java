package com.saoodahmad.cacheforge.cache.store;

import com.saoodahmad.cacheforge.cache.model.CacheEntry;

import java.util.Set;

public interface CacheStore {

    CacheEntry get(String key);

    void put(String key, CacheEntry entry);

    CacheEntry remove(String key);

    boolean containsKey(String key);

    Set<String> keys();

    void clear();

}
