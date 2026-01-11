package com.saoodahmad.cacheforge.cache.engine;

import com.saoodahmad.cacheforge.cache.model.CacheEntry;
import com.saoodahmad.cacheforge.cache.model.CacheResult;

import java.util.List;
import java.util.Set;

public interface CacheEngine {
    CacheResult setKey(String key, String val, long ttlInSecs);

    CacheResult getKey(String key);

    CacheResult deleteKey(String key);

    CacheEntry getKeyDirectlyFromStore(String key);

    void evictKeys(List<String> lruEvictedKeys);

    Set<String> snapshotKeys();

    List<String> snapshotLRU();

    int cacheCapacity();
}
