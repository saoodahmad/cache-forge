package com.saoodahmad.cacheforge.api.dtos;

import java.util.List;

public class CacheStateResponse {

    public List<CacheEntryDto> keys;
    public List<String> lru;
    public int capacity;

}
