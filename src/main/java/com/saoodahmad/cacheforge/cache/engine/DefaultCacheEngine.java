package com.saoodahmad.cacheforge.cache.engine;

import com.saoodahmad.cacheforge.cache.model.CacheEntry;
import com.saoodahmad.cacheforge.cache.model.CacheResult;
import com.saoodahmad.cacheforge.cache.policy.LRUPolicy;
import com.saoodahmad.cacheforge.cache.store.CacheStore;
import com.saoodahmad.cacheforge.cache.time.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DefaultCacheEngine  implements  CacheEngine {

    private final CacheStore store;

    private final LRUPolicy policy;

    private final int capacity;

    private static final Logger log = LoggerFactory.getLogger(DefaultCacheEngine.class);

    private final TimeProvider time;

    public DefaultCacheEngine(CacheStore store, TimeProvider time) {
        this.store = store;
        this.capacity = 5;
        this.time = time;
        this.policy = new LRUPolicy(this.capacity);
    }

    @Override
    public CacheResult setKey(String key, String val, long ttlInSecs) {
        log.debug("Executing Set Key command");

        log.debug("Key:" + key);
        log.debug("Value: " + val);
        log.debug("TTL in secs: " + ttlInSecs);

        CacheEntry entry = this.store.get(key);

        if(entry == null) {
            entry = new CacheEntry(val, ttlInSecs, this.time.nowMs());

            this.store.put(key, entry);

            this.policy.touch(key);

            List<String> lruEvictedKeys = this.policy.evictIfOverLimit();

            this.evictKeys(lruEvictedKeys);

            log.debug("New key added to cache");


            log.debug("==================================");


            return new CacheResult.Created(entry);

//            return new CacheOperationOutput(OperationType.SET, false, true, entry);

        }

        // Reuse the entry object by updating its field

        boolean expired = entry.isKeyExpired(this.time.nowMs());

        policy.forget(key);

        if(expired) {
            store.remove(key);

            entry = new CacheEntry(val, ttlInSecs, this.time.nowMs());
        }else {
            entry.updateVal(val);

            entry.setTtlSeconds(ttlInSecs, this.time.nowMs());
        }

        store.put(key, entry);

        policy.touch(key);

        List<String> lruEvictedKeys = policy.evictIfOverLimit();

        this.evictKeys(lruEvictedKeys);

        log.debug("Existing key refreshed in cache");

        log.debug("==================================");


        if(expired) {
            return  new CacheResult.Created(entry);
        }

        return new CacheResult.Updated(entry);

//        return new CacheOperationOutput(OperationType.SET, !expired, expired, entry);
    }

    @Override
    public CacheResult getKey(String key) {
        log.debug("Executing Get Key command");

        log.debug("Key: " + key);

        CacheEntry entry = this.store.get(key);

        if(entry == null) {
            log.debug("Key does not exist in cache");

            log.debug("==================================");

            return new CacheResult.Miss();
        }

        boolean keyExpired =  entry.isKeyExpired(this.time.nowMs());

        if(keyExpired) {
            this.store.remove(key);
            this.policy.forget(key);

            log.debug("Key is in cache but expired");

            log.debug("==================================");

            return new CacheResult.Expired();
        }

        this.policy.touch(key);

        log.debug("Key found in cache");

        log.debug("==================================");

        return new CacheResult.Hit(entry);
    }

    @Override
    public CacheResult deleteKey(String key) {
        log.debug("Executing Delete Key command");

        log.debug("Key: " + key);

        CacheEntry entry = this.store.get(key);

        if(entry == null) {
            log.debug("Key does not exist in cache");

            log.debug("==================================");

            return new CacheResult.Miss();

//            return new CacheOperationOutput(OperationType.DELETE, false, true, null);
        }

        boolean expired = entry.isKeyExpired(this.time.nowMs());

        this.store.remove(key);
        this.policy.forget(key);

        if(expired) {
            log.debug("Key is in cache but expired");

            log.debug("==================================");

            return new CacheResult.Expired();
//            return new CacheOperationOutput(OperationType.DELETE, false, true, null);
        }

        log.debug("Key deleted from cache");

        log.debug("==================================");

        return new CacheResult.Hit(entry);
//        return new CacheOperationOutput(OperationType.DELETE, true, false,  entry);
    }

    @Override
    public void evictKeys(List<String> lruEvictedKeys) {
        for (String key : lruEvictedKeys) {
            this.store.remove(key);
        }
    }

    @Override
    public Set<String> snapshotKeys() {
        return new HashSet<>(store.keys());
    }

    @Override
    public List<String> snapshotLRU() {
        return this.policy.snapshotLRU();
    }

    @Override
    public CacheEntry getKeyDirectlyFromStore(String key) {
        return this.store.get(key);
    }

    @Override
    public  int cacheCapacity() {
        return this.capacity;
    }

}
