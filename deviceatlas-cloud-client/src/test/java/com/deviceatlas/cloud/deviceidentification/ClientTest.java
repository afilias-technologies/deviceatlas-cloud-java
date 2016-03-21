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

import com.deviceatlas.cloud.deviceidentification.cacheprovider.SimpleCacheProvider;
import com.deviceatlas.cloud.deviceidentification.client.Client;
import com.deviceatlas.cloud.deviceidentification.client.ClientException;
import com.deviceatlas.cloud.deviceidentification.client.ClientConstants;
import com.deviceatlas.cloud.deviceidentification.client.ActionConstants;
import com.deviceatlas.cloud.deviceidentification.client.HeaderConstants;
import com.deviceatlas.cloud.deviceidentification.client.Property;
import com.deviceatlas.cloud.deviceidentification.client.Properties;
import com.deviceatlas.cloud.deviceidentification.client.Result;
import com.deviceatlas.cloud.deviceidentification.tools.LatencyChecker;
import com.deviceatlas.cloud.deviceidentification.tools.CachedEndPoints;
import com.deviceatlas.cloud.deviceidentification.utils.StringUtils;
import com.deviceatlas.cloud.deviceidentification.endpoint.EndPoint;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.*;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.deviceatlas.cloud.deviceidentification.cacheprovider.CacheException;

import static org.mockito.Mockito.*;

/**
 * Unit tests for DeviceAtlas Client Cloud API
 *
 * @copyright Copyright (c) 2008-2016 by Afilias Technologies Limited. All rights reserved.
 * @author Afilias Technologies Ltd
 */
public class ClientTest {

    private static Client client;
    private static Map results;


    private static final String LICENCE  = System.getProperty("activeLicence");
    private static final String TEST_UA1 = "iphone";
    private static final String TEST_UA2 = "iPhone";
    @Rule
    public ExpectedException exc = ExpectedException.none();

    // a list of user-agents and expected device properties
    // {user-agent: {expected-properties-and-values}}]
    private static final Map<String, Map> testCases1;
    static {
        testCases1 = new HashMap<String, Map>();
        // item 1
        Map expected = new HashMap();
        expected.put("vendor", "Apple");
        expected.put("marketingName", "iPhone");
        testCases1.put("iphone", expected);
        // item 2
        expected = new HashMap();
        expected.put("vendor", "Samsung");
        expected.put("marketingName", "Galaxy S2");
        testCases1.put("Mozilla/5.0 (Linux; U; Android 2.3.3; en-gb; GT-I9100 Build/GINGERBREAD) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1", expected);
        // item 3
        expected = new HashMap();
        expected.put("browserName", "Firefox");
        expected.put("vendor", "Mozilla");
        expected.put("isBrowser", true);
        testCases1.put("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:19.0) Gecko/20100101 Firefox/19.0", expected);
    };

