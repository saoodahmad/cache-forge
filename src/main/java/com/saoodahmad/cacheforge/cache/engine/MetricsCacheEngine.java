package com.saoodahmad.cacheforge.cache.engine;

import com.saoodahmad.cacheforge.cache.model.CacheEntry;
import com.saoodahmad.cacheforge.cache.model.CacheResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Primary
@Component
public class MetricsCacheEngine implements CacheEngine {

    private final CacheEngine delegate;

    private final Counter getCalls, getHits, getMisses, getExpired;
    private final Counter setCalls, setCreated, setUpdated;
    private final Counter delCalls, delHits, delMisses, delExpired;

    private final Timer getTimer, setTimer, delTimer;

    // gauges can be registered against *this* (delegate snapshots)
    public MetricsCacheEngine(DefaultCacheEngine delegate, MeterRegistry registry) {
        this.delegate = delegate;

        Tags base = Tags.of("cache", "cacheforge");

        getCalls   = Counter.builder("cacheforge.cache.calls").tags(base.and("op","get")).register(registry);
        getHits    = Counter.builder("cacheforge.cache.hits").tags(base.and("op","get")).register(registry);
        getMisses  = Counter.builder("cacheforge.cache.misses").tags(base.and("op","get")).register(registry);
        getExpired = Counter.builder("cacheforge.cache.expired").tags(base.and("op","get")).register(registry);

        setCalls   = Counter.builder("cacheforge.cache.calls").tags(base.and("op","set")).register(registry);
        setCreated = Counter.builder("cacheforge.cache.set.created").tags(base).register(registry);
        setUpdated = Counter.builder("cacheforge.cache.set.updated").tags(base).register(registry);

        delCalls   = Counter.builder("cacheforge.cache.calls").tags(base.and("op","del")).register(registry);
        delHits    = Counter.builder("cacheforge.cache.hits").tags(base.and("op","del")).register(registry);
        delMisses  = Counter.builder("cacheforge.cache.misses").tags(base.and("op","del")).register(registry);
        delExpired = Counter.builder("cacheforge.cache.expired").tags(base.and("op","del")).register(registry);

        // useful gauges with near-zero clutter
        registry.gauge("cacheforge.cache.capacity", base, this, e -> (double) e.cacheCapacity());
        registry.gauge("cacheforge.cache.store.keys", base, this, e -> (double) e.snapshotKeys().size());
        registry.gauge("cacheforge.cache.lru.size", base, this, e -> (double) e.snapshotLRU().size());

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
    }

    @Override
    public CacheResult getKey(String key) {
        getCalls.increment();
        CacheResult r = getTimer.record(() -> delegate.getKey(key));

        if (r instanceof CacheResult.Hit) getHits.increment();
        else if (r instanceof CacheResult.Expired) getExpired.increment();
        else getMisses.increment();

        return r;
    }

    @Override
    public CacheResult setKey(String key, String val, long ttlInSecs) {
        setCalls.increment();
        CacheResult r = setTimer.record(() -> delegate.setKey(key, val, ttlInSecs));

        if (r instanceof CacheResult.Created) setCreated.increment();
        else if (r instanceof CacheResult.Updated) setUpdated.increment();

        return r;
    }

    @Override
    public CacheResult deleteKey(String key) {
        delCalls.increment();
        CacheResult r = delTimer.record(() -> delegate.deleteKey(key));

        if (r instanceof CacheResult.Hit) delHits.increment();
        else if (r instanceof CacheResult.Expired) delExpired.increment();
        else delMisses.increment();

        return r;
    }

    @Override public void evictKeys(List<String> keys) { delegate.evictKeys(keys); }
    @Override public Set<String> snapshotKeys() { return new HashSet<>(delegate.snapshotKeys()); }
    @Override public List<String> snapshotLRU() { return delegate.snapshotLRU(); }
    @Override public CacheEntry getKeyDirectlyFromStore(String key) { return delegate.getKeyDirectlyFromStore(key); }
    @Override public int cacheCapacity() { return delegate.cacheCapacity(); }

}
