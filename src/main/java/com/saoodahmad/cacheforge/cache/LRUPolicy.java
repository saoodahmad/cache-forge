package com.saoodahmad.cacheforge.cache;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class LRUPolicy {

    private final Object lock = new Object();

    private final LinkedHashMap<String, Boolean> accessOrder =
            new LinkedHashMap<>(16, 0.75f, true);

    private volatile int maxEntries;

    public LRUPolicy(int maxEntries) {
        if (maxEntries <= 0) throw new IllegalArgumentException("maxEntries must be > 0");

        this.maxEntries = maxEntries;
    }

    public int getMaxEntries() {
        return this.maxEntries;
    }


    public void setMaxEntries(int maxEntries) {
        if (maxEntries <= 0) throw new IllegalArgumentException("maxEntries must be > 0");

        this.maxEntries = maxEntries;
    }

    public int size() {
        synchronized (lock) {
            return this.accessOrder.size();
        }
    }

    public void touch(String key) {
        if (key == null) return;

        synchronized (lock) {
            this.accessOrder.put(key, Boolean.TRUE); // put() also "touches" in access-order mode
        }
    }

    public void forget(String key) {
        if (key == null) return;

        synchronized (lock) {
            this.accessOrder.remove(key);
        }
    }


    public List<String> evictIfOverLimit() {
        List<String> evicted = new ArrayList<>();
        synchronized (lock) {
            int limit = this.maxEntries;
            while (this.accessOrder.size() > limit) {
                String oldest = this.eldestKeyUnsafe();
                if (oldest == null) break;
                this.accessOrder.remove(oldest);
                evicted.add(oldest);
            }
        }
        return evicted;
    }

    public String peekOldest() {
        synchronized (lock) {
            return this.eldestKeyUnsafe();
        }
    }

    private String eldestKeyUnsafe() {


        for (Map.Entry<String, Boolean> e : this.accessOrder.entrySet()) {
            return e.getKey(); // first entry is LRU in access-order mode
        }

        return null;
    }

    public ArrayList<String> snapshotLRU() {
        synchronized (lock) {
            return new ArrayList<>(this.accessOrder.keySet()); // [LRU ... MRU]
        }
    }
}