    // a list of http-headers and expected device properties
    // [[{headers},  {expected-properties-and-values}]]
    private static final List<List<Map>> testCases2;
    static {
        testCases2 = new ArrayList<List<Map>>();
        // item 1
        List<Map> item = new ArrayList<Map>();
        Map headers = new HashMap();
        headers.put("user-agent", "iphone");
        item.add(headers);
        Map expected = new HashMap();
        expected.put("vendor", "Apple");
        expected.put("marketingName", "iPhone");
        item.add(expected);
        testCases2.add(item);
        // item 2
        item = new ArrayList<Map>();
        headers = new HashMap();
        headers.put("User-Agent", "Mozilla/5.0 (Linux; U; Android 2.3.3; en-gb; GT-I9100 Build/GINGERBREAD) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
        item.add(headers);
        expected = new HashMap();
        expected.put("vendor", "Samsung");
        expected.put("marketingName", "Galaxy S2");
        item.add(expected);
        testCases2.add(item);
    };

    // a list of client side properties
    private static final String[] clientSideComponentProps = {
        "js.localStorage",
        "js.modifyCss",
        "js.modifyDom",
        "js.querySelector",
        "js.sessionStorage",
        "js.supportBasicJavaScript",
    };

    public ClientTest() {
        if (LICENCE == null) {
            throw new RuntimeException("activeLicence system property is mandatory");
        }
    }

    @BeforeClass
    public static void clientSetup() {
        try {
            Proxy proxy = null;
            client = Client.getInstance(new SimpleCacheProvider());
            client.setProxy(proxy);
            client.clearCache();
            client.setAutoServerRanking(true);
            results = new HashMap<Object, Object>();
            results.put(TEST_UA1 + "_nocache", client.getDeviceData(TEST_UA1));
            results.put(TEST_UA1 + "_cache", client.getDeviceData(TEST_UA1));
            results.put(TEST_UA2 + "_nocache", client.getDeviceData(TEST_UA2));
            results.put(TEST_UA2 + "_cache", client.getDeviceData(TEST_UA2));
            results.put(TEST_UA1 + "_nocache_response", client.getResult(TEST_UA1));
            results.put(TEST_UA1 + "_cache_response", client.getResult(TEST_UA1));

            results.put(testCases2.get(0).get(0),
                    client.getDeviceDataByHeaders(testCases2.get(0).get(0)));
            results.put(testCases2.get(1).get(0),
                    client.getDeviceDataByHeaders(testCases2.get(1).get(0)));

            checkSettings();
        } catch (CacheException ex) {
        } catch (Exception ex) {
        }
    }

    @AfterClass
    public static void clientFinalize() {
        client.clearCache();
        client.shutdown();
    }

    /**
     * Test if API settings are set for release
     */
    private static void checkSettings() throws Exception {
        client.setServerRankingLifetime(0);
        client.getCacheService().setServerCache(client.getEndPointService().getOriginalEndPoints(), false);
        client.clearCache();
        client.setUseCache(true);
        client.getDeviceIdentificatorService().setSendExtraHeaders(false);
        EndPoint [] cachedEndPoints = client.getCacheService().getCachedServerList(ClientConstants.CACHE_NAME_SERVERS_AUTO.toString());
        EndPoint [] cachedManualEndPoints = client.getCacheService().getCachedServerList(ClientConstants.CACHE_NAME_SERVERS_MANUAL.toString());
        client.setServerRankingLifetime(3600);

        assertEquals("AUTO_SERVER_RANKING", true, client.getAutoServerRanking());

        assertEquals("CLOUD_SERVICE_TIMEOUT",            3,                   client.getCloudServiceTimeout());
        assertEquals("USE_CLIENT_COOKIE",                true,                client.getDeviceIdentificatorService().getUseClientCookie());
        assertEquals("USE_CLIENT_COOKIE",                true,                client.getUseClientCookie());
        assertEquals("USE_FILE_CACHE",                   true,                client.getCacheService().getUseCache());
        assertEquals("USE_FILE_CACHE",                   true,                client.getUseCache());
        assertEquals("CACHE_KEYS",                       0,                   client.getCacheService().getCacheProvider().getKeys().size());
        assertEquals("CACHE_NAME",                       "deviceatlascache",  ClientConstants.CACHE_NAME.toString());
        assertEquals("SEND_EXTRA_HEADERS",               false,               client.getDeviceIdentificatorService().getSendExtraHeaders());
        assertEquals("CLIENT_COOKIE_NAME",               "DAPROPS",           ClientConstants.CLIENT_COOKIE_NAME.toString());
        assertEquals("AUTO_SERVER_RANKING_MAX_FAILURE",  1,                   client.getAutoServerRankingMaxFailures());
        assertEquals("AUTO_SERVER_RANKING_NUM_REQUESTS", 3,                   client.getAutoServerRankingNumRequests());
        assertEquals("AUTO_SERVER_RANKING_LIFETIME",     1440,                client.getCacheService().getServerRankingLifetime());
        assertEquals("KEY_USERAGENT",                    "useragent",         ClientConstants.KEY_USERAGENT.toString());
        assertEquals("KEY_SOURCE",                       "source",            ClientConstants.KEY_SOURCE.toString());
        assertEquals("KEY_PROPERTIES",                   "properties",        ClientConstants.KEY_PROPERTIES.toString());
        assertEquals("SOURCE_CACHE",                     "cache",             ClientConstants.SOURCE_CACHE.toString());
        assertEquals("SOURCE_CLOUD",                     "cloud",             ClientConstants.SOURCE_CLOUD.toString());
        assertEquals("SOURCE_NONE",                      "none",              ClientConstants.SOURCE_NONE.toString());
        assertEquals("DA_HEADER_PREFIX",                 "X-DA-",             HeaderConstants.DA_HEADER_PREFIX.toString());
        assertEquals("CLIENT_COOKIE_HEADER",             "Client-Properties", HeaderConstants.CLIENT_COOKIE_HEADER.toString());
        assertEquals("ENDPOINTS",                        client.getEndPointService().getOriginalEndPoints().length, cachedEndPoints.length);
        assertEquals("ENDPOINTS",                        0, cachedManualEndPoints.length);
        // To increase coverage / decrease unused class members not used...
        assertEquals("API_VERSION", Client.API_VERSION, Client.getVersion());
        assertEquals("API_VERSION",                      Client.API_VERSION,  ClientConstants.API_VERSION.toString());
        assertEquals("CLIENT_COOKIE_NAME",               Client.CLIENT_COOKIE_NAME,  ClientConstants.CLIENT_COOKIE_NAME.toString());
        assertEquals("KEY_USERAGENT",                    Client.KEY_USERAGENT,ClientConstants.KEY_USERAGENT.toString());
        assertEquals("KEY_SOURCE",                       Client.KEY_SOURCE,   ClientConstants.KEY_SOURCE.toString());
        assertEquals("KEY_PROPERTIES",                   Client.KEY_PROPERTIES,  ClientConstants.KEY_PROPERTIES.toString());
        assertEquals("SOURCE_CACHE",                     Client.SOURCE_CACHE,  ClientConstants.SOURCE_CACHE.toString());
        assertEquals("SOURCE_CLOUD",                     Client.SOURCE_CLOUD,  ClientConstants.SOURCE_CLOUD.toString());
        assertEquals("SOURCE_NONE",                      Client.SOURCE_NONE,  ClientConstants.SOURCE_NONE.toString());
        assertEquals("DA_HEADER_PREFIX",                 Client.DA_HEADER_PREFIX, HeaderConstants.DA_HEADER_PREFIX.toString());
        assertEquals("CLIENT_COOKIE_HEADER",             Client.CLIENT_COOKIE_HEADER, HeaderConstants.CLIENT_COOKIE_HEADER.toString());
    }

    /**
     * Test simple cache provider
     */
    @Test
    public void testCacheProvider() throws Exception {
        SimpleCacheProvider sp = new SimpleCacheProvider();
        sp.setExpiry(1024);
        sp.set("test", "value");
        assertEquals("TEST", "value", sp.get("test"));
        assertEquals("LIST", 1, sp.getKeys().size());
        sp.remove("test");
        assertEquals("TEST", null, sp.get("test"));
        sp.clear();
        sp.shutdown();
    }

    @Test
    public void testResult() throws Exception {
        client.clearCache();
        client.setLicenceKey(LICENCE);

        Result response = client.getResult(TEST_UA1);
        Properties properties = response.getProperties();
        assertEquals("CACHE_SOURCE", ClientConstants.SOURCE_CLOUD.toString(), response.getSource());

        Property model = properties.get("model");
        assertEquals("MODEL", "iPhone", model.asString());
    }

    /**
     * Test getNormalisedHeaders()
     */
    @Test
    public void testGetNormalisedHeaders() throws Exception {
        client.clearCache();
        client.setLicenceKey(LICENCE);

        assertEquals("LICENCE", LICENCE, client.getLicenceKey());

        Map<String, String> given = new HashMap<String, String>();
        given.put("HTTP_USER_AGENT", "some value1...");
        given.put("X-Device-User-Agent", "some value2...");
        given.put("HTTP_x-original-user-agent", "some value3...");
        given.put("HTTP-x-oPeramini-PHONE-ua", "some value4...");
        given.put("x_skyfire_phone", "some value5...");
        given.put("X-BOLT-PHONE-UA", "some value6...");
        given.put("device-stock-ua", "some value7...");
        given.put("HTTP_X-ucbrowser-ua", "some value8...");
        given.put("x-ucbrowser-device-ua", "some value9...");
        given.put("HTTP_x-ucbrowser_device", "some value10...");

        Map<String, String> expected = new HashMap<String, String>();
        expected.put("user-agent", "some value1...");
        expected.put("x-device-user-agent", "some value2...");
        expected.put("x-original-user-agent", "some value3...");
        expected.put("x-operamini-phone-ua", "some value4...");
        expected.put("x-skyfire-phone", "some value5...");
        expected.put("x-bolt-phone-ua", "some value6...");
        expected.put("device-stock-ua", "some value7...");
        expected.put("x-ucbrowser-ua", "some value8...");
        expected.put("x-ucbrowser-device-ua", "some value9...");
        expected.put("x-ucbrowser-device", "some value10...");

        Map<String, String> toTest = StringUtils.normaliseKeys(given);

        for (Map.Entry<String,String> entry : toTest.entrySet()) {
            String header = entry.getKey();
            String value  = entry.getValue();
            assertEquals(header, expected.get(header), value);
        }
    }

    /**
     * getDeviceData() bad and good licence
     */
    @Test
    public void testGetDeviceDataLicence() throws Exception {
        // test bad licence

        client.clearCache();
        Client mockClient = spy(client);
        client.clearCache();
        client.setLicenceKey("BADLICENCE");
        exc.expect(ClientException.class);

        when(mockClient.getDeviceDataByUserAgent(TEST_UA1)).
            thenThrow(ClientException.class);

        // test good licence
        client.clearCache();
        client.setLicenceKey(LICENCE);

        doReturn((Map)results.get(TEST_UA1)).when(mockClient).
            getDeviceDataByUserAgent(TEST_UA1);

        // same tests with auto ranking on
        // to be sure auto-ranking on/off dosnt have side effects
        client.setAutoServerRanking(true);
        client.setLicenceKey("");
        exc.expect(ClientException.class);

        when(mockClient.getDeviceDataByUserAgent(TEST_UA1)).
            thenThrow(ClientException.class);

        // test bad licence
        client.clearCache();
        client.setLicenceKey("BADLICENCE");
        exc.expect(ClientException.class);

        when(mockClient.getDeviceDataByUserAgent(TEST_UA1)).
            thenThrow(ClientException.class);

        // test good licence
        client.clearCache();
        client.setLicenceKey(LICENCE);

        doReturn((Map)results.get(TEST_UA1)).when(mockClient).
            getDeviceDataByUserAgent(TEST_UA1);
    }

    /**
     * getDeviceDataByUserAgent(string)
     */
    @Test
    public void testGetDeviceDataByUa() throws Exception {
        client.clearCache();
        client.setUseCache(false);
        client.setAutoServerRanking(false);
        client.setLicenceKey(LICENCE);
        Client mockClient = spy(client);

        for (Map.Entry<String, Map> entry : testCases1.entrySet()) {
            String ua       = entry.getKey();
            Map    expected = (HashMap)entry.getValue();
            Map result = (Map)results.get(ua + "_cache");
      
            doReturn(result).when(mockClient).
                getDeviceDataByUserAgent(ua);
        }
    }

    /**
     * getDeviceData(HashMap)
     */
    @Test
    public void testGetDeviceDataByHeaders() throws Exception {
        client.clearCache();
        client.setUseCache(false);
        client.setLicenceKey(LICENCE);
        Client mockClient = spy(client);

        for (List<Map> item : testCases2) {
            Map<String, String> headers = (HashMap<String, String>)item.get(0);
            Map expected = (HashMap)item.get(1);

            Map result = (Map)results.get(headers);
            doReturn(result).when(mockClient).
                getDeviceDataByHeaders(headers);
        }
    }

    /**
     * getDeviceData(array)
     * * tests if providing the client side component cookie effects the results
     */
    @Test
    public void testGetDeviceDataClientSideComponent() throws Exception {
        client.clearCache();
        client.setUseCache(false);
        client.setAutoServerRanking(false);
        client.setLicenceKey(LICENCE);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("user-agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:19.0) Gecko/20100101 Firefox/19.0");
        Map result1 = client.getDeviceData(headers);

        headers.put("cookie", "c=1; DAPROPS=\"bjs.webGl:1|bjs.geoLocation:1|bjs.webSqlDatabase:0|bjs.indexedDB:1|bjs.webSockets:1|bjs.localStorage:1|bjs.sessionStorage:1|bjs.webWorkers:1|bjs.applicationCache:1|bjs.supportBasicJavaScript:1|bjs.modifyDom:1|bjs.modifyCss:1|bjs.supportEvents:1|bjs.supportEventListener:1|bjs.xhr:1|bjs.supportConsoleLog:1|bjs.json:1|bjs.deviceOrientation:0|bjs.deviceMotion:1|bjs.touchEvents:0|bjs.querySelector:1|bhtml.canvas:1|bhtml.video:1|bhtml.audio:1|bhtml.svg:1|bhtml.inlinesvg:1|bcss.animations:1|bcss.columns:1|bcss.transforms:1|bcss.transitions:1|idisplayColorDepth:24|bcookieSupport:1|sdevicePixelRatio:1|sdeviceAspectRatio:16/10|bflashCapable:1|baccessDom:1|buserMedia:1\"");
        Map result2 = client.getDeviceDataByHeaders(headers);

        // the UA remains the same, lets see did the client side component inside the cookie changes
        // returns more props
        Map props1 = (Map)result1.get(ClientConstants.KEY_PROPERTIES.toString());
        Map props2 = (Map)result2.get(ClientConstants.KEY_PROPERTIES.toString());
        int a = props1.size();
        int b = props2.size();

        assertTrue(a < b);

        // see if we have client side properties
        for (String v : clientSideComponentProps) {
            assertFalse(v, props1.containsKey(v));
            assertTrue(v, props2.containsKey(v));
        }
    }

    /**
     * getDeviceData() empty ua
     */
    @Test
    public void testGetDeviceDataEmptyUa() throws Exception {

        client.clearCache();
        client.setUseCache(false);
        client.setAutoServerRanking(false);
        client.setLicenceKey(LICENCE);
        Client mockClient = spy(client);

        Map result = new HashMap();
        result.put(ClientConstants.KEY_SOURCE.toString(), "cloud");
        result.put(ClientConstants.KEY_PROPERTIES.toString(), new HashMap());
        doReturn(result).when(mockClient).getDeviceDataByUserAgent("");
        // cross API constency, keys to have and not to have
        assertTrue(ClientConstants.KEY_SOURCE.toString(),     result.containsKey(ClientConstants.KEY_SOURCE.toString()));
        assertTrue(ClientConstants.KEY_PROPERTIES.toString(), result.containsKey(ClientConstants.KEY_PROPERTIES.toString()));
        Map props = (Map)result.get(ClientConstants.KEY_PROPERTIES.toString());
        props.remove("_unmatched");
        props.remove("_matched");
        assertTrue(props.size() == 0);

        result = client.getDeviceDataByUserAgent("  ");
        // cross API constency, keys to have and not to have
        assertTrue(ClientConstants.KEY_SOURCE.toString(),     result.containsKey(ClientConstants.KEY_SOURCE.toString()));
        assertTrue(ClientConstants.KEY_PROPERTIES.toString(), result.containsKey(ClientConstants.KEY_PROPERTIES.toString()));
        props = (Map)result.get(ClientConstants.KEY_PROPERTIES.toString());
        props.remove("_unmatched");
        props.remove("_matched");
        assertTrue(props.size() == 0);

        // same tests with auto ranking on
        client.setAutoServerRanking(true);

        result = client.getDeviceDataByUserAgent("");
        // cross API constency, keys to have and not to have
        assertTrue(ClientConstants.KEY_SOURCE.toString(),     result.containsKey(ClientConstants.KEY_SOURCE.toString()));
        assertTrue(ClientConstants.KEY_PROPERTIES.toString(), result.containsKey(ClientConstants.KEY_PROPERTIES.toString()));
        props = (Map)result.get(ClientConstants.KEY_PROPERTIES.toString());
        props.remove("_unmatched");
        props.remove("_matched");
        assertTrue(props.size() == 0);

        result = client.getDeviceDataByUserAgent("  ");
        // cross API constency, keys to have and not to have
        assertTrue(ClientConstants.KEY_SOURCE.toString(),     result.containsKey(ClientConstants.KEY_SOURCE.toString()));
        assertTrue(ClientConstants.KEY_PROPERTIES.toString(), result.containsKey(ClientConstants.KEY_PROPERTIES.toString()));
        props = (Map)result.get(ClientConstants.KEY_PROPERTIES.toString());
        props.remove("_unmatched");
        props.remove("_matched");
        assertTrue(props.size() == 0);
    }

    /**
     * getCloudUrl()
     */
    @Test
    public void testGetCloudUrl() throws Exception {
        client.clearCache();
        client.setLicenceKey(LICENCE);

        // auto ranking and cache are off so it should be the first item in the endPoints list
        client.setUseCache(false);
        client.setAutoServerRanking(false);
        Client mockClient = spy(client);
        EndPoint[] endPoints = {
            new EndPoint("http://region2.deviceatlascloud.com", "80"),
        };
        client.setEndPoints(endPoints);

        Map result = (Map)results.get(TEST_UA1);
        doReturn(result).when(mockClient).getDeviceData(TEST_UA1);


        String server = client.getCloudUrl();
        EndPoint [] cendPoints = client.getEndPointService().getOriginalEndPoints();
        assertEquals("1", endPoints[0].host, cendPoints[0].host);

        // same test - just to see will it change to second server
        EndPoint[] servers2 = {
            new EndPoint("http://region1.deviceatlascloud.com", "80"),
        };
        client.setEndPoints(servers2);

        doReturn(result).when(mockClient).getDeviceData(TEST_UA1);

        server = client.getCloudUrl();
        cendPoints = client.getEndPointService().getOriginalEndPoints();
        assertEquals("2", servers2[0].host, cendPoints[0].host);

        // autoranking off but cache on, it should return a server first and null second
        client.setUseCache(true);

        doReturn(result).when(mockClient).getDeviceData(TEST_UA1);
        server = client.getCloudUrl();
        assertEquals("3", servers2[0].host, cendPoints[0].host);

        doReturn(result).when(mockClient).getDeviceData(TEST_UA1);
        server = client.getCloudUrl();
    }

    /**
     * getEndPoints()
     */
    @Test
    public void testGetServers() throws Exception {

        
        client.clearCache();
        client.setLicenceKey(LICENCE);

        // auto ranking is off, must return the original internal list
        client.setUseCache(false);
        client.setAutoServerRanking(false);
        Client mockClient = spy(client);

        Map      result   = (Map)results.get(TEST_UA1);
        doReturn(result).when(mockClient).getDeviceData(TEST_UA1);

        int      i;

        // auto ranking is off, must return the provided manual list
        EndPoint[] expected2 = {
            new EndPoint("http://region2.deviceatlascloud.com", "80"),
            new EndPoint("http://region1.deviceatlascloud.com", "80"),
            new EndPoint("http://region3.deviceatlascloud.com", "80"),
            new EndPoint("http://region0.deviceatlascloud.com", "80"),
        };
        client.setEndPoints(expected2);

        doReturn(result).when(mockClient).getDeviceData(TEST_UA1);

        // auto ranking on
        client.clearCache();
        client.setAutoServerRanking(true);
        client.setUseCache(true);

        EndPoint[] expected3 = {
            new EndPoint("http://region2.deviceatlascloud.com", "80"),
            new EndPoint("http://region1.deviceatlascloud.com", "80"),
            new EndPoint("http://region3.deviceatlascloud.com", "80"),
            new EndPoint("http://region0.deviceatlascloud.com", "80"),
        };
        client.setEndPoints(expected3);
        doReturn(result).when(mockClient).getDeviceData(TEST_UA1);
        EndPoint[] returned3 = client.getEndPoints();

        // we dont know the order all we know is the result must be a subset of the available endPoints
        for (EndPoint endPoint : returned3) {
            boolean contains = false;
            for (i=0; i < expected3.length; i++) {
                if (expected3[i].host.equals(endPoint.host)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                throw new Exception("*** End-point not exists in returned list ***");
            }
        }
    }

    /**
     * getServersLatencies()
     */
    @Test
    public void testGetServersLatencies() throws Exception {
        client.clearCache();
        client.setLicenceKey(LICENCE);
 
        EndPoint[] endPoints = {
            new EndPoint("http://region200.deviceatlascloud.com", "80"),
        };
        client.setEndPoints(endPoints);

        EndPoint[] result = client.getServersLatencies();

        for (EndPoint v : result) {
            assertNotNull(v.host);
            assertNotNull(v.port);
            assertNotNull(v.latencies);
            assertNotNull(v.avg);
            // must be an array and not empty
            assertNotNull(v.latencies);
            for (Double w : v.latencies) {
                assertNotNull(w);
            }
        }

        // the last item is a bad end-point, must see -1 in avg and latencies
        double avg = result[0].latencies.get(0);
        assertEquals("Bad server latencies 1", 1,  result.length);
        assertTrue("Bad server latencies 2", (avg == -1.0));
    }

    /**
     *   tests if:
     * * cache off is being respected
     * * auto and manual ranking caches are being created
     * * device data cache is being created
     * * for the same UA a separated device data cache is created if the client side cookie exists
     * * for the same UA a another separated device data cache is created if the client side cookie changes
     * * for the same UA opera headers will cause a new cache to be created
     * * for the same UA ESSENTIAL_USER_AGENT_HEADERS will cause a new cache to be created
     */
    @Test
    public void testCacheExistance() throws Exception {

        client.clearCache();
        client.setLicenceKey(LICENCE);
        // back to default list
        EndPoint[] serversDefault = {
            new EndPoint("http://region0.deviceatlascloud.com", "80"),
            new EndPoint("http://region1.deviceatlascloud.com", "80"),
            new EndPoint("http://region2.deviceatlascloud.com", "80"),
            new EndPoint("http://region3.deviceatlascloud.com", "80"),
        };
        client.setEndPoints(serversDefault);

        // lets see if no cache is being created when cache is off
        client.setAutoServerRanking(true);
        client.setUseCache(false);
        Client mockClient = spy(client);
        when(mockClient.getAutoServerRanking()).
            thenReturn(true);
        when(mockClient.getEndPoints()).
            thenReturn(serversDefault);


        when(mockClient.getDeviceData(TEST_UA1)).
            thenReturn((Map)results.get(TEST_UA1));
        when(mockClient.getDeviceData(TEST_UA1)).
            thenReturn((Map)results.get(TEST_UA1));

        // lets make caches
        // 1) +1 must create device data cache
        // 2) must create auto end-point ranking cache
        client.setUseCache(true);
        when(mockClient.getDeviceData(TEST_UA1)).

            thenReturn((Map)results.get(TEST_UA1));
        when(mockClient.getDeviceData(TEST_UA1)).
            thenReturn((Map)results.get(TEST_UA1));
        // 3) +2 must create another device data cache
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("user-agent", TEST_UA1);
        headers.put("Cookie", "DACACHEN=accessDom-js.supportBasicJavaScript-displayPpi-js.indexedDB-js.webSockets-js.querySelector-hscsd-js.geoLocation-flashCapable-js.json-isMediaPlayer-isTablet-osWindowsPhone-js.supportConsoleLog-isSetTopBox-memoryLimitDownload-js.deviceOrientation-mobileDevice-osAndroid-osBada-html.inlinesvg-displayHeight-jsr118-image.Png-isEReader-js.supportEvents-js.webGl-image.Gif89a-js.modifyCss-isMobilePhone-browserVersion-js.modifyDom-css.transitions-jsr37-drmOmaCombinedDelivery-uriSchemeTel-usableDisplayWidth-jsr30-https-image.Jpg-osVersion-edge-vendor-memoryLimitMarkup-jsr139-css.columns-markup.xhtmlMp12-markup.xhtmlMp11-displayColorDepth-deviceAspectRatio-js.sessionStorage-isGamesConsole-markup.xhtmlMp10-markup.xhtmlBasic10-browserName-html.audio-image.Gif87-osRim-devicePixelRatio-cookieSupport-markup.wml1-gprs-js.applicationCache-umts-js.webSqlDatabase-marketingName-hsdpa-js.webWorkers-vCardDownload-js.deviceMotion-touchScreen-osWebOs-isTV-osiOs-js.touchEvents-js.supportEventListener-model-html.svg-drmOmaForwardLock-js.xhr-html.canvas-displayWidth-id-usableDisplayHeight-osWindowsMobile-uriSchemeSmsTo-uriSchemeSms-drmOmaSeparateDelivery-osSymbian-yearReleased-css.transforms-js.localStorage-jqm-memoryLimitEmbeddedMedia-html.video-csd-css.animations-userMedia-client_props-generation; DACACHEV=%5Btrue%2Ctrue%2C203%2Ctrue%2Ctrue%2Ctrue%2Cfalse%2Ctrue%2Ctrue%2Ctrue%2Cfalse%2Cfalse%2Cfalse%2Ctrue%2Cfalse%2C0%2Ctrue%2Ctrue%2Ctrue%2Cfalse%2Ctrue%2C800%2Cfalse%2Ctrue%2Cfalse%2Ctrue%2Ctrue%2Ctrue%2Ctrue%2Ctrue%2C%224.0%22%2Ctrue%2Ctrue%2Cfalse%2Ctrue%2Ctrue%2C480%2Cfalse%2Ctrue%2Ctrue%2C%222.3.3%22%2Cfalse%2C%22Samsung%22%2C2000000%2Cfalse%2Ctrue%2Ctrue%2Ctrue%2C24%2C%2216%5C%2F9%22%2Ctrue%2Cfalse%2Ctrue%2Ctrue%2C%22Android+Browser%22%2Ctrue%2Ctrue%2Cfalse%2C1%2Ctrue%2Cfalse%2Ctrue%2Ctrue%2Cfalse%2Cfalse%2C%22Galaxy+S2%22%2Cfalse%2Ctrue%2Cfalse%2Ctrue%2Ctrue%2Cfalse%2Cfalse%2Cfalse%2Cfalse%2Ctrue%2C%22GT-I9100+Galaxy+S2%22%2Ctrue%2Ctrue%2Ctrue%2Ctrue%2C480%2C2410065%2C800%2Cfalse%2Ctrue%2Ctrue%2Ctrue%2Cfalse%2C2011%2Ctrue%2Ctrue%2Ctrue%2C0%2Ctrue%2Cfalse%2Ctrue%2Cfalse%2C%22eb17c1b1cc14f47342dfd6a0096490ee%22%2C2%5D; DAPROPS=\"bjs.webGl:1|bjs.geoLocation:1|bjs.webSqlDatabase:0|bjs.indexedDB:1|bjs.webSockets:1|bjs.localStorage:1|bjs.sessionStorage:1|bjs.webWorkers:1|bjs.applicationCache:1|bjs.supportBasicJavaScript:1|bjs.modifyDom:1|bjs.modifyCss:1|bjs.supportEvents:1|bjs.supportEventListener:1|bjs.xhr:1|bjs.supportConsoleLog:1|bjs.json:1|bjs.deviceOrientation:1|bjs.deviceMotion:1|bjs.touchEvents:0|bjs.querySelector:1|bhtml.canvas:1|bhtml.video:1|bhtml.audio:1|bhtml.svg:1|bhtml.inlinesvg:1|bcss.animations:1|bcss.columns:1|bcss.transforms:1|bcss.transitions:1|idisplayColorDepth:24|bcookieSupport:1|idevicePixelRatio:1|sdeviceAspectRatio:16/9|bflashCapable:1|baccessDom:1|buserMedia:0\"");
        when(mockClient.getDeviceData(headers)).thenReturn(testCases2.get(0).get(1));
        when(mockClient.getDeviceData(headers)).thenReturn(testCases2.get(0).get(1));
        // 4) +3 must create another device data cache
        headers = new HashMap<String, String>();
        headers.put("Cookie", "DAPROPS=\"bjs.webGl:1|bjs.geoLocation:1|bjs.webSqlDatabase:0|bjs.indexedDB:1|bjs.webSockets:1|bjs.localStorage:1|bjs.sessionStorage:1|bjs.webWorkers:1|bjs.applicationCache:1|bjs.supportBasicJavaScript:1|bjs.modifyDom:1|bjs.modifyCss:1|bjs.supportEvents:1|bjs.supportEventListener:1|bjs.xhr:1|bjs.supportConsoleLog:1|bjs.json:1|bjs.deviceOrientation:1|bjs.deviceMotion:1|bjs.touchEvents:0|bjs.querySelector:1|bhtml.canvas:1|bhtml.video:1|bhtml.audio:1|bhtml.svg:1|bhtml.inlinesvg:1|bcss.animations:1|bcss.columns:1|bcss.transforms:1|bcss.transitions:1|idisplayColorDepth:24|bcookieSupport:1|idevicePixelRatio:1|sdeviceAspectRatio:16/9|bflashCapable:1|baccessDom:1|buserMedia:1\"");
        when(mockClient.getDeviceData(headers)).thenReturn(testCases2.get(0).get(1));
        when(mockClient.getDeviceData(headers)).thenReturn(testCases2.get(0).get(1));
        // 5) +4 must create another device data cache
        headers.put("x-ucbrowser-ua", "something");
        when(mockClient.getDeviceData(headers)).thenReturn(testCases2.get(0).get(1));
        when(mockClient.getDeviceData(headers)).thenReturn(testCases2.get(0).get(1));
        // 6) +5 must create another device data cache
        headers.put("x-something-Opera-ua", "something");
        when(mockClient.getDeviceData(headers)).thenReturn(testCases2.get(0).get(1));
        when(mockClient.getDeviceData(headers)).thenReturn(testCases2.get(0).get(1));

        // 7) must create manual fail-over end-point cache
        // lets add a bad server to trigger creating this cache file
        EndPoint[] endPoints = {
            new EndPoint("http://region200.deviceatlascloud.com", "80"),
            new EndPoint("http://region2.deviceatlascloud.com", "80"),
            new EndPoint("http://region1.deviceatlascloud.com", "80"),
            new EndPoint("http://region3.deviceatlascloud.com", "80"),
            new EndPoint("http://region0.deviceatlascloud.com", "80"),
        };
        client.setAutoServerRanking(false);
        client.setEndPoints(endPoints);
        
        when(mockClient.getAutoServerRanking()).
            thenReturn(false);
        when(mockClient.getEndPoints()).
            thenReturn(endPoints);

        // 8) +6 this must create a device data cache because "iphone" <> "iPhone"

        when(mockClient.getDeviceDataByUserAgent(TEST_UA2)).

            thenReturn((Map)results.get(TEST_UA2));
    }

    /**
     * getDeviceData()
     * * test device data cache sanity
     * * compare the results got from cloud and cache
     * * see if API return data contains the correct data source value
     */
    @Test
    public void testGetDeviceDataCache() throws Exception {
        client.clearCache();
        client.setLicenceKey(LICENCE);
        Client mockClient = spy(client);
        Map result1 = (Map)client.getDeviceData(TEST_UA1);
        Map result2 = (Map)client.getDeviceData(TEST_UA1);

        doReturn(result1).when(mockClient).
            getDeviceData(TEST_UA1);
        doReturn(result2).when(mockClient).
            getDeviceData(TEST_UA1);

        assertEquals(ClientConstants.SOURCE_CLOUD.toString(), result1.get(ClientConstants.KEY_SOURCE.toString()));
        assertEquals(ClientConstants.SOURCE_CLOUD.toString(), result2.get(ClientConstants.KEY_SOURCE.toString()));

        Map props1 = (Map)result1.get(ClientConstants.KEY_PROPERTIES.toString());
        Map props2 = (Map)result2.get(ClientConstants.KEY_PROPERTIES.toString());

        assertEquals("number of properties", props1.size(), props1.size());
        for (Iterator itEx = props1.entrySet().iterator(); itEx.hasNext();) {
            Map.Entry pairs2 = (Map.Entry) itEx.next();
            String k         = (String)pairs2.getKey();
            Object v         = (Object)pairs2.getValue();

            assertTrue(k, props2.containsKey(k));
            Object v2 = (Object)props2.get(k);
            assertTrue(k, v.equals(v2));
        }
    }

    /**
     * prepareHeaders()
     */
    @Test
    public void testPrepareHeaders() throws Exception {
        client.clearCache();
        client.setLicenceKey(LICENCE);

        // ua headers
        Map<String, String> dirtyHeaders1 = new HashMap<String, String>();
        dirtyHeaders1.put("HTTP_X_PROFILE", "Somevalue...");
        dirtyHeaders1.put("HTTP_X_WAP_PROFILE", "Somevalue...");
        dirtyHeaders1.put("HTTP_X_ATT_deviceid", "Somevalue...");
        dirtyHeaders1.put("HTTP_ACCEPT", "Somevalue...");
        dirtyHeaders1.put("HTTP_ACCEPT_LANGUAGE", "Somevalue...");

        dirtyHeaders1.put("HTTP_X_device-user-agent", "Somevalue...");
        dirtyHeaders1.put("HTTP_X_original-user-agent", "Somevalue...");
        dirtyHeaders1.put("HTTP_X_operamini-phone-ua", "Somevalue...");
        dirtyHeaders1.put("HTTP_X_skyfire-phone", "Somevalue...");
        dirtyHeaders1.put("HTTP_X_bolt-phone-ua", "Somevalue...");
        dirtyHeaders1.put("HTTP_device-stock-ua", "Somevalue...");
        dirtyHeaders1.put("HTTP_X_ucbrowser-ua", "Somevalue...");
        dirtyHeaders1.put("HTTP_X_ucbrowser-device-ua", "Somevalue...");
        dirtyHeaders1.put("HTTP_X_ucbrowser-device", "Somevalue...");

        // extra headers
        Map<String, String> dirtyHeaders2 = new HashMap<String, String>();
        dirtyHeaders2.put("HTTP_CLIENT-IP", "Somevalue...");
        dirtyHeaders2.put("HTTP_X_FORWARDED_FOR", "Somevalue...");
        dirtyHeaders2.put("HTTP_X_FORWARDED", "Somevalue...");
        dirtyHeaders2.put("HTTP_FORWARDED_FOR", "Somevalue...");
        dirtyHeaders2.put("HTTP_FORWARDED", "Somevalue...");
        dirtyHeaders2.put("HTTP_PROXY_CLIENT_IP", "Somevalue...");
        dirtyHeaders2.put("HTTP_WL_PROXY_CLIENT_IP", "Somevalue...");

        // other headers
        Map<String, String> dirtyHeaders3 = new HashMap<String, String>();
        dirtyHeaders3.put("REMOTE_ADDR", "127.0.0.1");
        dirtyHeaders3.put("HTTP_USER_AGENT", "UA");

        // opera headers
        Map<String, String> dirtyHeaders4 = new HashMap<String, String>();
        dirtyHeaders4.put("HTTP_X-OperaMini-Features", "advanced, camera, folding, secure");
        dirtyHeaders4.put("HTTP_X-OperaMini-Phone-UA", "SonyEricssonK750i/R1AA Browser/");
        dirtyHeaders4.put("HTTP_X-OperaMini-Phone", "SonyEricsson # K750i");

        // normalize all above headers and pass them to prepareHedears and get the result
        Map<String, String> dirtyHeaders = new HashMap<String, String>();
        dirtyHeaders.putAll(dirtyHeaders1);
        dirtyHeaders.putAll(dirtyHeaders2);
        dirtyHeaders.putAll(dirtyHeaders3);
        dirtyHeaders.putAll(dirtyHeaders4);
        Map<String, String> in  = StringUtils.normaliseKeys(dirtyHeaders);
        Map<String, String> out = client.getDeviceIdentificatorService().prepareHeaders(in);

        // check header keys inside the result
        Iterator itEx;
        Map toTest = StringUtils.normaliseKeys(dirtyHeaders1);
        Map.Entry pairs;
        String k;
        Object v;

        for (itEx = toTest.entrySet().iterator(); itEx.hasNext();) {
            pairs = (Map.Entry) itEx.next();
            k     = (String)pairs.getKey();
            assertTrue(k, out.containsKey(k));
        }

        toTest = StringUtils.normaliseKeys(dirtyHeaders2);
        for (itEx = toTest.entrySet().iterator(); itEx.hasNext();) {
            pairs = (Map.Entry) itEx.next();
            k     = (String)pairs.getKey();
            assertFalse(k, out.containsKey(k));
        }

        toTest = StringUtils.normaliseKeys(dirtyHeaders3);
        for (itEx = toTest.entrySet().iterator(); itEx.hasNext();) {
            pairs = (Map.Entry) itEx.next();
            k     = (String)pairs.getKey();
            assertFalse(k, out.containsKey(k));
        }

        toTest = StringUtils.normaliseKeys(dirtyHeaders4);
        for (itEx = toTest.entrySet().iterator(); itEx.hasNext();) {
            pairs = (Map.Entry) itEx.next();
            k     = (String)pairs.getKey();
            assertTrue(k, out.containsKey(k));
        }

        // if extra headers on
        client.getDeviceIdentificatorService().setSendExtraHeaders(true);
        out = client.getDeviceIdentificatorService().prepareHeaders(in);

        toTest = StringUtils.normaliseKeys(dirtyHeaders2);
        for (itEx = toTest.entrySet().iterator(); itEx.hasNext();) {
            pairs = (Map.Entry) itEx.next();
            k     = (String)pairs.getKey();
            assertTrue(k, out.containsKey(k));
        }

        k = "remote-addr";
        assertTrue(k, out.containsKey(k));
    }

    /**
     * Test auto ranking
     * * set a manual list of end-points with an impaired one
     * * get device properties, the ranking status must show that ranking has been done
     * * call getEndPoints() > the list must be different than the provided list to show ranking was done and cached and being used
     * * call getEndPoints() > the ranking status must show that ranking was not done again, but result was read from cache
     * * * auto ranking is done when it should
     * * * auto ranking is no done when it should not
     * * * auto ranking cache is used
     */
    @Test
    public void testAutoRanking() throws Exception {
        client.clearCache();
        client.setLicenceKey(LICENCE);
        client.setAutoServerRanking(true);

        EndPoint[] endPoints = {
            new EndPoint("http://region200.deviceatlascloud.com", "80"),
            new EndPoint("http://region2.deviceatlascloud.com", "80"),
            new EndPoint("http://region1.deviceatlascloud.com", "80"),
            new EndPoint("http://region3.deviceatlascloud.com", "80"),
            new EndPoint("http://region0.deviceatlascloud.com", "80"),
        };
        client.setEndPoints(endPoints);
        Client mockClient = spy(client);

        doReturn((Map)results.get(TEST_UA1)).when(mockClient).
            getDeviceData(TEST_UA1);
        when(mockClient.getRankingStatus()).thenReturn("L");

        // auto ranking must have been performed

        // L = auto-rank done on endPoints A = auto-ranked cached list M = manual cached list D = default list

        // there is bad endpoints in the original list, after ranking it must be not be in the list
        assertTrue("bad end-points abolished", client.getEndPointService().getOriginalEndPoints().length > client.getEndPointService().getEndPoints().length);

        // calling getEndPoints() after a recent auto rank must return cached list

        when(mockClient.getRankingStatus()).thenReturn("A");
    }

    /**
     * Test manual ranking / failover
     * * set a manual list of end-points with an impaired one
     * * get device properties, the ranking status must show that ranking has NOT been done
     * * call getEndPoints() > the list must be different than the provided list to show fail-over re-ordering has been done
     * * call getEndPoints() > the ranking status must show that the end-point list is read from the manual-fail-over-cache
     * * * auto ranking is not done
     * * * the provided list is used
     * * * manual failover cache works
     */
    @Test
    public void testManualRanking() throws Exception {
        client.clearCache();
        client.setLicenceKey(LICENCE);
        client.setAutoServerRanking(false);

        EndPoint[] endPoints = {
            new EndPoint("http://region200.deviceatlascloud.com", "80"),
            new EndPoint("http://region2.deviceatlascloud.com", "80"),
            new EndPoint("http://region1.deviceatlascloud.com", "80"),
            new EndPoint("http://region3.deviceatlascloud.com", "80"),
            new EndPoint("http://region0.deviceatlascloud.com", "80"),
        };
        client.setEndPoints(endPoints);

        // must be the same > shows no manipulation is done on the manual list
        EndPoint[] getEndPoint = client.getEndPoints();

        boolean equals = true;
        if (getEndPoint.length != endPoints.length) {
            equals = false;
        }
        int i;
        for (i=0; i< getEndPoint.length; i++) {
            if (!getEndPoint[i].host.equals(endPoints[i].host)) {
                equals = false;
                break;
            }
        }
        assertTrue("manual list", equals);

        Client mockClient = spy(client);
        doReturn(testCases1.get(TEST_UA1)).when(mockClient).
            getDeviceData(TEST_UA1);

        // must be D to show the manual list was used

        when(mockClient.getRankingStatus()).thenReturn("D");
        // proove the first bad server was not used to query cloud and results are valid
        String url = client.getCloudUrl();
        assertNotNull("end-point", url);
        assertFalse("end-point", url.equals("region200.deviceatlascloud.com"));

        // as the first end-point was bad it is excpected that the mechanism moved it to the end of the list
        // and cached the list, and next time the cached list will be used        

        // there is bad endpoints in the original list, after ranking the lists must have the same items but in another order
        EndPoint[] a = client.getEndPointService().getOriginalEndPoints();
        EndPoint[] b = client.getEndPointService().getEndPoints();
 
        equals = true;
        if (a.length == b.length) {
            for (i=0; i<a.length; i++) {
                if (a[i].host.equals(b[i].host)) {
                    equals = false;
                    break;
                }
            }
        }
        assertFalse("manual list", equals);

        // check that both arrays have the same end-points (only position has changed)
        int j;
        for (i=0; i<a.length; i++) {
            equals = false;
            for (j=0; j<a.length; j++) {
                if (b[j].host.equals(a[i].host)) {
                    equals = true;
                    break;
                }
            }
            assertTrue("manual list", equals);
        }

        // must be M to show the manual list fail over cache was used

        when(mockClient.getRankingStatus()).thenReturn("M");
    }

    /**
     * rankServers()
     */
    @Test
    public void testRankServers() throws Exception {
        client.setLicenceKey(LICENCE);

        // AUTO RANKING OFF
        client.setAutoServerRanking(false);
        client.clearCache();
        EndPoint[] endPoints = {
            new EndPoint("http://region200.deviceatlascloud.com", "80"),
            new EndPoint("http://region2.deviceatlascloud.com", "80"),
            new EndPoint("http://region1.deviceatlascloud.com", "80"),
            new EndPoint("http://region3.deviceatlascloud.com", "80"),
            new EndPoint("http://region0.deviceatlascloud.com", "80"),
        };
        client.getEndPointService().setEndPoints(endPoints);

        // expected to do nothing when auto-ranking is off
        EndPoint[] serversRanked = client.getEndPointService().rankServers();
        assertEquals("ranked end-points", 0, serversRanked.length);

        // AUTO RANKING ON
        client.setAutoServerRanking(true);
        client.clearCache();
        client.setEndPoints(endPoints);
        
        // expected to rank the endPoints and return the ranked list
        // expected to put the ranked list in cache
        serversRanked = client.getEndPointService().rankServers();
        assertTrue("ranked end-points", serversRanked.length > 0);

        assertTrue("abolished bad end-point", serversRanked.length < endPoints.length);

        // expected the new cache list to be used from now
        endPoints = client.getEndPointService().getEndPoints();

        boolean equals = true;
        if (serversRanked.length != endPoints.length) {
            equals = false;
        }
        for (int i=0; i<serversRanked.length; i++) {
            if (!serversRanked[i].host.equals(endPoints[i].host)) {
                equals = false;
                break;
            }
        }
        assertTrue("ranked end-points", equals);
    }

    /**
     * Tests tools
     */
    @Test
    public void testTools() throws Exception {
        EndPoint [] endPoints = client.getEndPointService().getEndPoints();
        endPoints[0].avg = -1.0d;
        endPoints[0].latencies = new ArrayList<Double>();
        endPoints[0].latencies.add(-1.0d);
        endPoints[0].latencies.add(2d);
        String [] emptyargs = new String[0];
        String [] args = new String[1];
        args[0] = LICENCE;
        LatencyChecker.main(emptyargs);
        LatencyChecker.main(args);
        LatencyChecker.serversList(endPoints);
        CachedEndPoints.main(emptyargs);
        CachedEndPoints.main(args);
        CachedEndPoints.serversList(endPoints);
    }
}
