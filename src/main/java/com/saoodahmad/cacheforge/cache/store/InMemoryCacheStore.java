package com.saoodahmad.cacheforge.cache.store;

import com.saoodahmad.cacheforge.cache.model.CacheEntry;
import com.saoodahmad.cacheforge.cache.model.CacheKey;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryCacheStore implements CacheStore {

    private final ConcurrentHashMap<CacheKey, CacheEntry> store;

    public InMemoryCacheStore() {
        this.store = new ConcurrentHashMap<CacheKey, CacheEntry>();
    }

    @Override
    public CacheEntry get(CacheKey key) {
        return this.store.get(key);
    }

    @Override
    public void put(CacheKey key, CacheEntry entry) {
        this.store.put(key, entry);
    }

    @Override
    public CacheEntry remove(CacheKey key) {
        return this.store.remove(key);
    }

    @Override
    public boolean containsKey(CacheKey key) {
        return this.store.containsKey(key);
    }

    @Override
    public List<CacheKey> keys() {
        return this.store.keySet().stream().toList();
    }

    @Override
    public void clear() {
        this.store.clear();
    }

}
