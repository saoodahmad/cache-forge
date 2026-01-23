package com.saoodahmad.cacheforge.cache;

import com.saoodahmad.cacheforge.cache.engine.MetricsEngine;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CachePurgeJob {
    private final MetricsEngine engine;

    public CachePurgeJob(MetricsEngine engine) {
        this.engine = engine;
    }

    @Scheduled(cron = "${cacheforge.purge.cron}", zone = "Asia/Kolkata")
    public void purge() {
        engine.clear();
        engine.populateByDefault();
    }

    @PostConstruct()
    public void populate() {
        engine.populateByDefault();
    }
}
