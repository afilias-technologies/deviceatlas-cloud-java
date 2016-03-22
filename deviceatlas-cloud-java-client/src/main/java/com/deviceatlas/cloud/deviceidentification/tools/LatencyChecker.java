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

import com.deviceatlas.cloud.deviceidentification.client.Client;
import com.deviceatlas.cloud.deviceidentification.service.CacheService;
import com.deviceatlas.cloud.deviceidentification.cacheprovider.SimpleCacheProvider;
import com.deviceatlas.cloud.deviceidentification.endpoint.EndPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a small CLI app which tests and prints DeviceAtlas cloud endPoints
 * performance. This script can help you manually arrange the DeviceAtlas endpoint
 * list when auto ranking is off.
 *
 * <br/>
 * <b>Usage:</b><br/>
 * Linux:<br/>
 *   java -cp ./thirdparty/ehcache-1.6.2.jar:./config:deviceatlas-cloud-java-client-VERSION.jar LatencyChecker LICENCE-KEY
 * Windows:<br/>
 *   java -cp .\thirdparty\ehcache-1.6.2.jar;.\config;deviceatlas-cloud-java-client-VERSION.jar LatencyChecker LICENCE-KEY
 * 
 * Copyright (c) 2008-2016 by Afilias Technologies Limited. All rights reserved.
 * @author Afilias Technologies Ltd
 */
public class LatencyChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(LatencyChecker.class);
    private LatencyChecker() {
    }

    public static void serversList(EndPoint[] endPoints) {
        String best    = "\nNo good server found!";
        double bestAvg = -1.0d;

        for (EndPoint endPoint : endPoints) {
            String name = endPoint.host;
            String logMsg = "";
            logMsg += Double.compare(endPoint.avg, -1.0d) == 0 ?
                "\t(Could't connect to host)" : "";

            LOGGER.info("\n    " + name + ':' + endPoint.port);
            LOGGER.info(logMsg);

            for (Double latency : endPoint.latencies) {
                logMsg = Double.compare(latency, -10.d) == 0 ?
                    "\t\tn/a" : "\t\t" + Double.toString(latency) + "ms";
                LOGGER.info(logMsg);
            }

            LOGGER.info("        * average: " + endPoint.avg + "ms");
            boolean cond = Double.compare(bestAvg, -1.0d) == 0 || Double.compare(bestAvg, endPoint.avg) > 1;
            bestAvg = cond ? endPoint.avg : bestAvg;
            best = cond ? "\nBest endPoint >> " +  name + " <<" : best;
        }

        LOGGER.info(best);
    }

    public static void main(String [] args) {

        LOGGER.info("\nDeviceAtlas Cloud latency checker\n");
        LOGGER.info("Running tests, this may take a while...\n");

        try {
            if (args.length == 0) {
                LOGGER.warn("\nNo licence key provided.\n");
                LOGGER.warn("\nusage: ... LatencyChecker DA-LICENCE-KEY\n");
                return;
            }

            Client client = Client.getInstance(new CacheService(new SimpleCacheProvider()));
            client.setLicenceKey(args[0]);

            EndPoint[] endPoints = client.getServersLatencies();
            serversList(endPoints);

            client.getCacheService().shutdown();

            LOGGER.info("\nPlease see https://deviceatlas.com/resources/cloud-service-end-points for more information.\n");
            // catch the exceptions
        } catch (Exception ex) {
            LOGGER.error("check server latency", ex);
        }
    }
}
