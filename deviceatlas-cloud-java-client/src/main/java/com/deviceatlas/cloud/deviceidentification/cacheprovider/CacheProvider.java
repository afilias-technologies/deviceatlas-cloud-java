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

/**
 * Cache Provider interface. 
 *
 * @author Afilias Technologies Ltd
 */
public interface CacheProvider {
    /**
     * Getting an entry on the cache
     * @param key key of the cache entry
     * @param <T> cache entry
     * @throws CacheException when there is cache issues getting by key
     * @return Object
     */
    <T> T get(String key) throws CacheException;

    /**
     * Adding an entry to the cache
     * @param key key of the cache entry
     * @param entry cache entry
     * @param <T> cache entry
     * @throws CacheException when there is cache issues setting an element
     */
    <T> void set(String key, T entry) throws CacheException;

    /**
     * Removes an entry in the cache
     * @param key key of the cache entry
     * @throws CacheException when there is cache issues removing an entry
     */
    void remove(String key) throws CacheException;

    /**
     * Entirely clear the cache content
     */
    void clear();

    /**
     * Shutdown the general cache framework
     */
    void shutdown();

    /**
     * Returns all keys from a given cache
     * @return List
     */
    List<String> getKeys();

    /**
     * Reconfigure the expiry after
     * @param expiry cache time life in minutes
     */
    void setExpiry(int expiry);
}
