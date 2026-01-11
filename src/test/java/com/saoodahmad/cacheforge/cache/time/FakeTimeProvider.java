package com.saoodahmad.cacheforge.cache.time;

public class FakeTimeProvider implements TimeProvider {
    private long now;

    public FakeTimeProvider(long startMs) { this.now = startMs; }

    @Override public long nowMs() { return now; }

    public void advanceMs(long ms) { now += ms; }
}
