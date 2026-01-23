package com.saoodahmad.cacheforge.cache.engine;

import com.saoodahmad.cacheforge.cache.model.CacheEntry;
import com.saoodahmad.cacheforge.cache.model.CacheKey;

public interface MetricsEngine extends CommonEngine {
    CacheEntry getCacheEntryDirectlyFromStore(CacheKey key);

    void populateByDefault();
}
