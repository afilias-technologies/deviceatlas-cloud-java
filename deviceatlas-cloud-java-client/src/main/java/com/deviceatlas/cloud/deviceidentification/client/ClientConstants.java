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

package com.deviceatlas.cloud.deviceidentification.client;

public enum ClientConstants {
    /** API Version */
    API_VERSION("2.0.0"),
    /** Name of the cookie created by the DeviceAtlas client side component */
    CLIENT_COOKIE_NAME("DAPROPS"),
    /** The name of the key used to lookup the User-Agent in the results object */
    KEY_USERAGENT("useragent"),
    /** The name of the key used to lookup the source of data (none, cache or cloud) in the results object */
    KEY_SOURCE("source"),
    /** The name of the key used to lookup the device properties in the results object */
    KEY_PROPERTIES("properties"),
    /**
     * In the result Map, KEY_SOURCE may be set to this. Shows that the data source
     * of device properties set to KEY_PROPERTIES in the result Map was cache.
     */
    SOURCE_CACHE("cache"),
    /**
     * In the result Map, KEY_SOURCE may be set to this. Shows that the device
     * properties set to KEY_PROPERTIES where queried from DA cloud service.
     */
    SOURCE_CLOUD("cloud"),
    /**
     * In the result Map, KEY_SOURCE may be set to this.
     * Indicates that there was a problem getting device data.
     */
    SOURCE_NONE ("none"),
    /** ehcache settings - cache key for cached device data */
    CACHE_NAME("deviceatlascache"),
    /** ehcache settings - cache key for cached endPoints ranked list */
    CACHE_NAME_SERVERS_AUTO("deviceatlas_servers_cache_auto"),
    /** ehcache settings - cache key for cached endPoints manual fail-over list */
    CACHE_NAME_SERVERS_MANUAL("deviceatlas_servers_cache_manual"),
    /* String to identify Opera headers */
    OPERA_HEADER_IDENTIFIER("opera"),
    /** fields for connection service return **/
    CLOUD_SERVICE_STATUS("status"),
    CLOUD_SERVICE_MESSAGE("message"),
    CLOUD_SERVICE_RESULT("result");

    private final String text;

    ClientConstants(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}

