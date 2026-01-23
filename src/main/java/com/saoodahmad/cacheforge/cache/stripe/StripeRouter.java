package com.saoodahmad.cacheforge.cache.stripe;

import com.saoodahmad.cacheforge.cache.model.CacheKey;

public class StripeRouter {

    int stripeCount;

    public StripeRouter(int stripeCount) {
        this.stripeCount = stripeCount;
    }

    public int stripeIndex(CacheKey cKey) {
        int h = cKey.hashCode();

        // avoid negative modulo issues
        int idx = (h & 0x7fffffff) % this.stripeCount;

        return idx;
    }
}
