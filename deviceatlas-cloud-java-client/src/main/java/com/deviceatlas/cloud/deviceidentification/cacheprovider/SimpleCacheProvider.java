/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Afilias Technologies Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


package com.deviceatlas.cloud.deviceidentification.cacheprovider;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Map cache provider. The data is cached in memory inside a HashMap.
 *
 * @author Afilias Technologies Ltd
 */
public class SimpleCacheProvider implements CacheProvider {
    private static final int MAX_ENTRIES            = 4096;
    private static final int MAX_ENTRIES_EXTRA      = 5;
    private static final float LOAD_FACTOR          = 1f;
    private ConcurrentMap<String, Object> cache;
    private final Object cacheLock = new Object();

    public SimpleCacheProvider() {
        cache = new ConcurrentHashMap<String, Object>(MAX_ENTRIES + MAX_ENTRIES_EXTRA, LOAD_FACTOR);
    }

    @Override
    public <T> T get(String key) throws CacheException {
        return (T)cache.get(key);
    }

    @Override
    public <T> void set(String key, T entry) throws CacheException {
        synchronized(cacheLock) {
            if (cache.size() >= MAX_ENTRIES) {
                clear();
            }
        }

        cache.put(key, entry);
    }

    @Override
    public void remove(String key) throws CacheException {
        if (get(key) != null) {
            cache.remove(key);
        }
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public void shutdown() {
        return;
    }

    @Override
    public List <String> getKeys() {
        List keys = new ArrayList();
        for (String key : cache.keySet()) {
            keys.add(key);
        }

        return keys;
    }

    @Override
    public void setExpiry(int expiry) { 
        return;
    }
}
