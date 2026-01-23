package com.saoodahmad.cacheforge.cache.api;

import com.saoodahmad.cacheforge.cache.config.CacheForgeProperties;
import com.saoodahmad.cacheforge.cache.engine.MetricsCacheEngine;
import com.saoodahmad.cacheforge.cache.engine.MetricsEngine;
import com.saoodahmad.cacheforge.cache.engine.StripedCacheEngine;
import com.saoodahmad.cacheforge.cache.model.CacheKey;

import com.saoodahmad.cacheforge.cache.stripe.CacheStripe;
import com.saoodahmad.cacheforge.cache.time.FakeTimeProvider;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CacheApiTest {
    @Test
    void ttlExpiry_shouldMissAndCleanupStoreAndLru() {

        FakeTimeProvider time = new FakeTimeProvider(0);

        StripedCacheEngine engine = new StripedCacheEngine(new CacheForgeProperties(), time);

        MetricsEngine metricEngine = new MetricsCacheEngine(engine, new CompositeMeterRegistry());

        CacheApi cacheApi = new CacheApi(metricEngine);

        CacheKey cKeyX = new CacheKey("N1", "X");

        CacheOperationOutput setOut = cacheApi.setKey(cKeyX.getNamespace(), cKeyX.getKey(), "X", 1);
        assertFalse(setOut.hit);
        assertTrue(setOut.miss);
        assertNotNull(setOut.data);

        time.advanceNs(1100 * 1000000);

        CacheOperationOutput getOut = cacheApi.getKey(cKeyX.getNamespace(), cKeyX.getKey());
        assertFalse(getOut.hit);
        assertTrue(getOut.miss);
        assertNull(getOut.data);

        assertFalse(cacheApi.snapshotKeys().get(1).contains(cKeyX), "Expired key must be removed from store");
        assertFalse(cacheApi.snapshotLRU().get(1).contains(cKeyX), "Expired key must be removed from LRU");
    }

    @Test
    void ttlRefreshOnOverwrite_shouldExtendLifetime_andReportHitOnOverwrite() {
        FakeTimeProvider time = new FakeTimeProvider(0);

        StripedCacheEngine engine = new StripedCacheEngine(new CacheForgeProperties(), time);

        MetricsEngine metricEngine = new MetricsCacheEngine(engine, new CompositeMeterRegistry());

        CacheApi cacheApi = new CacheApi(metricEngine);

        CacheKey cKeyX = new CacheKey("N1", "X");

        CacheOperationOutput s1 = cacheApi.setKey(cKeyX.getNamespace(), cKeyX.getKey(), "X", 5);

        assertFalse(s1.hit);
        assertTrue(s1.miss);
        assertEquals("X", s1.data.getVal());

        CacheKey cKeyY = new CacheKey("N1", "Y");
        CacheKey cKeyZ = new CacheKey("N1", "Z");

        cacheApi.setKey(cKeyY.getNamespace(), cKeyY.getKey(), "Y", -1);
        cacheApi.setKey(cKeyZ.getNamespace(), cKeyZ.getKey(), "Z", -1);

        time.advanceNs(1100 * 1000000);

        CacheOperationOutput s2 = cacheApi.setKey(cKeyX.getNamespace(), cKeyX.getKey(), "X2", 2);

        assertTrue(s2.hit, "Overwrite before expiry should be a hit");
        assertFalse(s2.miss);
        assertEquals("X2", s2.data.getVal());

        time.advanceNs(1500 * 1000000);

        CacheOperationOutput g1 = cacheApi.getKey(cKeyX.getNamespace(), cKeyX.getKey());
        assertTrue(g1.hit);
        assertEquals("X2", g1.data.getVal());

        time.advanceNs(1100 * 1000000);

        CacheOperationOutput g2 = cacheApi.getKey(cKeyX.getNamespace(), cKeyX.getKey());
        assertFalse(g2.hit);
        assertTrue(g2.miss);
        assertNull(g2.data);

        assertFalse(cacheApi.snapshotKeys().get(1).contains(cKeyX));
        assertFalse(cacheApi.snapshotLRU().get(1).contains(cKeyX));
    }

    @Test
    void lruEviction_shouldEvictLeastRecentlyUsed_atCapacity5() {
        FakeTimeProvider time = new FakeTimeProvider(0);

        StripedCacheEngine engine = new StripedCacheEngine(new CacheForgeProperties(), time);

        MetricsEngine metricEngine = new MetricsCacheEngine(engine, new CompositeMeterRegistry());

        CacheApi cacheApi = new CacheApi(metricEngine);

        CacheKey cKeyA = new CacheKey("N1", "A");
        CacheKey cKeyB = new CacheKey("N1", "B");
        CacheKey cKeyC = new CacheKey("N1", "C");
        CacheKey cKeyD = new CacheKey("N1", "D");
        CacheKey cKeyE = new CacheKey("N1", "E");

        CacheStripe ckeyAStripe = engine.stripeFor(cKeyA);
        CacheStripe ckeyBStripe = engine.stripeFor(cKeyB);
        CacheStripe ckeyCStripe = engine.stripeFor(cKeyC);
        CacheStripe ckeyDStripe = engine.stripeFor(cKeyD);
        CacheStripe ckeyEStripe = engine.stripeFor(cKeyE);

        assertEquals(ckeyAStripe, ckeyCStripe);
        assertEquals(ckeyCStripe, ckeyEStripe);
        assertEquals(ckeyBStripe, ckeyDStripe);

        cacheApi.setKey(cKeyA.getNamespace(), cKeyA.getKey(), "A", -1);
        cacheApi.setKey(cKeyB.getNamespace(), cKeyB.getKey(), "B", -1);
        cacheApi.setKey(cKeyC.getNamespace(), cKeyC.getKey(), "C", -1);
        cacheApi.setKey(cKeyD.getNamespace(), cKeyD.getKey(), "D", -1);

        cacheApi.setKey(cKeyE.getNamespace(), cKeyE.getKey(), "E", -1);

        cacheApi.getKey(cKeyC.getNamespace(), cKeyC.getKey());

        Map<Integer, List<CacheKey>> lruKeys = cacheApi.snapshotLRU();
        assertEquals(List.of(cKeyB, cKeyD), lruKeys.get(0));
        assertEquals(List.of(cKeyE, cKeyC), lruKeys.get(1));
        assertFalse(lruKeys.get(1).contains(cKeyA));

        Map<Integer, List<CacheKey>> storeKeys = cacheApi.snapshotKeys();
        assertEquals(List.of(cKeyB, cKeyD), storeKeys.get(0));
        assertEquals(List.of(cKeyC, cKeyE), storeKeys.get(1));
        assertFalse(storeKeys.get(1).contains(cKeyA));
    }

    @Test
    void delete_shouldReturnDeletedEntry_orNullIfMissingOrExpired() {
        FakeTimeProvider time = new FakeTimeProvider(0);

        StripedCacheEngine engine = new StripedCacheEngine(new CacheForgeProperties(), time);

        MetricsEngine metricEngine = new MetricsCacheEngine(engine, new CompositeMeterRegistry());

        CacheApi cacheApi = new CacheApi(metricEngine);

        CacheKey cKeyK = new CacheKey("N1", "K");

        cacheApi.setKey(cKeyK.getNamespace(), cKeyK.getKey(), "K", -1);
        CacheOperationOutput del1 = cacheApi.deleteKey(cKeyK.getNamespace(), cKeyK.getKey());
        assertTrue(del1.hit);
        assertFalse(del1.miss);
        assertNotNull(del1.data);
        assertEquals("K", del1.data.getVal());

        CacheOperationOutput del2 = cacheApi.deleteKey(cKeyK.getNamespace(), cKeyK.getKey());
        assertFalse(del2.hit);
        assertTrue(del2.miss);
        assertNull(del2.data);

        CacheKey cKeyT = new CacheKey("N1", "T");

        cacheApi.setKey(cKeyT.getNamespace(), cKeyT.getKey(), "T", 1);
        time.advanceNs(1100 * 1000000);

        CacheOperationOutput del3 = cacheApi.deleteKey(cKeyT.getNamespace(), cKeyT.getKey());
        assertFalse(del3.hit);
        assertTrue(del3.miss);
        assertNull(del3.data);
    }

}
