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
import com.deviceatlas.cloud.deviceidentification.client.ClientException;
import com.deviceatlas.cloud.deviceidentification.client.ClientConstants;
import com.deviceatlas.cloud.deviceidentification.client.Result;
import com.deviceatlas.cloud.deviceidentification.client.HeaderConstants;
import com.deviceatlas.cloud.deviceidentification.client.Properties;
import com.deviceatlas.cloud.deviceidentification.utils.StringUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceIdentificatorService {
    private EndPointService endPointService;
    private String licenceKey                                           = null;
    private boolean useClientCookie                                     = true;
    private boolean sendExtraHeaders                                    = false;

    /// A list of http-headers to be sent to the DeviceAtlas Cloud. This headers
    /// are used for device detection, specially if a third party browser or a proxy
    /// changes the original user-agent.
    private static final String[] ESSENTIAL_HEADERS = {
        "x-profile",
        "x-wap-profile",
        "x-att-deviceid",
        "accept",
        "accept-language",
    };
    
    // A list of http-headers which may contain the original user-agent.
    // this headers are sent to DeviceAtlas Cloud beside.
    private static final String[] ESSENTIAL_USER_AGENT_HEADERS = {
        "x-device-user-agent",
        "x-original-user-agent",
        "x-operamini-phone-ua",
        "x-skyfire-phone",
        "x-bolt-phone-ua",
        "device-stock-ua",
        "x-ucbrowser-ua",
        "x-ucbrowser-device-ua",
        "x-ucbrowser-device",
        "x-puffin-ua",
    };

    // An array of additional http-headers to be sent to the DeviceAtlas Cloud.
    // This headers are not sent by default. This headers can be used for
    // carrier detection and geoip.
    private static final String[] EXTRA_HEADERS = {
        "client-ip",
        "x-forwarded-for",
        "x-forwarded",
        "forwarded-for",
        "forwarded",
        "proxy-client-ip",
        "wl-proxy-client-ip",
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceIdentificatorService.class);

    public DeviceIdentificatorService(EndPointService endPointService) {
        this.endPointService = endPointService;
    }

    /**
     * Gets the licence key in the identificator's level
     *
     * @return String
     */

    public String getLicenceKey() {
        return licenceKey;
    }

    /**
     * Sets the licence key in the identificator's level
     *
     * @param licenceKey
     */

    public void setLicenceKey(String licenceKey) {
        this.licenceKey = licenceKey;
        this.endPointService.setLicenceKey(this.licenceKey);
    }

    /**
     * Getter for the useClientCookie setting. Defaults to true. If TRUE then if
     * device data which is created by the DeviceAtlas client side component
     * (JS library) exists it will be used
     *
     * @return The sendExtraHeaders setting. Defaults to false
     */
    public boolean getUseClientCookie() {
        return useClientCookie;
    }

    /**
     * Setter for the useClientCookie setting. Defaults to true. If TRUE then if
     * device data which is created by the DeviceAtlas client side component
     * (JS library) exists it will be used
     *
     * @param useClientCookie true = use device data which is created by the
     * DeviceAtlas client side component if exists
     */
    public void setUseClientCookie(boolean useClientCookie) {
        this.useClientCookie = useClientCookie;
    }

    /**
     * Getter for the sendExtraHeaders setting. Defaults to false. If this
     * TRUE then extra client headers are sent with each request to the
     * service. If this is FALSE then only select headers essential for
     * detection are sent.
     *
     * @return The sendExtraHeaders setting. Defaults to false.
     */
    public boolean getSendExtraHeaders() {
        return sendExtraHeaders;
    }

    /**
     * Setter for the sendExtraHeaders setting. Defaults to false. If this
     * TRUE then extra client headers are sent with each request to the
     * service. If this is FALSE then only select headers essential for
     * detection are sent.
     *
     * @param sendExtraHeaders TRUE if to send extra headers, FALSE to just
     * send essential headers.
     */
    public void setSendExtraHeaders(boolean sendExtraHeaders) {
        this.sendExtraHeaders = sendExtraHeaders;
    }

    /**
     * Get device data from DeviceAtlas Cloud service.
     * If using cache is not turned off the device data will be cached after each
     * cloud call, if cached data exists for a device it will be used over the cloud.
     * If device data provided by "DeviceAtlas Client Side Component" exists in
     * a cookie then cloud data will be merged with the cookie data.
     *
     * @param request The HttpServletRequest request object
     * @return A Map of device data as {
     *      Client.KEY_USERAGENT: "UA",
     *      Client.KEY_SOURCE: "data source",
     *      Client.KEY_PROPERTIES: {"propertyName": "PropertyVal",},
     * } Note that Client.KEY_PROPERTIES will not exists for not detectable data
     * and on failures.
     * @throws ClientException When any part of detection or the API fails.
     * @deprecated
     */
    @Deprecated
    public Map getDeviceData(HttpServletRequest request) throws ClientException {
        Map<String, String> headers = prepareHeadersForServletRequest(request);

        return getDeviceDataByHeaders(headers);
    }

    /**
     * Get device data from DeviceAtlas Cloud service.
     * If using cache is not turned off the device data will be cached after each
     * cloud call, if cached data exists for a device it will be used over the cloud.
     * NOTE: it is recommended to use getDeviceDataByUserAgent() to get device data
     * when passing a user-agent string. Because "getDeviceData(String)" is
     * an overload for "getDeviceData(HttpServletRequest)" so the JavaEE lib would be
     * required when compiling your project. But If you use
     * "getDeviceDataByUserAgent(String)" instead, the JavaEE will not be required.
     *
     * @param userAgent User-agent string
     * @return A Map of device data as {
     *      Client.KEY_USERAGENT: "UA",
     *      Client.KEY_SOURCE: "data source",
     *      Client.KEY_PROPERTIES: {"propertyName": "PropertyVal",},
     * } Note that Client.KEY_PROPERTIES will not exists for not detectable data
     * and on failures.
     * @throws ClientException When any part of detection or the API fails.
     * @deprecated
     */
    @Deprecated
    public Map getDeviceData(String userAgent) throws ClientException {
        return getDeviceDataByUserAgent(userAgent);
    }

    /**
     * Get device data from DeviceAtlas Cloud service.
     * If using cache is not turned off the device data will be cached after each
     * cloud call, if cached data exists for a device it will be used over the cloud.
     * NOTE: it is recommended to use getDeviceDataByHeaders() to get device data
     * when passing a Map of headers. Because {@code getDeviceData(Map<String, String>)}
     * is an overload for "getDeviceData(HttpServletRequest)" the JavaEE lib would be
     * required when compiling your project. But if you use
     * {@code getDeviceDataByHeaders(Map<String, String>)} instead, the JavaEE will not be required.
     *
     * @param headers A Map of http headers {"header-name": "header-value",}
     * @return A Map of device data as {
     *      Client.KEY_USERAGENT: "UA",
     *      Client.KEY_SOURCE: "data source",
     *      Client.KEY_PROPERTIES: {"propertyName": "PropertyVal",},
     * } Note that Client.KEY_PROPERTIES will not exists for not detectable data
     * and on failures.
     * @throws ClientException When any part of detection or the API fails.
     * @deprecated
     */
    @Deprecated
    public Map getDeviceData(Map<String, String> headers) throws ClientException {
        return getDeviceDataByHeaders(headers);
    }

    /**
     * Get device data from DeviceAtlas Cloud service.
     * If using cache is not turned off the device data will be cached after each
     * cloud call, if cached data exists for a device it will be used over the cloud.
     *
     * @param userAgent User-agent string
     * @return A Map of device data as {
     *      Client.KEY_USERAGENT: "UA",
     *      Client.KEY_SOURCE: "data source",
     *      Client.KEY_PROPERTIES: {"propertyName": "PropertyVal",},
     * } Note that Client.KEY_PROPERTIES will not exists for not detectable data
     * and on failures.
     * @throws ClientException When any part of detection or the API fails.
     * @deprecated
     */
    @Deprecated
    public Map getDeviceDataByUserAgent(String userAgent) throws ClientException {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("user-agent", userAgent);
        return getDeviceDataByHeaders(headers);
    }

    /**
     * Get device data from DeviceAtlas Cloud service.
     * If using cache is not turned off the device data will be cached after each
     * cloud call, if cached data exists for a device it will be used over the cloud.
     *
     * @param headers A Map of http headers {"header-name": "header-value",}
     * @return A Map of device data as {
     *      Client.KEY_USERAGENT: "UA",
     *      Client.KEY_SOURCE: "data source",
     *      Client.KEY_PROPERTIES: {"propertyName": "PropertyVal",},
     * } Note that Client.KEY_PROPERTIES will not exists for not detectable data
     * and on failures.
     * 
     * @throws ClientException When any part of detection or the API fails.
     * @deprecated
     */
    @Deprecated
    public Map getDeviceDataByHeaders(Map<String, String> headers) throws ClientException {
        String userAgent;
        String cookie;
        Map<String, String> tmpHeaders;
        Map<String, Object> dataRet = prepareDataHeaders(headers);
        userAgent = (String)dataRet.get(HeaderConstants.UA_HEADER.toString());
        cookie = (String)dataRet.get(HeaderConstants.COOKIE_HEADER.toString());
        tmpHeaders = (Map<String,String>)dataRet.get(ClientConstants.CLOUD_SERVICE_RESULT.toString());
        Map <String, Object> results   = new HashMap <String, Object>();
        results.put(ClientConstants.KEY_USERAGENT.toString(), userAgent);

        try {

            setCacheData(results, userAgent, cookie, tmpHeaders);

        } catch (ClientException ex) {
            throw new ClientException(
                    "There was a problem getting/setting the device properties: \"" + ex.getMessage() + "\"",
                    ex
                    );
        }

        return results;
    }

    /**
     * Get device data from DeviceAtlas Cloud service.
     * If using cache is not turned off the device data will be cached after each
     * cloud call, if cached data exists for a device it will be used over the cloud.
     * If device data provided by "DeviceAtlas Client Side Component" exists in
     * a cookie then cloud data will be merged with the cookie data.
     *
     * @param request The HttpServletRequest request object
     * @return Result
     * @throws ClientException When any part of detection or the API fails.
     */
    public Result getResult(HttpServletRequest request) throws ClientException {
        Map<String, String> headers = prepareHeadersForServletRequest(request);

        return getResultByHeaders(headers);
    }

    /**
     * Get device data from DeviceAtlas Cloud service.
     * If using cache is not turned off the device data will be cached after each
     * cloud call, if cached data exists for a device it will be used over the cloud.
     * NOTE: it is recommended to use getDeviceDataByUserAgent() to get device data
     * when passing a user-agent string. Because "getDeviceData(String)" is
     * an overload for "getDeviceData(HttpServletRequest)" so the JavaEE lib would be
     * required when compiling your project. But If you use
     * "getDeviceDataByUserAgent(String)" instead, the JavaEE will not be required.
     *
     * @param userAgent User-agent string
     * @return Result
     * @throws ClientException When any part of detection or the API fails.
     */
    public Result getResult(String userAgent) throws ClientException {
        return getResultByUserAgent(userAgent);
    }

    /**
     * Get device data from DeviceAtlas Cloud service.
     * If using cache is not turned off the device data will be cached after each
     * cloud call, if cached data exists for a device it will be used over the cloud.
     * NOTE: it is recommended to use getDeviceDataByHeaders() to get device data
     * when passing a Map of headers. Because {@code getDeviceData(Map<String, String>)}
     * is an overload for "getDeviceData(HttpServletRequest)" the JavaEE lib would be
     * required when compiling your project. But if you use
     * {@code getDeviceDataByHeaders(Map<String, String>)} instead, the JavaEE will not be required.
     *
     * @param headers A Map of http headers {"header-name": "header-value",}
     * @return Result
     * @throws ClientException When any part of detection or the API fails.
     */
    public Result getResult(Map<String, String> headers) throws ClientException {
        return getResultByHeaders(headers);
    }

    /**
     * Get device data from DeviceAtlas Cloud service.
     * If using cache is not turned off the device data will be cached after each
     * cloud call, if cached data exists for a device it will be used over the cloud.
     *
     * @param userAgent User-agent string
     * @return Result
     * @throws ClientException When any part of detection or the API fails.
     */
    public Result getResultByUserAgent(String userAgent) throws ClientException {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("user-agent", userAgent);
        return getResultByHeaders(headers);
    }

    /**
     * Get device data from DeviceAtlas Cloud service.
     * If using cache is not turned off the device data will be cached after each
     * cloud call, if cached data exists for a device it will be used over the cloud.
     *
     * @param headers A Map of http headers {"header-name": "header-value",}
     * @return A Map of device data as {
     *      Client.KEY_USERAGENT: "UA",
     *      Client.KEY_SOURCE: "data source",
     *      Client.KEY_PROPERTIES: {"propertyName": "PropertyVal",},
     * } Note that Client.KEY_PROPERTIES will not exists for not detectable data
     * and on failures.
     * 
     * @throws ClientException When any part of detection or the API fails.
     */
    public Result getResultByHeaders(Map<String, String> headers) throws ClientException {
        String userAgent;
        String cookie;
        Map<String, String> tmpHeaders;
        Map<String, Object> dataRet = prepareDataHeaders(headers);
        userAgent = (String)dataRet.get(HeaderConstants.UA_HEADER.toString());
        cookie = (String)dataRet.get(HeaderConstants.COOKIE_HEADER.toString());
        tmpHeaders = (Map<String,String>)dataRet.get(ClientConstants.CLOUD_SERVICE_RESULT.toString());
        Result eResponse = new Result();
        eResponse.setHeaders(headers);

        try {

            setCacheData(eResponse, userAgent, cookie, tmpHeaders);
        } catch (ClientException ex) {
            throw new ClientException(
                    "There was a problem getting/setting the device properties: \"" + ex.getMessage() + "\"",
                    ex
                    );
        }

        return eResponse;
    }


    /**
     * Treats the headers from a servlet request
     *
     * @param request
     * @return Map
     */
    public Map<String, String> prepareHeadersForServletRequest(HttpServletRequest request) {
        // this is the only spot to use JavaEE > > >

        Map<String, String> headers   = new HashMap<String, String>();
        Enumeration e = request.getHeaderNames();
        Cookie [] cookies;
        while (e.hasMoreElements()) {
            String header    = (String)e.nextElement();
            String headerVal = request.getHeader(header);
            headers.put(header, headerVal);
        }

        if (useClientCookie && (cookies = request.getCookies()) != null) {
            for (int i = 0, l = cookies.length; i < l; i++) {
                Cookie cookie = cookies[i];
                if (cookie.getName().equals(ClientConstants.CLIENT_COOKIE_NAME.toString())) {
                    headers.put(ClientConstants.CLIENT_COOKIE_NAME.toString(), cookie.getValue());
                }
            }
        }

        headers.put(HeaderConstants.REMOTE_ADDR.toString(), request.getRemoteAddr());

        // < < < JavaEE
    
        return headers;
    }

    /**
     * Treats the various headers, creates the user agent and cookie
     *
     * @param headers
     * @return Map
     */

    public Map<String, Object> prepareDataHeaders(Map<String, String> headers) {
        Map<String, String> tmpHeaders;
        Map<String, Object> dataRet = new HashMap<String, Object>();
        endPointService.setCloudUrl(null);
        String userAgent;
        String cookie = null;
        endPointService.setRankingStatus(null);

        // get user agent
        tmpHeaders = StringUtils.normaliseKeys(headers);
        userAgent = tmpHeaders.get(HeaderConstants.UA_HEADER.toString());
        if (userAgent == null) {
            userAgent = "";
        }

        cookie = extractCookieValue(tmpHeaders);
        // fetch device data
        endPointService.setCalledServers(new ArrayList<String>());
        dataRet.put(HeaderConstants.UA_HEADER.toString(), userAgent);
        dataRet.put(HeaderConstants.COOKIE_HEADER.toString(), cookie);
        dataRet.put(ClientConstants.CLOUD_SERVICE_RESULT.toString(), headers);
        return dataRet;
    }

    /**
     * Returns the DA cookie value from the headers list
     *
     * @param headers
     * @return cookie
     */

    public String extractCookieValue(Map<String, String> headers) {
        String cookie = null;
        String rawCookies;
        Boolean ableToGetHeaders = useClientCookie && headers != null;
        String clientCookieName = ClientConstants.CLIENT_COOKIE_NAME.toString();
        // get client side component cookie value
        if (ableToGetHeaders &&
                (cookie = headers.get(clientCookieName)) != null) {
            return cookie;
        }

        if (ableToGetHeaders &&
                (rawCookies = headers.get(HeaderConstants.COOKIE_HEADER.toString())) != null) {
            String[] cookies = rawCookies.split(";");
            for (String c : cookies) {
                c = c.trim();
                if (c.startsWith(clientCookieName + "=")) {
                    return c.replace(clientCookieName + "=", "");
                }
            }
        }

        return null;
    }

    /**
     * Sets the cloud service data to the cache's layer
     *
     * @param results
     * @param userAgent
     * @param cookie
     * @param headers
     */

    public void setCacheData(Object results, String userAgent, String cookie, Map<String, String> headers) throws ClientException {
        String cacheKey = getCacheKey(userAgent, cookie, headers);
        String source    = ClientConstants.SOURCE_NONE.toString();
        Map<String, Object> data        = null;

        try {
            if (endPointService.getCacheService().getUseCache() && (data = endPointService.getCacheService().getCacheProvider().get(cacheKey)) != null) {
                source = ClientConstants.SOURCE_CACHE.toString();
            }

            if (data == null) {
                headers = prepareHeaders(headers);
                // add the client side component cookie
                if (cookie != null) {
                    headers.put(HeaderConstants.CLIENT_COOKIE_HEADER.toString(), cookie);
                }
                data   = endPointService.getCloudService(userAgent, headers);
                source = ClientConstants.SOURCE_CLOUD.toString();
                // put device data into cache

                if (endPointService.getCacheService().getUseCache()) {
                    endPointService.getCacheService().getCacheProvider().set(cacheKey, data);
                }
            }
        } catch (CacheException ex) {
            LOGGER.error("setCacheData", ex);
        }

        if (results instanceof Map) {
            ((Map<String, Object>)results).put(ClientConstants.KEY_SOURCE.toString(), source);
            if (data != null) {
                ((Map<String, Object>)results).put(ClientConstants.KEY_PROPERTIES.toString(), data);
            }
        } else if (results instanceof Result)  {
            ((Result)results).setSource(source);
            if (data != null) {
                Properties properties = new Properties();
                properties.putMap(data);
                ((Result)results).setProperties(properties);
            }
        } else {
            throw new ClientException("Invalid results type");
        }
    }

    /**
     * Get a key for caching device data
     */
    private String getCacheKey(String userAgent, String cookie, Map<String, String> headers) throws ClientException {
        StringBuilder sb = new StringBuilder(userAgent);
        if (headers != null) {
            // cache key - combination of user agent and JS created cookie
            for (byte i=0; i < ESSENTIAL_USER_AGENT_HEADERS.length; i++) {
                String val = headers.get(ESSENTIAL_USER_AGENT_HEADERS[i]);
                if (val != null) {
                    sb.append(val);
                }
            }
            // opera headers
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String header = entry.getKey();
                String val    = entry.getValue();
                if (header.contains(ClientConstants.OPERA_HEADER_IDENTIFIER.toString())) {
                    sb.append(val);
                }
            }
        }
        if (cookie != null) {
            sb.append(cookie);
        }

        return StringUtils.md5(sb.toString());
    }

    /**
     * Treats the Essential headers
     *
     * @param headers
     * @param newHeaders
     */

    public void prepareEssentialHeaders(Map<String, String> headers, Map<String, String> newHeaders) {
        // add essential headers which are useful for device detection
        for (byte i=0; i < ESSENTIAL_HEADERS.length; i++) {
            String headerVal = headers.get(ESSENTIAL_HEADERS[i]);
            if (headerVal != null) {
                newHeaders.put(ESSENTIAL_HEADERS[i], headerVal);
            }
        }
    }

    /**
     * Treats the Extra user agent headers
     *
     * @param headers
     * @param newHeaders
     */


    public void prepareEssentialUserAgentHeaders(Map<String, String> headers, Map<String, String> newHeaders) {
        // add special headers these can contain device info
        for (byte i=0; i < ESSENTIAL_USER_AGENT_HEADERS.length; i++) {
            String headerVal = headers.get(ESSENTIAL_USER_AGENT_HEADERS[i]);
            if (headerVal != null) {
                newHeaders.put(ESSENTIAL_USER_AGENT_HEADERS[i], headerVal);
            }
        }
    }

    /**
     * Treats the Opera identifier header
     *
     * @param headers
     * @param newHeaders
     */

    public void prepareOperaHeader(Map<String, String> headers, Map<String, String> newHeaders) {
        // opera headers
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String header = entry.getKey();
            if (header.contains(ClientConstants.OPERA_HEADER_IDENTIFIER.toString())) {
                newHeaders.put(header, entry.getValue());
            }
        }
    }

    /**
     * Treats the extra headers
     *
     * @param headers
     * @param newHeaders
     */

    public void prepareExtraHeaders(Map<String, String> headers, Map<String, String> newHeaders) {
        // add optional headers
        if (sendExtraHeaders) {
            for (byte i=0; i < EXTRA_HEADERS.length; i++) {
                String headerVal = headers.get(EXTRA_HEADERS[i]);
                if (headerVal != null) {
                    newHeaders.put(EXTRA_HEADERS[i], headerVal);
                }
            }
            // add the remote IP address
            String remoteAddrVal = headers.get(HeaderConstants.REMOTE_ADDR.toString());
            if (remoteAddrVal != null)
            {
                newHeaders.put(HeaderConstants.REMOTE_ADDR.toString(), remoteAddrVal);
            }
        }
    }

    /**
     * Extract headers to be send to the cloud service from a header Map object.
     * @param headers headers
     * @return Map
     */
    public Map<String, String> prepareHeaders(Map<String, String> headers) {
        Map<String, String> newHeaders = new HashMap<String, String>();
        prepareEssentialHeaders(headers, newHeaders);
        prepareEssentialUserAgentHeaders(headers, newHeaders);
        prepareOperaHeader(headers, newHeaders);
        prepareExtraHeaders(headers, newHeaders);

        return newHeaders;
    }
}
