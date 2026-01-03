package com.saoodahmad.cacheforge.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLOutput;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {

    ConcurrentHashMap<String, CacheEntry> store;

    LRUPolicy policy;

    int capacity;

    public Cache() {
        this.store = new ConcurrentHashMap<>();
        this.capacity = 5;
        this.policy = new LRUPolicy(this.capacity);
    }

    public CacheOperationOutput setKey(String key, String val, long ttlInSecs) {
        System.out.println("Executing Set Key command");

        System.out.println("Key:" + key);
        System.out.println("Value: " + val);
        System.out.println("TTL in secs: " + ttlInSecs);

        CacheEntry entry = this.store.get(key);

        if(entry == null) {
            entry = new CacheEntry(val, ttlInSecs);

            this.store.put(key, entry);

            this.policy.touch(key);

            List<String> lruEvictedKeys = this.policy.evictIfOverLimit();

            this.evictKeys(lruEvictedKeys);

            System.out.println("New key added to cache");


            System.out.println("==================================");


            return new CacheOperationOutput(OperationType.SET, false, true, entry);

        }

        // Reuse the entry object by updating its field

        boolean expired = entry.isKeyExpired();

        policy.forget(key);

        if(expired) {
            store.remove(key);

            entry = new CacheEntry(val, ttlInSecs);
        }else {
            entry.updateVal(val);

            entry.updateExpiry(ttlInSecs);
        }

        store.put(key, entry);

        policy.touch(key);

        List<String > lruEvictedKeys = policy.evictIfOverLimit();

        this.evictKeys(lruEvictedKeys);

        System.out.println("Existing key refreshed in cache");

        System.out.println("==================================");


        return new CacheOperationOutput(OperationType.SET, !expired, expired, entry);
    }

    public CacheOperationOutput getKey(String key) {
        System.out.println("Executing Get Key command");

        System.out.println("Key: " + key);

        CacheEntry entry = this.store.get(key);

        if(entry == null) {
            System.out.println("Key does not exist in cache");

            System.out.println("==================================");

            return new CacheOperationOutput(OperationType.GET, false, true, null);
        }

        boolean keyExpired =  entry.isKeyExpired();

        if(keyExpired) {
            this.store.remove(key);
            this.policy.forget(key);

            System.out.println("Key is in cache but expired");

            System.out.println("==================================");

            return new CacheOperationOutput(OperationType.GET, false, true, null);
        }

        this.policy.touch(key);

        System.out.println("Key found in cache");

        System.out.println("==================================");

        return new CacheOperationOutput(OperationType.GET,  true, false, entry);
    }


    public CacheEntry getKeyDirectlyFromStore(String key) {

        return this.store.get(key);
    }

    public CacheOperationOutput deleteKey(String key) {
        System.out.println("Executing Delete Key command");

        System.out.println("Key: " + key);

        CacheEntry entry = this.store.get(key);

        if(entry == null) {
            System.out.println("Key does not exist in cache");

            System.out.println("==================================");

            return new CacheOperationOutput(OperationType.DELETE, false, true, null);
        }

        boolean expired = entry.isKeyExpired();

        store.remove(key);
        policy.forget(key);

        if(expired) {
            System.out.println("Key is in cache but expired");

            System.out.println("==================================");

            return new CacheOperationOutput(OperationType.DELETE, false, true, null);
        }

        System.out.println("Key deleted from cache");

        System.out.println("==================================");

        return new CacheOperationOutput(OperationType.DELETE, true, false,  entry);
    }

    public void evictKeys(List<String> lruEvictedKeys) {
        for(String key: lruEvictedKeys) {
            this.store.remove(key);
        }
    }

    public Set<String> snapshotKeys() {
        return new HashSet<>(this.store.keySet());
    }

    public List<String> snapshotLRU() {
        return this.policy.snapshotLRU();
    }

    public int cacheCapacity() {
        return this.capacity;
    }
}
