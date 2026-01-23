package com.saoodahmad.cacheforge.api.dtos;

import java.util.List;
import java.util.Map;

public class CacheStateResponse {

    public Map<Integer, List<CacheEntryDto>> keys;
    public Map<Integer, List<LRUEntryDto>> lru;
    public int capacity;

}
