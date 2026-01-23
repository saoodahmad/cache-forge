package com.saoodahmad.cacheforge.cache.model;

public sealed interface CacheResult
        permits CacheResult.Hit,
        CacheResult.Miss,
        CacheResult.Expired,
        CacheResult.Created,
        CacheResult.Updated {

    record Hit(CacheEntry entry) implements CacheResult {}       // GET / DELETE
    record Miss() implements CacheResult {}                      // GET / DELETE
    record Expired() implements CacheResult {}                   // GET / DELETE

    record Created(CacheEntry entry) implements CacheResult {}   // SET (new)
    record Updated(CacheEntry entry) implements CacheResult {}   // SET (overwrite)
}
