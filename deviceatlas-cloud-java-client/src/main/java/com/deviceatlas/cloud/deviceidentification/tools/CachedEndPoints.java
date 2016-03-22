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


package com.deviceatlas.cloud.deviceidentification.tools;

/**
 * This is a small CLI app which returns the cached end-point (server) lists.
 *
 * <br/>
 * <b>Usage:</b><br/>
 * Linux:<br/>
 *   java -cp ./thirdparty/ehcache-1.6.2.jar:./config:deviceatlas-cloud-java-client-VERSION.jar CachedEndPoints LICENCE-KEY
 * Windows:<br/>
 *   java -cp .\thirdparty\ehcache-1.6.2.jar;.\config;deviceatlas-cloud-java-client-VERSION.jar CachedEndPoints LICENCE-KEY
 *
 * Copyright (c) 2008-2016 by Afilias Technologies Limited. All rights reserved.
 * @author Afilias Technologies Ltd
 */

import com.deviceatlas.cloud.deviceidentification.client.Client;
import com.deviceatlas.cloud.deviceidentification.service.CacheService;
import com.deviceatlas.cloud.deviceidentification.cacheprovider.SimpleCacheProvider;
import com.deviceatlas.cloud.deviceidentification.client.ClientConstants;
import com.deviceatlas.cloud.deviceidentification.endpoint.EndPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedEndPoints {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedEndPoints.class);

    private CachedEndPoints() {
    }

    public static void serversList(EndPoint[] endPoints) {
        if (endPoints == null) {
            return ;
        }

        for (EndPoint endPoint : endPoints) {

            String name = endPoint.host;
            LOGGER.warn("\n    " + name + ':' + endPoint.port);

            if (Double.compare(endPoint.avg, -1.0d) == 0 && endPoint.latencies != null) {

                for (Double latency : endPoint.latencies) {
                    String logMsg = Double.compare(latency, -1.0d) == 0 ?
                        "\tn/a" : "\t" + Double.toString(latency) + "ms";
                    LOGGER.warn(logMsg);
                }
                LOGGER.info("        * average: " + Double.toString(endPoint.avg) + "ms");
            }
        }
    }

    public static void main(String [] args) {

        LOGGER.info("\nDeviceAtlas Cloud\n");

        try {
            if (args.length == 0) {
                LOGGER.warn("\nNo licence key provided.\n");
                LOGGER.warn("\nusage: ... CachedEndPoints LICENCE-KEY\n");
                return;
            }

            Client client = Client.getInstance(new CacheService(new SimpleCacheProvider()));
            client.setLicenceKey(args[0]);

            /* server auto */
            LOGGER.info("********** CACHED SERVER AUTO RANKED LIST **********");
            EndPoint[] endPoints = client.getCacheService().getCachedServerList(ClientConstants.CACHE_NAME_SERVERS_AUTO.toString());
            serversList(endPoints);

            /* server manual */
            LOGGER.info("\n********** CACHED SERVER MANUAL FAILOVER LIST **********");
            endPoints = client.getCacheService().getCachedServerList(ClientConstants.CACHE_NAME_SERVERS_MANUAL.toString());
            serversList(endPoints);

            client.getCacheService().shutdown();

        // catch the exceptions
        } catch (Exception ex) {
            LOGGER.error("get cached endPoints", ex);
        }
    }
}
