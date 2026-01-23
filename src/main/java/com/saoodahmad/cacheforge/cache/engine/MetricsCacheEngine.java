package com.saoodahmad.cacheforge.cache.engine;

import com.saoodahmad.cacheforge.cache.model.CacheEntry;
import com.saoodahmad.cacheforge.cache.model.CacheKey;
import com.saoodahmad.cacheforge.cache.model.CacheResult;
import com.saoodahmad.cacheforge.cache.stripe.CacheStripe;
import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MetricsCacheEngine implements MetricsEngine {

    private final DefaultEngine delegate;

    private final Counter getCalls, getHits, getMisses, getExpired;
    private final Counter setCalls, setCreated, setUpdated;
    private final Counter delCalls, delHits, delMisses, delExpired;

    private final Timer getTimer, setTimer, delTimer;

    private final AtomicLong lastGetNs = new AtomicLong();
    private final AtomicLong lastSetNs = new AtomicLong();
    private final AtomicLong lastDelNs = new AtomicLong();

    // gauges can be registered against *this* (delegate snapshots)
    public MetricsCacheEngine(StripedCacheEngine delegate, MeterRegistry registry) {
        this.delegate = delegate;

        Tags base = Tags.of("cache", "cacheforge");

        getCalls = Counter.builder("cacheforge.cache.calls").tags(base.and("op", "get")).register(registry);
        getHits = Counter.builder("cacheforge.cache.hits").tags(base.and("op", "get")).register(registry);
        getMisses = Counter.builder("cacheforge.cache.misses").tags(base.and("op", "get")).register(registry);
        getExpired = Counter.builder("cacheforge.cache.expired").tags(base.and("op", "get")).register(registry);

        setCalls = Counter.builder("cacheforge.cache.calls").tags(base.and("op", "set")).register(registry);
        setCreated = Counter.builder("cacheforge.cache.set.created").tags(base).register(registry);
        setUpdated = Counter.builder("cacheforge.cache.set.updated").tags(base).register(registry);

        delCalls = Counter.builder("cacheforge.cache.calls").tags(base.and("op", "del")).register(registry);
        delHits = Counter.builder("cacheforge.cache.hits").tags(base.and("op", "del")).register(registry);
        delMisses = Counter.builder("cacheforge.cache.misses").tags(base.and("op", "del")).register(registry);
        delExpired = Counter.builder("cacheforge.cache.expired").tags(base.and("op", "del")).register(registry);

        getTimer = Timer.builder("cacheforge.cache.latency")
                .tags(base.and("op", "get"))
                .publishPercentileHistogram()
                .register(registry);

        setTimer = Timer.builder("cacheforge.cache.latency")
                .tags(base.and("op", "set"))
                .publishPercentileHistogram()
                .register(registry);

        delTimer = Timer.builder("cacheforge.cache.latency")
                .tags(base.and("op", "del"))
                .publishPercentileHistogram()
                .register(registry);

        Gauge.builder("cacheforge.cache.latency.last", lastGetNs, AtomicLong::get)
                .baseUnit("nanoseconds")
                .tags(base.and("op", "get"))
                .register(registry);

        Gauge.builder("cacheforge.cache.latency.last", lastSetNs, AtomicLong::get)
                .baseUnit("nanoseconds")
                .tags(base.and("op", "set"))
                .register(registry);

        Gauge.builder("cacheforge.cache.latency.last", lastDelNs, AtomicLong::get)
                .baseUnit("nanoseconds")
                .tags(base.and("op", "del"))
                .register(registry);
    }

    @Override
    public CacheResult getKey(CacheKey key) {
        long start = System.nanoTime();

        getCalls.increment();
        CacheResult r = delegate.getKey(key);

        long dur = System.nanoTime() - start;

        if (r instanceof CacheResult.Hit)
            getHits.increment();
        else if (r instanceof CacheResult.Expired)
            getExpired.increment();
        else
            getMisses.increment();

        lastGetNs.set(dur); // most recent
        getTimer.record(dur, TimeUnit.NANOSECONDS); // avg/p95/p99

        return r;
    }

    @Override
    public CacheResult setKey(CacheKey key, String val, long ttlInSecs) {
        long start = System.nanoTime();

        setCalls.increment();
        CacheResult r = delegate.setKey(key, val, ttlInSecs);

        long dur = System.nanoTime() - start;

        if (r instanceof CacheResult.Created)
            setCreated.increment();
        else if (r instanceof CacheResult.Updated)
            setUpdated.increment();

        lastSetNs.set(dur); // most recent
        setTimer.record(dur, TimeUnit.NANOSECONDS); // avg/p95/p99

        return r;
    }

    @Override
    public CacheResult deleteKey(CacheKey key) {
        long start = System.nanoTime();

        delCalls.increment();
        CacheResult r = delegate.deleteKey(key);

        long dur = System.nanoTime() - start;

        if (r instanceof CacheResult.Hit)
            delHits.increment();
        else if (r instanceof CacheResult.Expired)
            delExpired.increment();
        else
            delMisses.increment();

        lastDelNs.set(dur); // most recent
        delTimer.record(dur, TimeUnit.NANOSECONDS); // avg/p95/p99

        return r;
    }

    @Override
    public void evictKeys(List<CacheKey> keys, CacheStripe stripe) {
        delegate.evictKeys(keys, stripe);
    }

    @Override
    public HashMap<Integer, List<CacheKey>> snapshotKeys() {
        return delegate.snapshotKeys();
    }

    @Override
    public HashMap<Integer, List<CacheKey>> snapshotLRU() {
        return delegate.snapshotLRU();
    }

    @Override
    public CacheEntry getCacheEntryDirectlyFromStore(CacheKey key) {
        long start = System.nanoTime();

        CacheEntry entry = delegate.getCacheEntryDirectlyFromStore(key, getExpired);

        long dur = System.nanoTime() - start;

        lastGetNs.set(dur); // most recent
        getTimer.record(dur, TimeUnit.NANOSECONDS); // avg/p95/p99

        return entry;
    }

    @Override
    public int cacheCapacity() {
        return delegate.cacheCapacity();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    private String generateRandomChar() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        Random random = new Random();

        return alphabet.charAt(random.nextInt(alphabet.length())) + "";
    }

    private String generateRandomNamespace() {

        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        Random random = new Random();

        char aplha = alphabet.charAt(random.nextInt(alphabet.length()));

        int numeric = (int) (Math.random() * 20);

        return aplha + "" + numeric;
    }

    public void populateByDefault() {

        // k_v_x -> means key and value with serial no x
        // v_x -> means value with serial no x
        // ck_x -> cache key with serial no x;

        String k_v_1 = this.generateRandomChar();
        String k_v_2 = this.generateRandomChar();
        String k_v_3 = this.generateRandomChar();
        String k_v_4 = this.generateRandomChar();
        String k_v_5 = this.generateRandomChar();
        String k_v_6 = this.generateRandomChar();
        String k_v_7 = this.generateRandomChar();
        String k_v_8 = this.generateRandomChar();
        String k_v_9 = this.generateRandomChar();
        String k_v_10 = this.generateRandomChar();
        String k_v_11 = this.generateRandomChar();
        String k_v_12 = this.generateRandomChar();
        String k_v_13 = this.generateRandomChar();
        String k_v_14 = this.generateRandomChar();
        String v_10_updated = this.generateRandomChar();
        String k_v_15 = this.generateRandomChar();
        String k_v_16 = this.generateRandomChar();
        String k_v_17 = this.generateRandomChar();
        String k_v_18 = this.generateRandomChar();
        String k_v_19 = this.generateRandomChar();
        String k_v_20 = this.generateRandomChar();

        CacheKey ck_1 = new CacheKey(generateRandomNamespace(), k_v_1);
        CacheKey ck_2 = new CacheKey(generateRandomNamespace(), k_v_2);
        CacheKey ck_3 = new CacheKey(generateRandomNamespace(), k_v_3);
        CacheKey ck_4 = new CacheKey(generateRandomNamespace(), k_v_4);
        CacheKey ck_5 = new CacheKey(generateRandomNamespace(), k_v_5);
        CacheKey ck_6 = new CacheKey(generateRandomNamespace(), k_v_6);
        CacheKey ck_7 = new CacheKey(generateRandomNamespace(), k_v_7);
        CacheKey ck_8 = new CacheKey(generateRandomNamespace(), k_v_8);
        CacheKey ck_9 = new CacheKey(generateRandomNamespace(), k_v_9);
        CacheKey ck_10 = new CacheKey(generateRandomNamespace(), k_v_10);
        CacheKey ck_11 = new CacheKey(generateRandomNamespace(), k_v_11);
        CacheKey ck_12 = new CacheKey(generateRandomNamespace(), k_v_12);
        CacheKey ck_13 = new CacheKey(generateRandomNamespace(), k_v_13);
        CacheKey ck_14 = new CacheKey(generateRandomNamespace(), k_v_14);
        CacheKey ck_15 = new CacheKey(generateRandomNamespace(), k_v_15);
        CacheKey ck_16_non_existing = new CacheKey(generateRandomNamespace(), k_v_16);
        CacheKey ck_17_non_existing = new CacheKey(generateRandomNamespace(), k_v_17);
        CacheKey ck_18 = new CacheKey(generateRandomNamespace(), k_v_18);
        CacheKey ck_19 = new CacheKey(generateRandomNamespace(), k_v_19);
        CacheKey ck_20 = new CacheKey(generateRandomNamespace(), k_v_20);

        this.setKey(ck_1, k_v_1, -1);
        this.setKey(ck_2, k_v_2, -1);
        this.setKey(ck_3, k_v_3, -1);
        this.setKey(ck_4, k_v_4, -1);
        this.setKey(ck_5, k_v_5, -1);
        this.setKey(ck_6, k_v_6, -1);
        this.setKey(ck_7, k_v_7, -1);
        this.setKey(ck_8, k_v_8, -1);
        this.setKey(ck_9, k_v_9, -1);
        this.setKey(ck_10, k_v_10, -1);
        this.setKey(ck_11, k_v_11, -1);
        this.setKey(ck_12, k_v_12, -1);
        this.setKey(ck_13, k_v_13, -1);
        this.setKey(ck_14, k_v_14, -1);
        this.setKey(ck_15, k_v_15, 2);
        this.setKey(ck_18, k_v_18, -1);
        this.setKey(ck_19, k_v_19, -1);
        this.setKey(ck_20, k_v_20, -1);

        this.setKey(ck_10, v_10_updated, -1);

        this.getKey(ck_11);
        this.getKey(ck_13);
        this.getKey(ck_5);
        this.getKey(ck_4);
        this.getKey(ck_16_non_existing);
        this.getKey(ck_17_non_existing);
    }

}
