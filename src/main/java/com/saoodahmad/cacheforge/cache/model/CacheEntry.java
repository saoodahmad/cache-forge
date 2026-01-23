package com.saoodahmad.cacheforge.cache.model;

public class CacheEntry {

    private String val;

    /*
     * -1 no expiry
     * >0 expiry in seconds
     */

    private long ttlInSecs;

    private long expiresAt;

    // used when key is expired but is not evicted
    private boolean expiryCounted;

    public CacheEntry(String val, long ttlInSecs, long nowNs) {
        this.val = val;
        this.ttlInSecs = ttlInSecs;

        if (ttlInSecs == -1) {
            this.expiresAt = -1;
        } else {
            long ttlMs = Math.multiplyExact(ttlInSecs, 1000000000L);
            this.expiresAt = Math.addExact(nowNs, ttlMs);
        }

        this.expiryCounted = false;
    }

    public String getVal() {
        return this.val;
    }

    public void updateVal(String newVal) {
        this.val = newVal;
    }

    public long getTtlInSecs() {
        return this.ttlInSecs;
    }

    public void setTtlSeconds(long ttlInSecs, long nowNs) {
        this.ttlInSecs = ttlInSecs;
        if (ttlInSecs == -1) {
            this.expiresAt = -1;
        } else {
            long ttlNs = Math.multiplyExact(ttlInSecs, 1000000000L);
            this.expiresAt = Math.addExact(nowNs, ttlNs);
        }
    }

    public boolean isKeyExpired(long nowNs) {
        if (this.expiresAt == -1) {
            return false;
        }

        return nowNs >= this.expiresAt;
    }

    public void markExpiryCounted() {
        this.expiryCounted = true;
    }

    public boolean expiryCounted() {
        return expiryCounted;
    }

    public String toString() {
        return "value: " + this.val + ", ttl in secs: " + this.ttlInSecs;
    }
}
