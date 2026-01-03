package com.saoodahmad.cacheforge.cache;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static com.saoodahmad.cacheforge.common_utils.TestUtils.*;

class CacheTest {
    @Test
    void ttlExpiry_shouldMissAndCleanupStoreAndLru() {
        Cache cache = new Cache();

        CacheOperationOutput setOut = cache.setKey("X", "X", 1);
        assertFalse(setOut.hit);
        assertTrue(setOut.miss);
        assertNotNull(setOut.data);

        sleepMs(1100);

        CacheOperationOutput getOut = cache.getKey("X");
        assertFalse(getOut.hit);
        assertTrue(getOut.miss);
        assertNull(getOut.data);

        // Requires snapshot helpers (recommended)
        assertFalse(cache.snapshotKeys().contains("X"), "Expired key must be removed from store");
        assertFalse(cache.snapshotLRU().contains("X"), "Expired key must be removed from LRU");
    }

    @Test
    void ttlRefreshOnOverwrite_shouldExtendLifetime_andReportHitOnOverwrite() {
        Cache cache = new Cache();

        CacheOperationOutput s1 = cache.setKey("X", "X", 2);
        assertFalse(s1.hit);
        assertTrue(s1.miss);
        assertEquals("X", s1.data.getVal());

        cache.setKey("Y", "Y", -1);
        cache.setKey("Z", "Z", -1);

        sleepMs(1100);

        CacheOperationOutput s2 = cache.setKey("X", "X2", 2);
        assertTrue(s2.hit, "Overwrite before expiry should be a hit");
        assertFalse(s2.miss);
        assertEquals("X2", s2.data.getVal());

        sleepMs(1500);

        CacheOperationOutput g1 = cache.getKey("X");
        assertTrue(g1.hit);
        assertEquals("X2", g1.data.getVal());

        sleepMs(1100);

        CacheOperationOutput g2 = cache.getKey("X");
        assertFalse(g2.hit);
        assertTrue(g2.miss);
        assertNull(g2.data);

        assertFalse(cache.snapshotKeys().contains("X"));
        assertFalse(cache.snapshotLRU().contains("X"));
    }

    @Test
    void lruEviction_shouldEvictLeastRecentlyUsed_atCapacity5() {
        Cache cache = new Cache();

        cache.setKey("A", "A", -1);
        cache.setKey("B", "B", -1);
        cache.setKey("C", "C", -1);
        cache.setKey("D", "D", -1);
        cache.setKey("E", "E", -1);

        cache.getKey("C");

        assertEquals(List.of("A", "B", "D", "E", "C"), cache.snapshotLRU());

        cache.setKey("A", "A", -1);

        assertEquals(List.of("B", "D", "E", "C", "A"), cache.snapshotLRU());

        cache.setKey("F", "F", -1);

        Set<String> keys = cache.snapshotKeys();
        assertFalse(keys.contains("B"), "B should be evicted (LRU)");
        assertTrue(keys.containsAll(Set.of("A", "C", "D", "E", "F")));

        assertEquals(List.of("D", "E", "C", "A", "F"), cache.snapshotLRU());
    }

    @Test
    void delete_shouldReturnDeletedEntry_orNullIfMissingOrExpired() {
        Cache cache = new Cache();

        cache.setKey("K", "K", -1);
        CacheOperationOutput del1 = cache.deleteKey("K");
        assertTrue(del1.hit);
        assertFalse(del1.miss);
        assertNotNull(del1.data);
        assertEquals("K", del1.data.getVal());

        CacheOperationOutput del2 = cache.deleteKey("K");
        assertFalse(del2.hit);
        assertTrue(del2.miss);
        assertNull(del2.data);

        cache.setKey("T", "T", 1);
        sleepMs(1100);

        CacheOperationOutput del3 = cache.deleteKey("T");
        assertFalse(del3.hit);
        assertTrue(del3.miss);
        assertNull(del3.data);
    }
}
