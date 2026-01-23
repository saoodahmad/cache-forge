package com.saoodahmad.cacheforge.cache.policy;

import com.saoodahmad.cacheforge.cache.model.CacheKey;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class LRUPolicy {

    private final LinkedHashMap<CacheKey, Boolean> accessOrder;

    private volatile int maxEntries;

    public LRUPolicy(int maxEntries) {
        if (maxEntries <= 0) throw new IllegalArgumentException("maxEntries must be > 0");

        this.maxEntries = maxEntries;

        this.accessOrder = new LinkedHashMap<>(this.maxEntries, 0.75f, true);
    }

    public int getMaxEntries() {
        return this.maxEntries;
    }


    public void setMaxEntries(int maxEntries) {
        if (maxEntries <= 0) throw new IllegalArgumentException("maxEntries must be > 0");

        this.maxEntries = maxEntries;
    }

    public int size() {

        return this.accessOrder.size();

    }

    public void touch(CacheKey key) {
        if (key == null) return;

        this.accessOrder.put(key, Boolean.TRUE); // put() also "touches" in access-order mode

    }

    public void forget(CacheKey key) {
        if (key == null) return;

        this.accessOrder.remove(key);

    }


    public List<CacheKey> evictIfOverLimit() {
        List<CacheKey> evicted = new ArrayList<>();

        int limit = this.maxEntries;
        while (this.accessOrder.size() > limit) {
            CacheKey oldest = this.eldestKeyUnsafe();
            if (oldest == null) break;
            this.accessOrder.remove(oldest);
            evicted.add(oldest);
        }

        return evicted;
    }

    private CacheKey eldestKeyUnsafe() {
        for (Map.Entry<CacheKey, Boolean> e : this.accessOrder.entrySet()) {
            return e.getKey(); // first entry is LRU in access-order mode
        }

        return null;
    }

    public ArrayList<CacheKey> snapshotLRU() {
        return new ArrayList<>(this.accessOrder.keySet()); // [LRU ... MRU]
    }

    public void clear() {
        this.accessOrder.clear();
    }


}
