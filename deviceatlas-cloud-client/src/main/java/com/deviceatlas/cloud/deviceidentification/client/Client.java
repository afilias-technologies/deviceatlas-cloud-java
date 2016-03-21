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

import com.deviceatlas.cloud.deviceidentification.endpoint.EndPoint;
import com.deviceatlas.cloud.deviceidentification.cacheprovider.CacheException;
import com.deviceatlas.cloud.deviceidentification.cacheprovider.CacheProvider;
import com.deviceatlas.cloud.deviceidentification.service.CacheService;
import com.deviceatlas.cloud.deviceidentification.service.DeviceIdentificatorService;
import com.deviceatlas.cloud.deviceidentification.service.EndPointService;

import javax.servlet.http.HttpServletRequest;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DeviceAtlas Cloud API.
 * 
 * This client library can be used to easily get device data from the
 * DeviceAtlas Cloud service. To reduce cloud calls and improve performance
 * the API locally caches the data returned from the cloud service. This API
 * caches data using the ehcache library (http://ehcache.org/). The ehcache
 * configurations are placed inside /config/deviceatlas_ehcache.xml, this file
 * must always be added into the class path (even if you don't want to cache
 * device data).
 * The client is queried by passing either an HttpServletRequest object or a
 * collection of HTTP headers to it or a user-agent string. The device
 * properties will then be returned.
 * <br><br><b>Example 1:</b><br>Using the Request object from the app container.
 * <pre>
 * // HttpServletRequest request: Request object from Servlet container
 * first use
 * Client client = Client.getInstance(CacheProvider);
 * following use
 * Client client = Client.getInstance();
 * client.setLicenceKey("DeviceAtlas-licence-key");
 * try {
 *     Result results = client.getResult(request);
 * } catch (Exception x) { // handle exceptions etc... }
 * // The useragent used in the lookup
 * String ua = results.getHeaders().get(ClientConstants.KEY_USERAGENT.toString());
 * // The source of the data - cache, cloud etc
 * String dataSource = results.getSource();
 * // The actual device properties
 * Properties properties = results.getProperties();
 * </pre>
 * <br> <b>Example 2:</b><br>Passing a custom list of HTTP headers. This can
 * be useful if the app is not under a servlet container.
 * <pre>
 * Map <String, String> headers = new HashMap<String, String>();
 * // get data by passing headers (headers named are not case sensitive and may include  "_" or start with "HTTP_")
 * headers.put("User-Agent", "Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaN95_8GB/15.0.015; Profile/MIDP-2.0 Configuration/CLDC-1.1 ) AppleWebKit/413 (KHTML, like Gecko) Safari/413");
 * headers.put("X-Profile", "http://nds.nokia.com/uaprof/NN95_8GB-1r100.xml");
 * headers.put("Accept", "text/html,text/css,multipart/mixed,application/java-archive, application/java");
 * headers.put("Accept-Language", "en-us,en;q=0.5");
 * Client client = Client.getInstance();
 * try {
 *     Result results = client.getResultByHeaders(headers);
 * } catch (Exception x) { // handle exceptions etc... }
 * // The useragent used in the lookup
 * String ua = results.getHeaders().get(ClientConstants.KEY_USERAGENT.toString());
 * // The source of the data - cache, cloud etc
 * String dataSource = results.getSource();
 * // The actual device properties
 * Properties properties = results.getProperties();
 * </pre>
 * <br> <b>Example 3:</b><br>Passing a user-agent. This can be useful if the
 * app is not under a servlet container.
 * <pre>
 * Client client = Client.getInstance();
 * String ua = "Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaN95_8GB/15.0.015; Profile/MIDP-2.0 Configuration/CLDC-1.1 ) AppleWebKit/413 (KHTML, like Gecko) Safari/413";
 * try {
 *     Result results = client.getResultByUserAgent(ua);
 * } catch (Exception x) { // handle exceptions etc... }
 * // The useragent used in the lookup
 * String ua = results.getHeaders().get(ClientConstants.KEY_USERAGENT.toString()));
 * // The source of the data - cache, cloud etc
 * String dataSource = results.getSource();
 * // The actual device properties
 * Properties properties = results.getProperties();
 * </pre>
 *
 * Copyright (c) 2008-2016 by Afilias Technologies Limited. All rights reserved.
 * @author Afilias Technologies Ltd
 */
public class Client {
    /**
     * Please see ClientConstants.API_VERSION.toString()
     * @deprecated
     */
    @Deprecated public static final String API_VERSION = ClientConstants.API_VERSION.toString();
    /**
     * Please see ClientConstants.CLIENT_COOKIE_NAME.toString()
     * @deprecated
     */
    @Deprecated public static final String CLIENT_COOKIE_NAME = ClientConstants.CLIENT_COOKIE_NAME.toString();
    /**
     * Please see ClientConstants.KEY_USERAGENT.toString()
     * @deprecated
     */
    @Deprecated public static final String KEY_USERAGENT = ClientConstants.KEY_USERAGENT.toString();
    /**
     * Please see ClientConstants.KEY_SOURCE.toString()
     * @deprecated
     */
    @Deprecated public static final String KEY_SOURCE = ClientConstants.KEY_SOURCE.toString();
    /**
     * Please see ClientConstants.KEY_PROPERTIES.toString()
     * @deprecated
     */
    @Deprecated public static final String KEY_PROPERTIES = ClientConstants.KEY_PROPERTIES.toString();
    /**
     * Please see ClientConstants.SOURCE_CACHE.toString()
     * @deprecated
     */
    @Deprecated public static final String SOURCE_CACHE = ClientConstants.SOURCE_CACHE.toString();
    /**
     * Please see ClientConstants.SOURCE_CLOUD.toString()
     * @deprecated
     */
    @Deprecated public static final String SOURCE_CLOUD = ClientConstants.SOURCE_CLOUD.toString();
    /**
     * Please see ClientConstants.SOURCE_NONE.toString()
     * @deprecated
     */
    @Deprecated public static final String SOURCE_NONE = ClientConstants.SOURCE_NONE.toString();
    /**
     * Please see ActionConstants.FAILOVER_NOT_REQUIRED.toString()
     * @deprecated
     */
    @Deprecated private static final byte FAILOVER_NOT_REQUIRED = ActionConstants.FAILOVER_NOT_REQUIRED.getAction();
    /**
     * Please see ActionConstants.FAILOVER_STOP.toString()
     * @deprecated
     */
    @Deprecated private static final byte FAILOVER_STOP = ActionConstants.FAILOVER_STOP.getAction();
    /**
     * Please see ActionConstants.FAILOVER_CONTINUE.toString()
     * @deprecated
     */
    @Deprecated private static final byte FAILOVER_CONTINUE = ActionConstants.FAILOVER_CONTINUE.getAction();
    // headers
    /**
     * Please see HeaderConstants.DA_HEADER_PREFIX.toString()
     * @deprecated
     */
    @Deprecated public static final String DA_HEADER_PREFIX = HeaderConstants.DA_HEADER_PREFIX.toString();
    /**
     * Please see HeaderConstants.CLIENT_COOKIE_HEADER.toString()
     * @deprecated
     */
    @Deprecated public static final String CLIENT_COOKIE_HEADER = HeaderConstants.CLIENT_COOKIE_HEADER.toString();
    /**
     * Please see HeaderConstants.REMOTE_ADDR.toString()
     * @deprecated
     */
    @Deprecated private   static final String REMOTE_ADDR = HeaderConstants.REMOTE_ADDR.toString();
    /**
     * Please see HeaderConstants.UA_HEADER.toString()
     * @deprecated
     */
    @Deprecated private   static final String UA_HEADER = HeaderConstants.UA_HEADER.toString();
    /**
     * Please see HeaderConstants.COOKIE_HEADER.toString()
     * @deprecated
     */
    @Deprecated private   static final String COOKIE_HEADER = HeaderConstants.COOKIE_HEADER.toString();
    /**
     * Please see ClientConstants.OPERA_HEADER_IDENTIFIER.toString()
     * @deprecated
     */
    @Deprecated private static final String OPERA_HEADER_IDENTIFIER = ClientConstants.OPERA_HEADER_IDENTIFIER.toString();




    private static Client instance;
    private CacheService cacheService;
    private EndPointService endPointService;
    private DeviceIdentificatorService deviceIdentificatorService;

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);


    /**
     * Private constructor for singleton...
     * 
     * @param cachePropsProvider
     */
    private Client(CacheService cacheService) {
        this.cacheService = cacheService;
        this.endPointService = new EndPointService(this.cacheService);
        this.deviceIdentificatorService = new DeviceIdentificatorService(this.endPointService);
    }

    /**
     * Get the instance for the Client
     *
     * @param cacheService CacheService object with the cache provider
     * @return The instance of the Client
     * @throws CacheException This occurs if the Cache could not be initialized properly.
     */
    public static synchronized Client getInstance(CacheService cacheService) throws CacheException {
        if (cacheService == null) {
            throw new CacheException("The cache service needs to be provided");
        }
        if (instance == null) {
            instance = new Client(cacheService);
        }
        return instance;
    }

    /**
     * Get the instance for the Client
     *
     * @param cacheProvider CacheProvider object with the cache provider
     * @return The instance of the Client
     * @throws CacheException This occurs if the Cache could not be initialized properly.
     */
    public static synchronized Client getInstance(CacheProvider cacheProvider) throws CacheException {
        return Client.getInstance(new CacheService(cacheProvider));
    }

    public static synchronized Client getInstance() throws CacheException {
        if (instance == null) {
            throw new CacheException("getInstance(<CacheProvider>) needs to be called in first call");
        }

        return instance;
    }

    /**
     * Get the last DeviceAtlas cloud service end-point (server) URL called to
     * get device properties
     *
     * @return null or a URL. Null means no call to a DeviceAtlas cloud end-point
     * was done probably because the data was fetched from cache.
     */
    public String getCloudUrl() {
        return endPointService.getCloudUrl();
    }

    /**
     * Sets the last endpoint url used when call the service
     *
     * @param lastUsedCloudUrl
     */
    public void setCloudUrl(String lastUsedCloudUrl) {
        endPointService.setCloudUrl(lastUsedCloudUrl);
    }

    public String getRankingStatus() {
        return endPointService.getRankingStatus();
    }

    /**
     * Getter for cloud server list. 
     * 
     * The end-points in the returned array are used
     * for making DeviceAtlas cloud calls. The top end-point will most likely be
     * used unless it fails and the call falls to the next end-point and so on.
     *
     * @return An array of EndPoint objects (each object is a DeviceAtlas cloud end-point)
     * @throws ClientException when an exception from the Cloud instance side appears
     * @throws CacheException when cache fails to be written
     */
    public EndPoint[] getEndPoints() throws ClientException {
        EndPoint [] endPoints = new EndPoint[0];

        try {
            endPoints = endPointService.getEndPoints();
        } catch (CacheException ex) {
            LOGGER.error("getEndPoints", ex);
        }

        return endPoints;
    }
    /**
     * Setter for cloud server list. To set DeviceAtlas cloud end-points manually.
     * The order of this array matters. While healthy the top end-point will be used.
     *
     * @param endPoints An array of EndPoint objects, each object is a DeviceAtlas cloud end-point
     */
    public void setEndPoints(EndPoint[] endPoints) {
        this.endPointService.setEndPoints(endPoints.clone());
    }


    /**
     * Getter for auto server ranking.
     *
     * @return true = the API will auto rank DeviceAtlas end-points.
     * false = the default built-in DeviceAtlas end-points array or one provided
     * manually will be used by the API.
     */
    public boolean getAutoServerRanking() {
        return endPointService.getAutoServerRanking();
    }

    /**
     * Setter for auto server ranking.
     *
     * @param autoServerRanking true = the API must auto rank DeviceAtlas end-points.
     * false = the API should use the built-in EndPoint array or one which is provided to it.
     */
    public void setAutoServerRanking(boolean autoServerRanking) {
        endPointService.setAutoServerRanking(autoServerRanking);
    }

    /**
     * Getter for cloud service timeout. If the API requests service from a
     * DeviceAtlas end-point, shall the end-point not response in this amount of
     * time the next end-point in the list will be used.
     *
     * @return Time in seconds. 
     */
    public int getCloudServiceTimeout() {
        return endPointService.getCloudServiceTimeout();
    }

    /**
     * Setter for cloud service timeout. If the API requests service from a
     * DeviceAtlas end-point, shall the end-point not response in this amount of
     * time the next end-point in the list will be used.
     *
     * @param cloudServiceTimeout Time in seconds.
     */
    public void setCloudServiceTimeout(int cloudServiceTimeout) {
        endPointService.setCloudServiceTimeout(cloudServiceTimeout);
    }

    /**
     * Getter for max server failures.
     *
     * @return Number of times an end-point is allowed to fail during auto ranking.
     */
    public int getAutoServerRankingMaxFailures() {
        return endPointService.getAutoServerRankingMaxFailures();
    }

    /**
     * Setter for max server failures. Number of times an end-point is allowed to
     * fail during auto ranking. Failing more times and the end-pint will be excluded.
     *
     * @param autoServerRankingMaxFailures End-point fail tolerance during auto ranking.
     */
    public void setAutoServerRankingMaxFailures(int autoServerRankingMaxFailures) {
        endPointService.setAutoServerRankingMaxFailures(autoServerRankingMaxFailures);
    }

    /**
     * When auto ranking end-points, this number of requests will be sent to each
     * end-point and the mean time will be used for ranking the end-point.
     *
     * @return Number or requests per end-point during auto-ranking.
     */
    public int getAutoServerRankingNumRequests() {
        return endPointService.getAutoServerRankingNumRequests();
    }

    /**
     * When auto ranking end-points, this number of requests will be sent to each
     * end-point and the mean time will be used for ranking the end-point.
     *
     * @param autoServerRankingNumRequests Number or requests per end-point during auto-ranking.
     */
    public void setAutoServerRankingNumRequests(int autoServerRankingNumRequests) {
        endPointService.setAutoServerRankingNumRequests(autoServerRankingNumRequests);
    }

    /**
     * Returns the server ranking cache lifetime
     *
     * @return serverRankingLifetime
     */

    public int getServerRankingLifetime() {
        return cacheService.getServerRankingLifetime();
    }

    /**
     * Sets the server ranking cache lifetime
     *
     * @param serverRankingListLifetime
     */

    public void setServerRankingLifetime(int serverRankingListLifetime) {
        cacheService.setServerRankingLifetime(serverRankingListLifetime);
    }

    /**
     * Getter for manual ranking server phase out Lifetime. When not auto-ranking
     * shall the top end-point fail the list will be re-arranged and the failed
     * end-point will be moved to the bottom. ServerPhaseOutLifetime indicated
     * for how long should this re-arranged EndPoint list be used over the original.
     *
     * @deprecated As of release 1.5 getServerPhaseOutLifetime() and
     * getAutoServerRankingLifetime() have been merged.
     * @return Time in minutes.
     */
    @Deprecated
    public int getPhaseOutLifetime() {
        return cacheService.getServerRankingLifetime();
    }

    /**
     * Setter for auto ranking cached end-point list lifetime. After each auto ranking
     * the ranked end point list will be valid for this amount of time. When the list
     * is too old the API will rank it again unless the auto ranking is off.
     *
     * @deprecated As of release 1.5 setPhaseOutLifetime() and
     * setPhaseOutLifetime() have been merged.
     */
    @Deprecated
    public void setPhaseOutLifetime(int serverRankListLifetime) {
        cacheService.setServerRankingLifetime(serverRankListLifetime);
    }

    /**
     * Getter for auto ranking cached end-point list lifetime. After each auto ranking
     * the ranked end point list will be valid for this amount of time. When the list
     * is too old the API will rank it again unless the auto ranking is off.
     * AutoServerRankingLifetime indicated
     * for how long should this re-arranged EndPoint list be used over the original.
     *
     * @deprecated As of release 1.5 getAutoServerRankingLifetime() and
     * getAutoServerRankingLifetime() have been merged.
     * @return Time in minutes.
     */
    @Deprecated
    public int getAutoServerRankingLifetime() {
        return cacheService.getServerRankingLifetime();
    }

    /**
     * Setter for auto ranking cached end-point list lifetime. After each auto ranking
     * the ranked end point list will be valid for this amount of time. When the list
     * is too old the API will rank it again unless the auto ranking is off.
     *
     * @deprecated As of release 1.5 setAutoServerRankingLifeTime() and
     * setAutoServerRankingLifetime() have been merged.
     */

    @Deprecated
    public void setAutoServerRankingLifetime(int serverRankListLifetime) {
        cacheService.setServerRankingLifetime(serverRankListLifetime);
    }

    /**
     * Returns the cache layer
     *
     * @return cacheService
     */

    public CacheService getCacheService() {
        return cacheService;
    }

    /**
     * Returns the endPoint layer
     *
     * @return cacheService
     */

    public EndPointService getEndPointService() {
        return endPointService;
    }

    /**
     * Returns the identificator layer
     *
     * @return cacheService
     */

    public DeviceIdentificatorService getDeviceIdentificatorService() {
        return deviceIdentificatorService;
    }

    /**
     * Getter for DeviceAtlas cloud licence key.
     *
     * @return DeviceAtlas licence key
     */
    public String getLicenceKey() {
        return deviceIdentificatorService.getLicenceKey();
    }

    /**
     * Setter for DeviceAtlas cloud licence key.
     *
     * @param licenceKey The licence key issued to gain access to DeviceAtlas Cloud.
     */
    public void setLicenceKey(String licenceKey) {
        deviceIdentificatorService.setLicenceKey(licenceKey);
    }


    /**
     * Get the API version
     *
     * @return API VERSION
     */
    public static String getVersion() {
        return ClientConstants.API_VERSION.toString();
    }

    /**
     * Get the cloud service data from the identificator layer
     *
     * @param request
     * @return Map
     * @deprecated
     */

    @Deprecated
    public Map getDeviceData(HttpServletRequest request) throws ClientException {
        return deviceIdentificatorService.getDeviceData(request);
    }

    /**
     * Get the cloud service data from the identificator layer
     *
     * @param userAgent
     * @return Map
     * @deprecated
     */

    @Deprecated
    public Map getDeviceData(String userAgent) throws ClientException {
        return deviceIdentificatorService.getDeviceData(userAgent);
    }
    
    /**
     * Get the cloud service data from the identificator layer
     *
     * @param headers
     * @return Map
     * @deprecated
     */

    @Deprecated
    public Map getDeviceData(Map<String, String> headers) throws ClientException {
        return deviceIdentificatorService.getDeviceData(headers);
    }

    /**
     * Get the cloud service data from the identificator layer
     *
     * @param userAgent
     * @return Map
     * @deprecated
     */

    @Deprecated
    public Map getDeviceDataByUserAgent(String userAgent) throws ClientException {
        return deviceIdentificatorService.getDeviceDataByUserAgent(userAgent);
    }

    /**
     * Get the cloud service data from the identificator layer
     *
     * @param headers
     * @return Map
     * @deprecated
     */

    @Deprecated
    public Map getDeviceDataByHeaders(Map<String, String> headers) throws ClientException {
        return deviceIdentificatorService.getDeviceDataByHeaders(headers);
    }

    /**
     * Get the cloud service data from the identificator layer
     *
     * @param request
     * @return Result
     */

    public Result getResult(HttpServletRequest request) throws ClientException {
        return deviceIdentificatorService.getResult(request);
    }

    /**
     * Get the cloud service data from the identificator layer
     *
     * @param userAgent
     * @return Result
     */

    public Result getResult(String userAgent) throws ClientException {
        return deviceIdentificatorService.getResult(userAgent);
    }
    
    /**
     * Get the cloud service data from the identificator layer
     *
     * @param headers
     * @return Result
     */

    public Result getResult(Map<String, String> headers) throws ClientException {
        return deviceIdentificatorService.getResult(headers);
    }

    /**
     * Get the cloud service data from the identificator layer
     *
     * @param userAgent
     * @return Result
     */

    public Result getResultByUserAgent(String userAgent) throws ClientException {
        return deviceIdentificatorService.getResultByUserAgent(userAgent);
    }

    /**
     * Get the cloud service data from the identificator layer
     *
     * @param headers
     * @return Result
     */

    public Result getResultByHeaders(Map<String, String> headers) throws ClientException {
        return deviceIdentificatorService.getResultByHeaders(headers);
    }

    /**
     * Sets the optional proxy for reaching cloud's service
     *
     * @param proxy
     */
    public void setProxy(Proxy proxy) {
        endPointService.setProxy(proxy);
    }

    /**
     * This throws an exception to prevent cloning this singleton.
     *
     * @return Nothing as exception always thrown
     * @throws CloneNotSupportedException when tries to clone using the
     * singleton pattern
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }


    /**
     * Get DeviceAtlas cloud end-point list. If auto-ranking is on then the ranked
     * end-point list is returned otherwise the manual or default original or
     * fail-over list will be returned.
     * If auto-ranking is on but no valid end-pont list is found inside the cache
     * then the end-points will be ranked and cached.
     *
     * @return An array of EndPoint objects
     */
    public EndPoint[] getServersLatencies() {
        return endPointService.getServersLatencies();
    }

    /**
     * Get endPoints and their service latencies.
     *
     * @param numRequests Number of times to request from server
     * @return An array of EndPoint objects
     */
    public EndPoint[] getServersLatencies(int numRequests) {
        return endPointService.getServersLatencies(numRequests);
    }

    /**
     * If auto-ranking is on then rank the DeviceAtlas cloud end-points and put in cache.
     *
     * @return The ranked or re-ordered end-point list
     * @throws CacheException when an error checking server latencies appears
     */
    public EndPoint[] rankServers() throws CacheException {
        return endPointService.rankServers();
    }

    /**
     * Getter for the useClientCookie setting. Defaults to true. If TRUE then if
     * device data which is created by the DeviceAtlas client side component
     * (JS library) exists it will be used
     *
     * @return The sendExtraHeaders setting. Defaults to false
     */
    public boolean getUseClientCookie() {
        return deviceIdentificatorService.getUseClientCookie();
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
        deviceIdentificatorService.setUseClientCookie(useClientCookie);
    }
    /**
     * Is device data being cached by the API or not.
     *
     * @return true(default) = data is being cached.
     */
    public boolean getUseCache() {
        return cacheService.getUseCache();
    }

    /**
     * Setter to cache or not to cache device data.
     *
     * @param useCache true = cache data after getting device data from DeviceAtlas cloud
     */
    public void setUseCache(boolean useCache) {
        cacheService.setUseCache(useCache);
    }

    /**
     * Clear all cached data.
     */
    public void clearCache() {
        cacheService.clearCache();
    }

    /**
     * This should be called when shutting down your application. It asks
     * the cache manager to shutdown the cache and write the cache to disk.
     *
     * See http://ehcache.org/documentation/code-samples#shutdown-the-cachemanager
     */
    public void shutdown() {
        cacheService.shutdown();
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
        return deviceIdentificatorService.getSendExtraHeaders();
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
        deviceIdentificatorService.setSendExtraHeaders(sendExtraHeaders);
    }
}
