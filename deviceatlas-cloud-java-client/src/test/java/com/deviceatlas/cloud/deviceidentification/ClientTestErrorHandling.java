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


package com.deviceatlas.cloud.deviceidentification;

import com.deviceatlas.cloud.deviceidentification.client.Client;
import com.deviceatlas.cloud.deviceidentification.client.ClientException;
import com.deviceatlas.cloud.deviceidentification.client.ClientConstants;
import com.deviceatlas.cloud.deviceidentification.client.IncorrectPropertyTypeException;
import com.deviceatlas.cloud.deviceidentification.parser.JsonParser;
import com.deviceatlas.cloud.deviceidentification.endpoint.EndPoint;
import com.deviceatlas.cloud.deviceidentification.utils.NetworkUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.deviceatlas.cloud.deviceidentification.cacheprovider.FileCacheProvider;
import com.deviceatlas.cloud.deviceidentification.cacheprovider.EhCacheCacheProvider;
import com.deviceatlas.cloud.deviceidentification.cacheprovider.MemcachedCacheProvider;
import com.deviceatlas.cloud.deviceidentification.cacheprovider.CacheException;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.net.URL;
import java.net.HttpURLConnection;

/**
 * Unit tests for error and exception cases
 *
 * note! all cloud endpoints defined in the API must be healthy, otherwise the tests wont work as expected
 * note! this tests must be change if cloud error messages change
 *
 * @copyright Copyright (c) 2008-2016 by Afilias Technologies Limited. All rights reserved.
 * @author Afilias Technologies Ltd
 */
public class ClientTestErrorHandling {

    private static Client client                  = null;
    private static Map result                     = null;

    @Rule
    public ExpectedException exc = ExpectedException.none();

    private static final String ACTIVE_LICENCE    = System.getProperty("activeLicence");
    private static final String INVALID_LICENCE   = "INVALID_LICENCE";
    private static final String EXPIRED_LICENCE   = "EXPIRED_LICENCE";
    private static final String EXCEEDED_LICENCE  = "EXCEEDED_LICENCE";

    private static final String TEST_UA           = "Mozilla/5.0 (Linux; U; Android 2.3.3; en-gb; GT-I9100 Build/GINGERBREAD) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";

    EndPoint[] goodEndPoints                      = {
        new EndPoint("http://region0.deviceatlascloud.com", "80"),
        new EndPoint("http://region1.deviceatlascloud.com", "80"),
        new EndPoint("http://region2.deviceatlascloud.com", "80"),
        new EndPoint("http://region3.deviceatlascloud.com", "80"),
    };

    EndPoint[] badEndPoints                       = {
        new EndPoint("1.r1.!@#$<>endpoint.bad", "80"),
        new EndPoint("2.r2.!@#$<>endpoint.bad", "80"),
        new EndPoint("4.r3.!@#$<>endpoint.bad", "80"),
        new EndPoint("3.r4.!@#$<>endpoint.bad", "80"),
    };

    private boolean hasLicenceSet(String licenceKey) {
        return licenceKey != null;
    }

    public ClientTestErrorHandling() {
        if (!hasLicenceSet(ACTIVE_LICENCE)) {
            throw new RuntimeException("activeLicence system property us mandatory");
        }
    }

    @BeforeClass
    public static void clientSetup() {
        try {
            client = Client.getInstance(new FileCacheProvider());
            client.setAutoServerRanking(true);
            client.setCloudServiceTimeout(5);
            client.setAutoServerRankingMaxFailures(2);
            client.setAutoServerRankingNumRequests(4);
            client.setServerRankingLifetime(3600);
            client.setPhaseOutLifetime(client.getServerRankingLifetime());
            client.setAutoServerRankingLifetime(client.getServerRankingLifetime());
            client.clearCache();
            client.setLicenceKey(ACTIVE_LICENCE);
            result = client.getDeviceData(TEST_UA);
            assertNotNull(result);
            assertEquals("TIMEOUT", 5, client.getCloudServiceTimeout());
            assertEquals("FAILURES", 2, client.getAutoServerRankingMaxFailures());
            assertEquals("REQUESTS", 4, client.getAutoServerRankingNumRequests());
            assertEquals("PHASEOUT", client.getServerRankingLifetime(), client.getPhaseOutLifetime());
            assertEquals("SERVERRANKING", client.getServerRankingLifetime(), client.getAutoServerRankingLifetime());
        } catch (CacheException ex) {
        } catch (ClientException ex) {
        }
    }

