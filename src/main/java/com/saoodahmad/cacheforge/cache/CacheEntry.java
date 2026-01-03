package com.saoodahmad.cacheforge.cache;

public class CacheEntry{

    private String val;

    /*
     * -1 no expiry
     * >0 expiry in seconds
     */

    private long ttlInSecs;

    private long expiresAt;


    CacheEntry (String val, long ttlInSecs) {
        this.val = val;
        this.ttlInSecs = ttlInSecs;

        if(ttlInSecs == -1) {
            this.expiresAt = -1;
        }else {
            this.expiresAt = System.currentTimeMillis() + ttlInSecs * 1000;
        }
    }


    public String getVal () {
        return this.val;
    }

    public void updateVal(String newVal) {
        this.val = newVal;
    }

    public long getExpiry () {
        return this.ttlInSecs;
    }

    public void updateExpiry(long expiryInSec) {
        this.ttlInSecs = expiryInSec;
        if(expiryInSec == -1) {
            this.expiresAt = -1;
        }else {
            this.expiresAt = System.currentTimeMillis() + expiryInSec * 1000;
        }
    }

    public boolean isKeyExpired() {
        if (this.expiresAt == -1) {
            return false;
        }

        return System.currentTimeMillis() >= this.expiresAt;
    }

    public String toString() {
        return "value: " + this.val + ", ttl in secs: " + this.ttlInSecs;
    }
}
