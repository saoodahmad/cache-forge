package com.saoodahmad.cacheforge.cache;


public class CacheOperationOutput {

   public OperationType opType;

    public boolean hit;

    public boolean miss;

    public CacheEntry data;

    CacheOperationOutput(OperationType opType, boolean hit, boolean miss, CacheEntry data) {
        this.opType = opType;
        this.hit = hit;
        this.miss = miss;
        this.data = data;
    }

    @Override
    public String toString() {
        return "OperationType: " + this.opType.desc() + ", Hit: " + this.hit + ", Miss: " + this.miss + ", data: " + this.data;
    }
}
