package com.saoodahmad.cacheforge.cache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cacheforge")
public class CacheForgeProperties {

    private int stripes = 2; // value remains 2 if it is not overridden in properties file
    private int capacity = 4; // value remains 4 if it is not overridden in properties file

    public int getStripes() {
        return stripes;
    }

    public void setStripes(int stripes) {
        this.stripes = stripes;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
