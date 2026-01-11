package com.saoodahmad.cacheforge.cache.store;

import com.saoodahmad.cacheforge.cache.model.CacheEntry;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryCacheStore implements CacheStore {

    private final ConcurrentHashMap<String, CacheEntry> store;

    public InMemoryCacheStore() {
        this.store = new ConcurrentHashMap<>();
    }

    @Override
    public CacheEntry get(String key) {
       return this.store.get(key);
    }

    @Override
    public void put(String key, CacheEntry entry) {
        this.store.put(key, entry);
    }

    @Override
    public CacheEntry remove(String key) {
        return this.store.remove(key);
    }

    @Override
    public boolean containsKey(String key) {
        return this.store.containsKey(key);
    }

    @Override
    public Set<String> keys() {
        return this.store.keySet();
    }

    @Override
    public void clear() {
        this.store.clear();
    }
}
