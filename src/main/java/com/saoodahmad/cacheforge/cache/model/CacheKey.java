package com.saoodahmad.cacheforge.cache.model;

import java.util.Objects;

public class CacheKey {

    private final String namespace;

    private final String key;

    public CacheKey(String namespace, String key) {
        this.namespace = namespace;
        this.key = key;
    }


    public String getKey() {
        return this.key;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String fullKey() {
        return this.namespace + ":" + this.key;
    }

    @Override
    public String toString() {
        return "Namespace: " + this.namespace + " key: " + this.key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheKey that = (CacheKey) o;

        return Objects.equals(this.namespace, that.namespace)
                && Objects.equals(this.key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.namespace, this.key);
    }
}
