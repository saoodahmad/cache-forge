package com.saoodahmad.cacheforge.cache.stripe;

import com.saoodahmad.cacheforge.cache.policy.LRUPolicy;
import com.saoodahmad.cacheforge.cache.store.CacheStore;

import java.util.concurrent.locks.ReentrantLock;

public class CacheStripe {

    public final int id;
    public final int capacity;
    public final CacheStore store;
    public final LRUPolicy lru;
    public final ReentrantLock lock = new ReentrantLock();

    public CacheStripe(int id, int capacity, CacheStore store, LRUPolicy lru) {
        this.id = id;
        this.capacity = capacity;
        this.store = store;
        this.lru = lru;
    }

    public int getStripeId() {
        return this.id;
    }

    public void clear() {
        this.store.clear();
        this.lru.clear();
    }
}
