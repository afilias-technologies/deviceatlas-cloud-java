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
import com.deviceatlas.cloud.deviceidentification.client.ActionConstants;
import com.deviceatlas.cloud.deviceidentification.parser.JsonException;
import com.deviceatlas.cloud.deviceidentification.parser.JsonParser;
import com.deviceatlas.cloud.deviceidentification.endpoint.EndPoint;
import com.deviceatlas.cloud.deviceidentification.utils.NetworkUtils;
import com.deviceatlas.cloud.deviceidentification.utils.RequestBuilderUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import java.net.Proxy;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndPointService {
    private CacheService cacheService;
    private boolean autoServerRanking                               = true;
    private boolean serverListIsManual                              = false;
    private int     autoServerLatencyNumRequests                    = 3;
    private int     autoServerRankingMaxFailures                    = 1;
    private int     cloudServiceTimeout                             = 3;
    private String  selfAutoRanking                                 = "n"; // auto ranking by API or user
    private String  licenceKey                                      = null;
    private String errorMessage                                     = "";
    // server fail-over and ranking system
    private String  lastUsedCloudUrl;
    /* For tracking ranking mechanism */
    protected String rankingStatus;
    /* proxy object */
    private   Proxy proxy;
    // to avoid getting stuck in and end-less loop when rankServers() calls getEndPoints()
    private boolean getServersRankIfRequired                        = true;
    protected List<String> calledServers;      // for test and debug
    private   List<String> fatalErrors                              = null; // for test and debug
    private byte failoverAction;
    private EndPoint[] endPoints                                    = {
        new EndPoint("http://region0.deviceatlascloud.com", "80"),
        new EndPoint("http://region1.deviceatlascloud.com", "80"),
        new EndPoint("http://region2.deviceatlascloud.com", "80"),
        new EndPoint("http://region3.deviceatlascloud.com", "80"),
    };
    private static final Logger LOGGER = LoggerFactory.getLogger(EndPointService.class);

    public EndPointService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * Gets the current action to take regarding the connection service
     *
     * @return failoverAction
     */

    public byte getFailoverAction() {
        return failoverAction;
    }

    /**
     * Sets the action level
     *
     * @param failoverAction
     */

    public void setFailoverAction(byte failoverAction) {
        this.failoverAction = failoverAction;
    }

    /**
     * Returns the underlying cache layer
     *
     * @return cacheService
     */

    public CacheService getCacheService() {
        return cacheService;
    }

    /**
     * true if the server list is the manual type
     *
     * @return serverListIsManual
     */

    public boolean isServerListManual() {
        return serverListIsManual;
    }

    /**
     * Sets the type of server list (manual or not)
     *
     * @param serverListIsManual
     */

    public void setServerListIsManual(boolean serverListIsManual) {
        this.serverListIsManual = serverListIsManual;
    }

    /**
     * Gets the last endpoint url used when call the service
     *
     * @return lastUsedCloudUrl
     */

    public String getCloudUrl() {
        return lastUsedCloudUrl;
    }

    /**
     * Sets the last endpoint url used when call the service
     *
     * @param lastUsedCloudUrl
     */

    public void setCloudUrl(String lastUsedCloudUrl) {
        this.lastUsedCloudUrl = lastUsedCloudUrl;
    }

    /**
     * Returns the cloud service's timeout
     *
     * @return cloudServiceTimeout
     */

    public int getCloudServiceTimeout() {
        return cloudServiceTimeout;
    }

    /**
     * Sets the cloud service's timeout
     *
     * @param cloudServiceTimeout
     */

    public void setCloudServiceTimeout(int cloudServiceTimeout) {
        this.cloudServiceTimeout = cloudServiceTimeout;
    }

    /**
     * Returns the maximum amount of failures for server ranking's checks
     *
     * @return autoServerRankingMaxFailures
     */

    public int getAutoServerRankingMaxFailures() {
        return autoServerRankingMaxFailures;
    }

    /**
     * Sets the maximum amount of failures for server ranking's checks
     *
     * @param autoServerRankingMaxFailures
     */
    
    public void setAutoServerRankingMaxFailures(int autoServerRankingMaxFailures) {
        this.autoServerRankingMaxFailures = autoServerRankingMaxFailures;
    }

    /**
     * Returns the maximum number of requests
     *
     * @return autoServerLatencyNumRequests
     */

    public int getAutoServerRankingNumRequests() {
        return autoServerLatencyNumRequests;
    }

    /**
     * Sets the maximum number of requests
     *
     */

    public void setAutoServerRankingNumRequests(int autoServerLatencyNumRequests) {
        this.autoServerLatencyNumRequests = autoServerLatencyNumRequests;
    }

    /**
     * Returns the current ranking status
     *
     * @return rankingStatus
     */

    public String getRankingStatus() {
        return rankingStatus;
    }

    /**
     * Sets the current ranking status
     *
     * @param rankingStatus
     */

    public void setRankingStatus(String rankingStatus) {
        this.rankingStatus = rankingStatus;
    }

    /**
     * Sets the licence key
     *
     * @param licenceKey
     */

    public void setLicenceKey(String licenceKey) {
        this.licenceKey = licenceKey;
    }

    /**
     * Gets the list of ranking endpoints (manual or auto)
     *
     * @return cachedEndPoints
     */

    public EndPoint[] getEndPoints() throws CacheException {
        EndPoint[] cachedEndPoints;
        if (autoServerRanking) {
            selfAutoRanking = "y";
            List<Map> data = cacheService.getCacheServersManualRanking();
            if (data != null) {
                serverListIsManual = false;
                cachedEndPoints = cacheService.convertServerMapListToServerList(data);
                rankingStatus = "A";
                return cachedEndPoints;
            }
            // no or expired server ranked list - rank endPoints
            if (getServersRankIfRequired) {
                cachedEndPoints = rankServers();
                if (cachedEndPoints != null) {
                    return cachedEndPoints;
                }
            }
        }
        // check if manual list is cached or not
        // manual list is cached and used for some time when top server fails
        serverListIsManual = true;
        List<Map> data = cacheService.getCacheServersManualRanking();
        if (data != null) {
            cachedEndPoints = cacheService.convertServerMapListToServerList(data);
            rankingStatus = "M";
            return cachedEndPoints;
        }
        // return default unranked list
        rankingStatus = "D";
        cachedEndPoints = endPoints;
        return cachedEndPoints;
    }

    /**
     * Returns the original endpoints list
     *
     * @return endPoints
     */

    public EndPoint[] getOriginalEndPoints() {
        return endPoints.clone();
    }

    /**
     * Sets the original list of endpoints
     *
     * @param endPoints
     */

    public void setEndPoints(EndPoint [] endPoints) {
        this.endPoints = endPoints.clone();
    }

    /**
     * Returns the fist endpoint
     *
     * @return endPoints
     */

    public EndPoint getFirstEndPoint() {
        return this.endPoints[0];
    }

    /**
     * Sets the first endpoint
     *
     * @param endPoint
     */

    public void setFirstEndPoint(EndPoint endPoint) {
        this.endPoints[0] = endPoint;
    }

    /**
     * Checks if the server ranking is in auto mode
     *
     * @return autoServerRanking
     */

    public boolean getAutoServerRanking() {
        return autoServerRanking;
    }

    /**
     * Set the server ranking in auto mode
     *
     * @param autoServerRanking
     */

    public void setAutoServerRanking(boolean autoServerRanking) {
        this.autoServerRanking = autoServerRanking;
    }

    /**
     * Returns the list of errors
     *
     * @return fatalErrors
     */

    public List<String> getFatalErrors() {
        return fatalErrors;
    }

    /**
     * Returns the latencies of endpoints
     *
     * @param endPoint
     * @param numRequests
     * @return latencies
     */

    public List<Double> getServerLatency(EndPoint endPoint, int numRequests) {
        int          failures  = 0;
        List<Double> latencies = new ArrayList<Double>();
        long         tStart;
        fatalErrors  = null;
        // the first request includes API end-point settings
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(
                "Latency-Checker",
                selfAutoRanking
                + autoServerRanking
                + cloudServiceTimeout
                + autoServerRankingMaxFailures
                + autoServerLatencyNumRequests
                + cacheService.getServerRankingLifetime()
                );
        // ignore results from the first call because it can take an unreal long time
        for (int i = 0; i < numRequests + 1 && failures < autoServerRankingMaxFailures; ++ i) {
            tStart = System.currentTimeMillis();
            if (i > 0) {
                headers.put("Latency-Checker", Integer.toString(i));
            }
            try {
                failoverAction = ActionConstants.FAILOVER_NOT_REQUIRED.getAction();
                List<String> errors = new ArrayList<String>();
                connectCloud(endPoint, "", headers, errors);

                if (failoverAction == ActionConstants.FAILOVER_NOT_REQUIRED.getAction() &&
                        i > 0) {
                    latencies.add((double)(System.currentTimeMillis() - tStart));
                }

                if (failoverAction == ActionConstants.FAILOVER_STOP.getAction()) {
                    // licence errors which are found at ranking, to stop any further cloud call
                    fatalErrors = errors;
                    break;
                } else if (failoverAction == ActionConstants.FAILOVER_NOT_REQUIRED.getAction()) {
                    continue;
                }

            } catch (ClientException e) {
                LOGGER.error("get endPoint latency", e);
            }

            ++failures;
            latencies.add(-1.0d);

        }

        return latencies;
    }

    /**
     * Get endPoints and their service latencies.
     *
     * @param numRequests Number of times to request from server
     * @return An array of EndPoint objects
     */
    public EndPoint[] getServersLatencies(int numRequests) {
        rankingStatus = "L";
        // test the endPoints in a randomly
        EndPoint[] localEndPoints = this.endPoints;
        int           len     = localEndPoints.length;
        List<Integer> seed    = new ArrayList<Integer>();
        int      k, j;
        for (k = 0; k < len; ++k) {
            seed.add(k);
        }
        Random rnd = new Random();
        List<Double> latencies;
        double sum;
        EndPoint endPoint;
        calledServers    = new ArrayList<String>();

        while (!seed.isEmpty()) {
            j = rnd.nextInt(seed.size());
            // process endPoint > > >
            endPoint = localEndPoints[seed.get(j)];
            // get selected endPoint latency
            latencies = getServerLatency(endPoint, numRequests);
            if (fatalErrors != null && !fatalErrors.isEmpty()) {
                return new EndPoint[0];
            }
            endPoint.latencies = latencies;
            if (latencies.contains(-1.0d)) {
                endPoint.avg = -1.0d;
            } else {
                sum = 0.0d;
                for (double latency : latencies) {
                    sum += latency;
                }
                endPoint.avg = sum / (double)numRequests;
            }
            // < < < proccess endPoint
            seed.remove(j);
        }

        return localEndPoints;
    }

    /**
     * Get endPoints and their service latencies.
     *
     * @return An array of EndPoint objects
     */

    public EndPoint[] getServersLatencies() {
        return getServersLatencies(autoServerLatencyNumRequests);
    }

    /**
     * Returns the servers called
     *
     * @return calledServers
     */

    public List<String> getCalledServers() {
        return calledServers;
    }

    /**
     * Sets the servers called
     *
     * @param calledServers
     */

    public void setCalledServers(List<String> calledServers) {
        this.calledServers = calledServers;
    }

    /**
     * Adds an endpoint to the list
     *
     * @param endPointL
     * @param endPoint
     */

    public void addEndPoint(List<EndPoint> endPointL, EndPoint endPoint) {
        int i;
        if (Double.compare(endPoint.avg, (double)-1) != 0) {
            for (i = 0; i < endPointL.size(); i++) {
                if (Double.compare(endPoint.avg, endPointL.get(i).avg) < 0) {
                    break;
                }
            }
            endPointL.add(i, endPoint);
        }
    }

    /**
     * Sets the end point
     */

    public void setEndPointsIfNotRanked() {
        // try to extend the cache expire time
        try {
            getServersRankIfRequired = false;
            cacheService.setServerCache(getEndPoints(), false);
            getServersRankIfRequired = true;
        } catch (Exception ex) {
            LOGGER.error("rank endPoints", ex);
        }
    }

    /**
     * If auto-ranking is on then rank the DeviceAtlas cloud end-points and put in cache.
     *
     * @return The ranked or re-ordered end-point list
     * @throws CacheException when an error checking server latencies appears
     */
    public EndPoint[] rankServers() throws CacheException {
        EndPoint [] emptyEndPoints = new EndPoint[0];
        if (!autoServerRanking) {
            return emptyEndPoints;
        }

        List<EndPoint> endPointL = new ArrayList<EndPoint>();

        EndPoint[] endPointLatencies = getServersLatencies();
        if (endPointLatencies.length == 0) {
            return emptyEndPoints;
        }

        for (EndPoint endPoint : endPointLatencies) {
            addEndPoint(endPointL, endPoint);
        }

        // no server detected
        if (endPointL.isEmpty()) {
            setEndPointsIfNotRanked();
            return emptyEndPoints;
        }

        // put server list in cache
        EndPoint[] localEndPoints = endPointL.toArray(new EndPoint[endPointL.size()]);
        cacheService.setServerCache(localEndPoints, true);
        return localEndPoints;
    }

    /**
     * Sets HTTP proxy settings for the remote requests
     * 
     * @param proxy proxy to set for the remote requests
     */
    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    /**
     * Get the properties from a cloud end point
     */
    public Map connectCloud(EndPoint endPoint, String userAgent, Map<String, String> headers, List<String> errors) throws ClientException {

        final String connectCloudError = "connect cloud";
        getCalledServers().add(endPoint.host);

        String         message = "";
        String         results = "";
        int status = 0;

        try {
            String url = new RequestBuilderUtils(endPoint.getUrl(), licenceKey, userAgent).
                buildCloudUrl();

            NetworkUtils nUtils = new NetworkUtils(proxy);
            Map<String, Object> connectionRet = nUtils.setConnection(url, headers, getCloudServiceTimeout());
            status = (Integer)connectionRet.get(ClientConstants.CLOUD_SERVICE_STATUS.toString());
            message = connectionRet.get(ClientConstants.CLOUD_SERVICE_MESSAGE.toString()).toString();
            if (connectionRet.containsKey(ClientConstants.CLOUD_SERVICE_RESULT.toString())) {
                results = connectionRet.get(ClientConstants.CLOUD_SERVICE_RESULT.toString()).toString();

                // if error status
                return decodeData(results);
            }
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error(connectCloudError, ex);

        } catch (JsonException ex) {
            LOGGER.error(connectCloudError, ex);

        } catch (NullPointerException ex) {
            LOGGER.error(connectCloudError, ex);
            throw new ClientException(ex.getMessage());

        }

        errorControler(endPoint, status, message);
        errors.add(errorMessage);

        return null;
    }

    /**
     * Decodes the JSON data and extracts the properties.
     */
    private Map decodeData(String dataStr) throws JsonException {
        if (dataStr == null) {
            throw new JsonException(JsonException.BAD_DATA, "Attempt to decode empty data.");
        }
        String tmpDataStr = dataStr.trim();
        if (!tmpDataStr.isEmpty() && tmpDataStr.charAt(0) == '{' && tmpDataStr.charAt(tmpDataStr.length() - 1) == '}') {
            Map decoded = new JsonParser(tmpDataStr).getHashMap();
            if (decoded.containsKey(ClientConstants.KEY_PROPERTIES.toString())) {
                return (HashMap)decoded.get(ClientConstants.KEY_PROPERTIES.toString());
            }
        }
        return null;
    }

    /**
     * when an end-point returns an error this method will check it
     * @return (action, error-message)
     */
    private void errorControler(EndPoint endPoint, int status, String msg) {
        failoverAction = ActionConstants.FAILOVER_CONTINUE.getAction();

        // Invalid licence key, Licence monthly quota exceeded
        if (msg.toLowerCase().indexOf("forbidden") != -1) {
            failoverAction = ActionConstants.FAILOVER_STOP.getAction();
        }

        errorMessage = failoverAction + ": Error getting data from DeviceAtlas Cloud end-point \"" +
            endPoint.host + "\", response " + status + ", Reason: ";

        String tmpMsg = msg.replace("\n", " ").replace("\r", " ").replaceAll("\\<[^>]*>","");
        errorMessage += tmpMsg;
    }

    /**
     * if top endPoints failed, move them to the bottom and re-cache the list
     *
     * @param localEndPoints
     * @param i
     */

    public void moveEndPointOrder(EndPoint [] localEndPoints, int i) throws CacheException {
        if (i > 0) {
            int k;
            int j = localEndPoints.length - 1;
            for (k = 0; k < i; k++) {
                EndPoint temp = localEndPoints[j];
                localEndPoints[j]  = getFirstEndPoint();
                setFirstEndPoint(temp);
            }
            cacheService.setServerCache(
                    localEndPoints,
                    isServerListManual()
                    );
        }
    }

    /**
     * Treats the endPoint response
     *
     * @param localEndPoints
     * @param userAgent
     * @param headers
     * @param errors
     * @return Map
     */

    public Map getCloudServiceResponse(EndPoint [] localEndPoints, String userAgent,
            Map<String, String> headers, List<String> errors) throws CacheException {
        Map response;
        // for each server try to get service or go for next
        try {
            for (int i = 0; i < getEndPoints().length; i++) {
                setFailoverAction(ActionConstants.FAILOVER_NOT_REQUIRED.getAction());
                EndPoint endPoint = localEndPoints[i];
                response         = connectCloud(endPoint, userAgent, headers, errors);
                lastUsedCloudUrl = endPoint.getUrl();
                // if endPoint did not fail
                if (getFailoverAction() == ActionConstants.FAILOVER_NOT_REQUIRED.getAction()) {
                    moveEndPointOrder(localEndPoints, i);
                    return response;

                } else if (getFailoverAction() == ActionConstants.FAILOVER_STOP.getAction()) {
                    break;
                }
            }
        } catch (ClientException ex) {
            LOGGER.error("gtCloudServiceResponse", ex);
        }

        return null;
    }

    /**
     * Get device properties from DeviceAtlas cloud service
     */
    public Map getCloudService(String userAgent, Map<String, String> headers) throws ClientException {
        List<String> errors;
        // getEndPoints returns the auto or manual server list
        try {
            EndPoint[] localEndPoints = getEndPoints();

            // if cloud was called upon ranking via the getEndPoints() call, and it was recognized
            // that the licence is unusable then dont try and throw the error
            if (getFatalErrors() == null || getFatalErrors().isEmpty()) {
                errors       = new ArrayList<String>();
                Map response;
                if ((response = getCloudServiceResponse(localEndPoints, userAgent, headers, errors)) != null) {
                    return response;
                }
            } else {
                errors = getFatalErrors();
            }
            // when all endPoints fail display their errors
            if (!errors.isEmpty()) {
                StringBuilder errorStr = new StringBuilder();
                for (String error: errors) {
                    errorStr.append(error + "\n");
                }
                throw new ClientException(errorStr.toString());
            }
            throw new ClientException("No server has been defined.");
        } catch (CacheException ex) {
            LOGGER.error("getCloudService", ex);
        }

        return null;
    }
}
