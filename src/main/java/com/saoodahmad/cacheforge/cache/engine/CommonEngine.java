package com.saoodahmad.cacheforge.cache.engine;

import com.saoodahmad.cacheforge.cache.model.CacheKey;
import com.saoodahmad.cacheforge.cache.model.CacheResult;
import com.saoodahmad.cacheforge.cache.stripe.CacheStripe;

import java.util.HashMap;
import java.util.List;

public interface CommonEngine {

    CacheResult setKey(CacheKey rawKey, String val, long ttlInSecs);

    CacheResult getKey(CacheKey key);

    CacheResult deleteKey(CacheKey key);

    void evictKeys(List<CacheKey> lruEvictedKeys, CacheStripe stripe);

    HashMap<Integer, List<CacheKey>> snapshotKeys();

    HashMap<Integer, List<CacheKey>> snapshotLRU();

    int cacheCapacity();

    void clear();
}
