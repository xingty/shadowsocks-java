package org.wiyi.ss.core;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K,V> extends LinkedHashMap<K,V> {
    private static final int MAX_CACHE_SIZE = 10;

    private int cacheSize;

    public LRUCache() {
        this(MAX_CACHE_SIZE);
    }

    public LRUCache(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > cacheSize;
    }

}
