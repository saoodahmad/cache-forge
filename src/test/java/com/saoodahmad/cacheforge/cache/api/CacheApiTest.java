package com.saoodahmad.cacheforge.cache.api;

import com.saoodahmad.cacheforge.cache.engine.DefaultCacheEngine;
import com.saoodahmad.cacheforge.cache.store.InMemoryCacheStore;
import com.saoodahmad.cacheforge.cache.time.FakeTimeProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CacheApiTest {
    @Test
    void ttlExpiry_shouldMissAndCleanupStoreAndLru() {

        FakeTimeProvider time = new FakeTimeProvider(0);

        CacheApi cacheApi =  new CacheApi(new DefaultCacheEngine(new InMemoryCacheStore(), time));

        CacheOperationOutput setOut = cacheApi.setKey("X", "X", 1);
        assertFalse(setOut.hit);
        assertTrue(setOut.miss);
        assertNotNull(setOut.data);

        time.advanceMs(1100);

        CacheOperationOutput getOut = cacheApi.getKey("X");
        assertFalse(getOut.hit);
        assertTrue(getOut.miss);
        assertNull(getOut.data);

        // Requires snapshot helpers (recommended)
        assertFalse(cacheApi.snapshotKeys().contains("X"), "Expired key must be removed from store");
        assertFalse(cacheApi.snapshotLRU().contains("X"), "Expired key must be removed from LRU");
    }

    @Test
    void ttlRefreshOnOverwrite_shouldExtendLifetime_andReportHitOnOverwrite() {
        FakeTimeProvider time = new FakeTimeProvider(0);

        CacheApi cacheApi =  new CacheApi(new DefaultCacheEngine(new InMemoryCacheStore(), time));

        CacheOperationOutput s1 = cacheApi.setKey("X", "X", 2);
        assertFalse(s1.hit);
        assertTrue(s1.miss);
        assertEquals("X", s1.data.getVal());

        cacheApi.setKey("Y", "Y", -1);
        cacheApi.setKey("Z", "Z", -1);

        time.advanceMs(1100);

        CacheOperationOutput s2 = cacheApi.setKey("X", "X2", 2);
        assertTrue(s2.hit, "Overwrite before expiry should be a hit");
        assertFalse(s2.miss);
        assertEquals("X2", s2.data.getVal());

        time.advanceMs(1500);

        CacheOperationOutput g1 = cacheApi.getKey("X");
        assertTrue(g1.hit);
        assertEquals("X2", g1.data.getVal());

        time.advanceMs(1100);

        CacheOperationOutput g2 = cacheApi.getKey("X");
        assertFalse(g2.hit);
        assertTrue(g2.miss);
        assertNull(g2.data);

        assertFalse(cacheApi.snapshotKeys().contains("X"));
        assertFalse(cacheApi.snapshotLRU().contains("X"));
    }

    @Test
    void lruEviction_shouldEvictLeastRecentlyUsed_atCapacity5() {
        FakeTimeProvider time = new FakeTimeProvider(0);

        CacheApi cacheApi =  new CacheApi(new DefaultCacheEngine(new InMemoryCacheStore(), time));

        cacheApi.setKey("A", "A", -1);
        cacheApi.setKey("B", "B", -1);
        cacheApi.setKey("C", "C", -1);
        cacheApi.setKey("D", "D", -1);
        cacheApi.setKey("E", "E", -1);

        cacheApi.getKey("C");

        assertEquals(List.of("A", "B", "D", "E", "C"), cacheApi.snapshotLRU());

        cacheApi.setKey("A", "A", -1);

        assertEquals(List.of("B", "D", "E", "C", "A"), cacheApi.snapshotLRU());

        cacheApi.setKey("F", "F", -1);

        Set<String> keys = cacheApi.snapshotKeys();
        assertFalse(keys.contains("B"), "B should be evicted (LRU)");
        assertTrue(keys.containsAll(Set.of("A", "C", "D", "E", "F")));

        assertEquals(List.of("D", "E", "C", "A", "F"), cacheApi.snapshotLRU());
    }

    @Test
    void delete_shouldReturnDeletedEntry_orNullIfMissingOrExpired() {
        FakeTimeProvider time = new FakeTimeProvider(0);

        CacheApi cacheApi =  new CacheApi(new DefaultCacheEngine(new InMemoryCacheStore(), time));

        cacheApi.setKey("K", "K", -1);
        CacheOperationOutput del1 = cacheApi.deleteKey("K");
        assertTrue(del1.hit);
        assertFalse(del1.miss);
        assertNotNull(del1.data);
        assertEquals("K", del1.data.getVal());

        CacheOperationOutput del2 = cacheApi.deleteKey("K");
        assertFalse(del2.hit);
        assertTrue(del2.miss);
        assertNull(del2.data);

        cacheApi.setKey("T", "T", 1);
        time.advanceMs(1100);

        CacheOperationOutput del3 = cacheApi.deleteKey("T");
        assertFalse(del3.hit);
        assertTrue(del3.miss);
        assertNull(del3.data);
    }
}
