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

package com.deviceatlas.cloud.deviceidentification.service;

import com.deviceatlas.cloud.deviceidentification.cacheprovider.CacheException;
import com.deviceatlas.cloud.deviceidentification.cacheprovider.CacheProvider;
import com.deviceatlas.cloud.deviceidentification.cacheprovider.FileCacheProvider;

import com.deviceatlas.cloud.deviceidentification.endpoint.EndPoint;
import com.deviceatlas.cloud.deviceidentification.client.ClientConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CacheService {
    /**
     * Please see ClientConstants.CACHE_NAME.toString()
     * @deprecated
     */
    @Deprecated public static final String CACHE_NAME                   = ClientConstants.CACHE_NAME.toString();
    /**
     * Please see ClientConstants.CACHE_NAME_SERVERS_AUTO.toString()
     * @deprecated
     */
    @Deprecated public static final String CACHE_NAME_SERVERS_AUTO      = ClientConstants.CACHE_NAME_SERVERS_AUTO.toString();
    /**
     * Please see ClientConstants.CACHE_NAME_SERVERS_MANUAL.toString()
     * @deprecated
     */
    @Deprecated public static final String CACHE_NAME_SERVERS_MANUAL    = ClientConstants.CACHE_NAME_SERVERS_MANUAL.toString();
    
    private int     serverRankListLifetime                              = 1440;
    private boolean useCache                                            = true;

    private CacheProvider   cachePropsProvider;
    private CacheProvider   cacheServersRankingProvider;

    public CacheService(CacheProvider cachePropsProvider) throws CacheException {
        this.cachePropsProvider = cachePropsProvider;
        this.cacheServersRankingProvider = new FileCacheProvider();
        cacheServersRankingProvider.setExpiry(serverRankListLifetime);
    }

    /**
     * Returns the server ranking cache lifetime
     *
     * @return serverRankingLifetime
     */

    public int getServerRankingLifetime() {
        return serverRankListLifetime;
    }

    /**
     * Sets the server ranking cache lifetime
     *
     * @param serverRankListLifetime
     */
    
    public void setServerRankingLifetime(int serverRankListLifetime) {
        this.serverRankListLifetime = serverRankListLifetime;
    }

    /**
     * Gets the cache Provider
     *
     * @return cachePropsProvider
     */

    public CacheProvider getCacheProvider() {
        return cachePropsProvider;
    }

    /**
     * Is device data being cached by the API or not.
     *
     * @return true(default) = data is being cached.
     */
    public boolean getUseCache() {
        return useCache;
    }

    /**
     * Setter to cache or not to cache device data.
     *
     * @param useCache true = cache data after getting device data from DeviceAtlas cloud
     */
    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    /**
     * Clear all cached data.
     */
    public void clearCache() {
        cachePropsProvider.clear();
        cacheServersRankingProvider.clear();
    }

    /**
     * This should be called when shutting down your application. It asks
     * the cache manager to shutdown the cache and write the cache to disk.
     *
     * See http://ehcache.org/documentation/code-samples#shutdown-the-cachemanager
     */
     public void shutdown() {
         this.cachePropsProvider.shutdown();
         this.cacheServersRankingProvider.shutdown();
     }

    /**
     * Get cached server list. For debugging cache contents.
     *
     * @param cacheKey cache key Client.CACHE_NAME_SERVERS_AUTO or Client.CACHE_NAME_SERVERS_MANUAL
     * @return array of EndPoint objects or null if cache is empty
     */
    public EndPoint[] getCachedServerList(String cacheKey) throws CacheException {
        EndPoint[] cachedEndPoints = null;

        if (cacheKey.equals(ClientConstants.CACHE_NAME_SERVERS_AUTO.toString())) {
            List<Map> data = cacheServersRankingProvider.get(ClientConstants.CACHE_NAME_SERVERS_AUTO.toString());
            if (data != null) {
                cachedEndPoints = convertServerMapListToServerList(data);
            }
        } else if (cacheKey.equals(ClientConstants.CACHE_NAME_SERVERS_MANUAL.toString())) {
            List<Map> data = cacheServersRankingProvider.get(ClientConstants.CACHE_NAME_SERVERS_MANUAL.toString());
            if (data != null) {
                cachedEndPoints = convertServerMapListToServerList(data);
            }
        }

        return cachedEndPoints;
    }

    /**
     * Convert a list of (server-data HashMaps) to an Array of EndPoint objects
     * ehcache stores (server-data HashMaps)
     */
    public EndPoint[] convertServerMapListToServerList(List<Map> listOfServerMaps) {
        List<EndPoint> endPointL = new ArrayList<EndPoint>();

        for (Map serverMap : listOfServerMaps) {
            EndPoint newEndPoint = new EndPoint();
            newEndPoint.fromMap(serverMap);
            endPointL.add(newEndPoint);
        }

        return endPointL.toArray(new EndPoint[endPointL.size()]);
    }

    /**
     * Write server list to cache. Depending on the key auto ranked server list
     * or manual ranking fail-over server list will be set
     */
    public void setServerCache(EndPoint[] endPoints, boolean isManual) throws CacheException {
        // if server caching is disabled
        if (serverRankListLifetime == 0) {
            return;
        }

        String key = isManual ? ClientConstants.CACHE_NAME_SERVERS_MANUAL.toString() :
            ClientConstants.CACHE_NAME_SERVERS_AUTO.toString();
        // cache sorted endPoints
        try {
            /* convert EndPoint array to (server-data HashMap) */
            List<Map> listOfServerMaps = new ArrayList<Map>();
            for (EndPoint endPoint : endPoints) {
                listOfServerMaps.add(endPoint.toMap());
            }
            /* cache auto or manual */
            cacheServersRankingProvider.set(key, listOfServerMaps);
        } catch (CacheException ex) {
            throw new CacheException("Failed to put end point list in cache.", ex);
        }
    }

    /**
     * Returns the list of ranked servers (auto or manual)
     *
     * @param key
     * @return List
     */

    private List<Map> getCacheServersRanking(String key) throws CacheException {
        return cacheServersRankingProvider.get(key);
    }

    /**
     * Returns the list of auto ranked servers
     *
     * @return List
     */

    public List<Map> getCacheServersAutoRanking() throws CacheException {
        return getCacheServersRanking(ClientConstants.CACHE_NAME_SERVERS_AUTO.toString());
    }

    /**
     * Returns the list of manual ranked servers
     *
     * @return List
     */
    public List<Map> getCacheServersManualRanking() throws CacheException {
        return getCacheServersRanking(ClientConstants.CACHE_NAME_SERVERS_MANUAL.toString());
    }
}
