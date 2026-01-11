package com.saoodahmad.cacheforge.cache.time;

import org.springframework.stereotype.Component;

@Component
public class SystemTimeProvider implements TimeProvider {
    @Override
    public long nowMs() {
        return System.currentTimeMillis();
    }
}
