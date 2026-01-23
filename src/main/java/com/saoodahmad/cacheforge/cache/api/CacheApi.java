package com.saoodahmad.cacheforge.cache.api;

import com.saoodahmad.cacheforge.cache.OperationType;
import com.saoodahmad.cacheforge.cache.engine.MetricsEngine;
import com.saoodahmad.cacheforge.cache.model.CacheEntry;
import com.saoodahmad.cacheforge.cache.model.CacheKey;
import com.saoodahmad.cacheforge.cache.model.CacheResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CacheApi {

    private final MetricsEngine engine;

    public CacheApi(MetricsEngine engine) {
        this.engine = engine;
    }

    public CacheOperationOutput getKey(String namespace, String key) {
        CacheKey cKey = new CacheKey(namespace, key);

        CacheResult result = engine.getKey(cKey);

        if (result instanceof CacheResult.Hit(CacheEntry entry)) {
            return new CacheOperationOutput(
                    OperationType.GET,
                    true,
                    false,
                    entry
            );
        }

        return new CacheOperationOutput(
                OperationType.GET,
                false,
                true,
                null
        );
    }

    public CacheOperationOutput setKey(String namespace, String key, String val, long ttl) {
        CacheKey cKey = new CacheKey(namespace, key);

        CacheResult result = engine.setKey(cKey, val, ttl);

        if (result instanceof CacheResult.Created(CacheEntry entry)) {
            return new CacheOperationOutput(
                    OperationType.SET,
                    false,
                    true,
                    entry
            );
        }

        if (result instanceof CacheResult.Updated(CacheEntry entry)) {
            return new CacheOperationOutput(OperationType.SET, true, false, entry);
        }

        throw new IllegalStateException("Unhandled CacheResult in setKey: " + result);
    }

    public CacheOperationOutput deleteKey(String namespace, String key) {
        CacheKey cKey = new CacheKey(namespace, key);

        CacheResult result = engine.deleteKey(cKey);

        if (result instanceof CacheResult.Hit(CacheEntry entry)) {
            return new CacheOperationOutput(
                    OperationType.DELETE,
                    true,
                    false,
                    entry
            );

        }

        return new CacheOperationOutput(
            OperationType.DELETE,
            false,
            true,
            null
        );
    }

    public Map<Integer, List<CacheKey>> snapshotKeys() {
        return engine.snapshotKeys();
    }

    public Map<Integer, List<CacheKey>> snapshotLRU() {
        return engine.snapshotLRU();
    }

    public CacheEntry getCacheEntryDirectlyFromStore(String namespace, String key) {
        CacheKey cKey = new CacheKey(namespace, key);

        return engine.getCacheEntryDirectlyFromStore(cKey);
    }

    public int cacheCapacity() {
        return engine.cacheCapacity();
    }

    public void clear() {
        engine.clear();
    }
}
