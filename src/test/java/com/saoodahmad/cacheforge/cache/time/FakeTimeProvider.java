package com.saoodahmad.cacheforge.cache.time;

public class FakeTimeProvider implements TimeProvider {
    private long now;

    public FakeTimeProvider(long startNs) {
        this.now = startNs;
    }

    @Override
    public long nowNs() {
        return this.now;
    }

    public void advanceNs(long ns) {
        now += ns;
    }
}
