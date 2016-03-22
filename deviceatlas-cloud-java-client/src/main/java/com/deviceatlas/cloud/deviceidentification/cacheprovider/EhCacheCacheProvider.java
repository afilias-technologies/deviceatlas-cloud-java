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

import java.util.List;
import java.net.URL;

import com.deviceatlas.cloud.deviceidentification.client.ClientConstants;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * EhCache cache provider. 
 *
 * @author Afilias Technologies Ltd
 */
public class EhCacheCacheProvider implements CacheProvider {
    private static final String SETTING_FILE        = "/deviceatlas-ehcache.xml";
    private static final String CACHE_MANAGER_NAME  = "deviceAtlasCloudCacheManager";
    private CacheManager       cacheManager;
    private Cache              cache;

    public EhCacheCacheProvider(String name, int expiry) {
        URL url = getClass().getResource(SETTING_FILE);
        cacheManager = new CacheManager(url);
        cacheManager.setName(CACHE_MANAGER_NAME);
        cache = cacheManager.getCache(name);

        if (expiry > 0) {
            cache.getCacheConfiguration().setTimeToLiveSeconds((long)expiry);
        }
    }

    public EhCacheCacheProvider() {
        this(ClientConstants.CACHE_NAME.toString(), 0);
    }

    @Override
    public <T> T get(String key) throws CacheException {
        try {
            Element elt = cache.get(key);
            if (elt != null) {
                return (T)elt.getObjectValue();
            }
        } catch (net.sf.ehcache.CacheException ex) {
            throw new CacheException(
                    "Failed to get cache entry in " + key, ex
                    );
        }

        return null;
    }

    @Override
    public <T> void set(String key, T entry) throws CacheException {
        try {
            Element elt = new Element(key, entry);
            cache.put(elt);
        } catch (net.sf.ehcache.CacheException ex) {
            throw new CacheException(
                    "Failed to put cache entry in " + key, ex
                    );
        }
    }

    @Override
    public void remove(String key) throws CacheException {
        try {
            cache.remove(key);
        } catch (net.sf.ehcache.CacheException ex) {
            throw new CacheException(
                    "Failed to remove cache entry in " + key, ex
                    );
        }
    }

    @Override
    public void clear() {
        cache.removeAll();
    }

    @Override
    public void shutdown() {
        cacheManager.shutdown();
    }

    @Override
    public List <String> getKeys() {
        return cache.getKeys();
    }

    @Override
    public void setExpiry(int expiry) {
        cache.getCacheConfiguration().setTimeToLiveSeconds((long)expiry);
    }
}