    private int substrCount(String str, String findStr) {
        int lastIndex = 0;
        int count     = 0;
        while (lastIndex != -1) {
            lastIndex = str.indexOf(findStr, lastIndex);
            if (lastIndex != -1) {
                count ++;
                lastIndex += findStr.length();
             }
        }
        return count;
    }

    @AfterClass
    public static void clientFinalize() {
        client.clearCache();
        client.shutdown();
    }

    @Test
    public void testCacheProvider() throws Exception {
        client.clearCache();
        FileCacheProvider fp = new FileCacheProvider();
        File f = new File("null");
        Path p = Paths.get(f.toString(), "null");
        p = fp.getCachePath("null");
        fp.lockAndSet("test", "value");
        fp.setCacheFileEntry(f, p);
        fp = new FileCacheProvider();
        fp.set("test", "value");
        assertEquals("TEST", "value", fp.get("test"));
        assertNotEquals("KEYS", 0, fp.getKeys().size());
        fp.remove("test");
        fp.clear();
        assertEquals("KEYS", 0, fp.getKeys().size());
        fp.shutdown();

        EhCacheCacheProvider eh = new EhCacheCacheProvider();
        eh.setExpiry(10);
        eh.set("test", "value");
        eh.get("test");
        assertNull("TEST", eh.get("test2"));
        eh.remove("test");
        eh.clear();
        assertNotNull("KEYS", eh.getKeys());
        eh.shutdown();

        MemcachedCacheProvider mp = new MemcachedCacheProvider();
        mp.setExpiry(10);
        mp.set("test", "value");
        mp.get("test");
        assertNull("TEST", mp.get("test2"));
        mp.remove("test");
        mp.clear();
        assertNotNull("KEYS", mp.getKeys());
        mp.shutdown();
    }

    /**
     * Tests of json parser
     */
    @Test
    public void testJsonParser() throws Exception {
        final String goodJson = "{\"test\":[0,1,2,3,4]}";
        final String badJson = "{\"key\": value}";
        JsonParser ps = new JsonParser(goodJson);
        assertNotNull(ps);
        ps = new JsonParser(badJson);
        assertNotNull(ps);
    }

    /**
     * Tests cloud connection
     */
    @Test
    public void testConnectionData() throws Exception {
        NetworkUtils nw = new NetworkUtils(null);
        URL u = new URL("http://www.example.com");
        Map<String, Object> connectionRet = new HashMap<String, Object>();
        HttpURLConnection url = (HttpURLConnection)u.openConnection();
        nw.setConnectionData(url, connectionRet);
    }

    @Test
    public void testException() throws Exception {
        IncorrectPropertyTypeException ex = new IncorrectPropertyTypeException("test");
        assertEquals("CAUSE", "test", ex.getMessage());
    }

    /**
     * Test network issue
     */
    @Test
    public void testNetworkIssue() throws Exception {
        if (!hasLicenceSet(ACTIVE_LICENCE)) {
            return;
        }
        client.clearCache();
        assertEquals("NO_KEYS", 0, client.getCacheService().getCacheProvider().getKeys().size());
        client.setUseCache(false);
        client.setLicenceKey(ACTIVE_LICENCE);
        client.setEndPoints(badEndPoints);
        EndPoint [] cachedServers = client.getCacheService().getCachedServerList(ClientConstants.CACHE_NAME_SERVERS_AUTO.toString());
        assertNull(cachedServers);
        Client mockClient = spy(client);

        exc.expect(Exception.class);
        when(mockClient.getDeviceData(TEST_UA)).thenThrow(Exception.class);

    }

    /**
     * Test cases that the cloud returns "forbidden licence"
     */

    private void testInvalidLicenceCases(String title, String licenceKey) throws Exception {

        client.clearCache();
        client.setUseCache(false);
        client.setLicenceKey(licenceKey);
        client.setEndPoints(goodEndPoints);
        Client mockClient = spy(client);

        exc.expect(Exception.class);
        when(mockClient.getDeviceData(TEST_UA)).thenThrow(Exception.class);
    }

    /**
     * Test invalid licence
     */
    @Test
    public void testInvalidLicence() throws Exception {
        testInvalidLicenceCases("invalid licence", INVALID_LICENCE);
    }

    /**
     * Test expired licence
     */
    @Test
    public void testExpiredLicence() throws Exception {
        testInvalidLicenceCases("expired licence", EXPIRED_LICENCE);
    }

    /**
     * Test exceeded licence
     */
    @Test
    public void testExceededLicence() throws Exception {
        client.clearCache();
        client.setUseCache(false);
        client.setLicenceKey(EXCEEDED_LICENCE);
        client.setEndPoints(goodEndPoints);

        testInvalidLicenceCases("exceeded licence", EXCEEDED_LICENCE);
    }
}
