package com.saoodahmad.cacheforge.cache.model;

public class CacheEntry{

    private String val;

    /*
     * -1 no expiry
     * >0 expiry in seconds
     */

    private long ttlInSecs;

    private long expiresAt;


    public CacheEntry (String val, long ttlInSecs, long nowMs) {
        this.val = val;
        this.ttlInSecs = ttlInSecs;

        if(ttlInSecs == -1) {
            this.expiresAt = -1;
        }else {
            long ttlMs = Math.multiplyExact(ttlInSecs, 1000L);
            this.expiresAt = Math.addExact(nowMs, ttlMs);
        }
    }


    public String getVal () {
        return this.val;
    }

    public void updateVal(String newVal) {
        this.val = newVal;
    }

    public long getTtlInSecs () {
        return this.ttlInSecs;
    }

    public void setTtlSeconds(long ttlInSecs, long nowMs) {
        this.ttlInSecs = ttlInSecs;
        if(ttlInSecs == -1) {
            this.expiresAt = -1;
        }else {
            long ttlMs = Math.multiplyExact(ttlInSecs, 1000L);
            this.expiresAt = Math.addExact(nowMs, ttlMs);
        }
    }

    public boolean isKeyExpired(long nowMs) {
        if (this.expiresAt == -1) {
            return false;
        }

        return nowMs >= this.expiresAt;
    }

    public String toString() {
        return "value: " + this.val + ", ttl in secs: " + this.ttlInSecs;
    }
}
