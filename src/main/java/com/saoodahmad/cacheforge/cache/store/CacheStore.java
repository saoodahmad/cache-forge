package com.saoodahmad.cacheforge.cache.store;

import com.saoodahmad.cacheforge.cache.model.CacheEntry;
import com.saoodahmad.cacheforge.cache.model.CacheKey;

import java.util.List;

public interface CacheStore {

    CacheEntry get(CacheKey key);

    void put(CacheKey key, CacheEntry entry);

    CacheEntry remove(CacheKey key);

    boolean containsKey(CacheKey key);

    List<CacheKey> keys();

    void clear();

}
