package com.saoodahmad.cacheforge.cache.api;

import com.saoodahmad.cacheforge.cache.OperationType;
import com.saoodahmad.cacheforge.cache.engine.CacheEngine;
import com.saoodahmad.cacheforge.cache.model.CacheEntry;
import com.saoodahmad.cacheforge.cache.model.CacheResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class CacheApi {

    private final CacheEngine engine;

    public CacheApi(CacheEngine engine) {
        this.engine = engine;
    }

    public CacheOperationOutput getKey(String key) {
        CacheResult result = engine.getKey(key);

        if (result instanceof CacheResult.Hit hit) {
            return new CacheOperationOutput(
                    OperationType.GET,
                    true,
                    false,
                    hit.entry()
            );
        }

        // Miss OR Expired → miss in current API contract
        return new CacheOperationOutput(
                OperationType.GET,
                false,
                true,
                null
        );
    }

    public CacheOperationOutput setKey(String key, String val, long ttl) {
        CacheResult result = engine.setKey(key, val, ttl);

        if (result instanceof  CacheResult.Created created) {
            return new CacheOperationOutput(
                    OperationType.SET,
                    false,
                    true,
                    created.entry()
            );
        }

        if (result instanceof  CacheResult.Updated updated) {
            return new CacheOperationOutput(OperationType.SET, true, false, updated.entry() );
        }

        throw new IllegalStateException("Unhandled CacheResult in setKey: " + result);
    }

    public CacheOperationOutput deleteKey(String key) {
        CacheResult result = engine.deleteKey(key);

        if (result instanceof CacheResult.Hit hit) {
            return new CacheOperationOutput(
                    OperationType.DELETE,
                    true,
                    false,
                    hit.entry()
            );

        }

        return new CacheOperationOutput(
            OperationType.DELETE,
            false,
            true,
            null
        );
    }

    public Set<String> snapshotKeys() {
        return engine.snapshotKeys();
    }

    public List<String> snapshotLRU() {
        return engine.snapshotLRU();
    }

    public CacheEntry getKeyDirectlyFromStore(String key) {
        return engine.getKeyDirectlyFromStore(key);
    }

    public int cacheCapacity() {
        return engine.cacheCapacity();
    }


}
