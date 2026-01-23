package com.saoodahmad.cacheforge.cache.engine;

import com.saoodahmad.cacheforge.cache.config.CacheForgeProperties;
import com.saoodahmad.cacheforge.cache.model.CacheEntry;
import com.saoodahmad.cacheforge.cache.model.CacheKey;
import com.saoodahmad.cacheforge.cache.model.CacheResult;
import com.saoodahmad.cacheforge.cache.stripe.CacheStripe;
import com.saoodahmad.cacheforge.cache.policy.LRUPolicy;
import com.saoodahmad.cacheforge.cache.store.CacheStore;
import com.saoodahmad.cacheforge.cache.store.InMemoryCacheStore;
import com.saoodahmad.cacheforge.cache.stripe.StripeRouter;
import com.saoodahmad.cacheforge.cache.time.TimeProvider;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class StripedCacheEngine implements DefaultEngine {

    private static final Logger log = LoggerFactory.getLogger(StripedCacheEngine.class);

    private final TimeProvider time;
    private final int totalCapacity;
    private final CacheStripe[] stripes;

    private final StripeRouter stripeRouter;

    public StripedCacheEngine(CacheForgeProperties props, TimeProvider time) {

        this.time = time;

        int stripesCount = props.getStripes();
        this.totalCapacity = props.getCapacity();

        validateStripesAndCapacity(stripesCount, this.totalCapacity);

        int perStripeCap = this.totalCapacity / stripesCount;

        this.stripes = new CacheStripe[stripesCount];

        for (int i = 0; i < stripesCount; i++) {
            CacheStore store = new InMemoryCacheStore();
            LRUPolicy lru = new LRUPolicy(perStripeCap);

            stripes[i] = new CacheStripe(i, perStripeCap, store, lru);

        }

        this.stripeRouter = new StripeRouter(stripesCount);

    }

    @Override
    public CacheResult setKey(CacheKey cKey, String val, long ttlInSecs) {

        log.debug("Executing Set Key command");

        log.debug("Key:{}", cKey);
        log.debug("Value: {}", val);
        log.debug("TTL in secs: {}", ttlInSecs);

        CacheStripe stripe = this.stripeFor(cKey);

        stripe.lock.lock();

        try {
            CacheEntry entry = stripe.store.get(cKey);

            if (entry == null) {
                entry = new CacheEntry(val, ttlInSecs, time.nowNs());
                stripe.store.put(cKey, entry);
                stripe.lru.touch(cKey);

                List<CacheKey> lruEvictedKeys = stripe.lru.evictIfOverLimit();

                this.evictKeys(lruEvictedKeys, stripe);

                log.debug("New key added to cache");

                log.debug("==================================");

                return new CacheResult.Created(entry);
            }

            long nowNs = this.time.nowNs();

            boolean expired = entry.isKeyExpired(nowNs);

            if (expired) {
                stripe.store.remove(cKey);
                stripe.lru.forget(cKey);

                entry = new CacheEntry(val, ttlInSecs, nowNs);
            } else {
                entry.updateVal(val);

                entry.setTtlSeconds(ttlInSecs, nowNs);
            }

            stripe.store.put(cKey, entry);

            stripe.lru.touch(cKey);

            List<CacheKey> lruEvictedKeys = stripe.lru.evictIfOverLimit();

            this.evictKeys(lruEvictedKeys, stripe);

            log.debug("Existing key refreshed in cache");

            log.debug("==================================");

            if (expired) {
                return new CacheResult.Created(entry);
            }

            return new CacheResult.Updated(entry);

        } finally {
            stripe.lock.unlock();
        }
    }

    @Override
    public CacheResult getKey(CacheKey cKey) {

        log.debug("Executing Get Key command");

        log.debug("Key: {}", cKey);

        CacheStripe stripe = this.stripeFor(cKey);

        stripe.lock.lock();

        try {

            CacheEntry entry = stripe.store.get(cKey);

            if (entry == null) {
                log.debug("Key does not exist in cache");

                log.debug("==================================");

                return new CacheResult.Miss();
            }

            boolean keyExpired = entry.isKeyExpired(this.time.nowNs());

            if (keyExpired) {
                stripe.store.remove(cKey);
                stripe.lru.forget(cKey);

                log.debug("Key is in cache but expired");

                log.debug("==================================");

                return new CacheResult.Expired();
            }

            stripe.lru.touch(cKey);

            log.debug("Key found in cache");

            log.debug("==================================");

            return new CacheResult.Hit(entry);
        } finally {
            stripe.lock.unlock();
        }
    }

    @Override
    public CacheResult deleteKey(CacheKey cKey) {

        log.debug("Executing Delete Key command");

        log.debug("Key: {}", cKey);

        CacheStripe stripe = stripeFor(cKey);

        stripe.lock.lock();

        try {
            CacheEntry entry = stripe.store.get(cKey);

            if (entry == null) {
                log.debug("Key does not exist in cache");

                log.debug("==================================");

                return new CacheResult.Miss();
            }

            boolean expired = entry.isKeyExpired(this.time.nowNs());

            stripe.store.remove(cKey);
            stripe.lru.forget(cKey);

            if (expired) {
                log.debug("Key is in cache but expired");

                log.debug("==================================");

                return new CacheResult.Expired();
            }

            log.debug("Key deleted from cache");

            log.debug("==================================");

            return new CacheResult.Hit(entry);
        } finally {
            stripe.lock.unlock();
        }
    }

    @Override
    public CacheEntry getCacheEntryDirectlyFromStore(CacheKey cKey, Counter expiredCounter) {
        long timeNow = this.time.nowNs();

        CacheStripe stripe = stripeFor(cKey);

        stripe.lock.lock();

        try {
            CacheEntry entry = stripe.store.get(cKey);

            if (entry.isKeyExpired(timeNow) && !entry.expiryCounted()) {
                expiredCounter.increment();
                entry.markExpiryCounted();
            }

            return entry;
        } finally {
            stripe.lock.unlock();
        }
    }

    @Override
    public void evictKeys(List<CacheKey> lruEvictedKeys, CacheStripe stripe) {
        for (CacheKey key : lruEvictedKeys) {
            stripe.store.remove(key);
        }
    }

    @Override
    public HashMap<Integer, List<CacheKey>> snapshotKeys() {
        HashMap<Integer, List<CacheKey>> storeKeys = new HashMap<>();

        for (CacheStripe stripe : stripes) {
            stripe.lock.lock();

            try {
                storeKeys.put(stripe.id, stripe.store.keys());
            } finally {
                stripe.lock.unlock();
            }
        }

        return storeKeys;

    }

    @Override
    public HashMap<Integer, List<CacheKey>> snapshotLRU() {
        HashMap<Integer, List<CacheKey>> lruKeys = new HashMap<>();

        for (CacheStripe stripe : stripes) {
            stripe.lock.lock();

            try {
                lruKeys.put(stripe.id, stripe.lru.snapshotLRU());
            } finally {
                stripe.lock.unlock();
            }
        }

        return lruKeys;
    }

    @Override
    public int cacheCapacity() {
        return this.totalCapacity;
    }

    public CacheStripe stripeFor(CacheKey ck) {
        int idx = this.stripeRouter.stripeIndex(ck);

        return stripes[idx];
    }

    public static void validateStripesAndCapacity(int stripes, int capacity) {
        if (stripes <= 0) {
            throw new IllegalArgumentException("Stripes must be > 0");
        }

        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity count must be > 0");
        }

        if (capacity % stripes != 0) {
            throw new IllegalArgumentException("Capacity must be divisible by no of stripes");
        }

    }

    @Override
    public void clear() {
        for (CacheStripe stripe : stripes) {
            stripe.lock.lock();
            try {
                stripe.clear();
            } finally {
                stripe.lock.unlock();
            }
        }

    }

}
