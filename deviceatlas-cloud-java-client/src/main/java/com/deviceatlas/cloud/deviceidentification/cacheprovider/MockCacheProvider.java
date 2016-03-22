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
import java.util.ArrayList;

/**
 * Mock a cache provider. 
 * 
 * Use this cache provider only for testing and experiments, do not use this 
 * in production.
 *
 * @author Afilias Technologies Ltd
 */
public class MockCacheProvider implements CacheProvider {
    public MockCacheProvider() {
    }

    @Override
    public <T> T get(String key) throws CacheException {
        return null; 
    }
    /**
     * No set method implementation for mock cache
     */
    @Override
    public <T> void set(String key, T entry) throws CacheException {
        return;
    }
    /**
     * No remove method implementation for mock cache
     */
    @Override
    public void remove(String key) throws CacheException {
        return;
    }
    /**
     * No clear method implementation for mock cache
     */
    @Override
    public void clear() { 
        return;
    }
    /**
     * No shutdown method implementation for mock cache
     */
    @Override
    public void shutdown() { 
        return;
    }
    @Override
    public List<String> getKeys() { 
        return new ArrayList<String>();  
    }
    /**
     * No expiry setting method implementation for mock cache
     */
    @Override
    public void setExpiry(int expiry) { 
        return;
    }
}
