package com.saoodahmad.cacheforge.common_utils;

public final class TestUtils {
    private TestUtils() {
    }

    public static void sleepMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Test interrupted", e);
        }
    }

}
