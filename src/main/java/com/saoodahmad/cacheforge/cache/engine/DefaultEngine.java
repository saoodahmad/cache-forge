package com.saoodahmad.cacheforge.cache.engine;

import com.saoodahmad.cacheforge.cache.model.CacheEntry;
import com.saoodahmad.cacheforge.cache.model.CacheKey;
import io.micrometer.core.instrument.Counter;

public interface DefaultEngine extends CommonEngine {
    CacheEntry getCacheEntryDirectlyFromStore(CacheKey key, Counter expiredCounter);
}
