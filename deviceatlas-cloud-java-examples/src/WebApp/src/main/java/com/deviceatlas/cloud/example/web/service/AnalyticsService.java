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


package com.deviceatlas.cloud.example.web.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import org.slf4j.Logger;
import java.io.IOException;

import com.deviceatlas.cloud.deviceidentification.client.ClientConstants;
import com.deviceatlas.cloud.deviceidentification.client.Result;
import com.deviceatlas.cloud.deviceidentification.client.Properties;
import com.deviceatlas.cloud.deviceidentification.client.ClientException;
import com.deviceatlas.cloud.deviceidentification.client.IncorrectPropertyTypeException;
import com.deviceatlas.cloud.example.web.service.DetectionService;
import java.util.Comparator;


@Service
public class AnalyticsService {
    
    private static final String USERAGENTS_FILE_TO_ANALYSE = "/static/ua.data";

    public Map<String, Object> getParamsFromExampleAnalyticsService(DetectionService detectionService, Logger log) {
        int total = 0;
        int trafficDesktopBrowsers = 0;
        int totalMobileTraffic = 0;
        int trafficByMobileVendorTotal = 0;
        int trafficByOsTotal = 0;

        Map<String, Integer> trafficMobileDevices = new TreeMap<String, Integer>();
        Map<String, Integer> trafficByMobileVendor = new TreeMap<String, Integer>();
        Map<String, Integer> trafficByOs = new TreeMap<String, Integer>();
        Map<String, Object> params = new HashMap<String, Object>();
        Result cloudData;
        Properties properties = null;

        BufferedReader userAgentsReader = null;
        InputStream userAgentsFile = null;
        String userAgent;

        // Open a list of user-agents which the analysis will be done on
        try {
            userAgentsFile = getClass().getResourceAsStream(USERAGENTS_FILE_TO_ANALYSE);
            userAgentsReader = new BufferedReader(new InputStreamReader(userAgentsFile));

            while ((userAgent = userAgentsReader.readLine()) != null) {
                userAgent = userAgent.trim();

                // Get properties from the user-agent
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-Agent", userAgent);

                try {
                    cloudData = detectionService.getCloudResponse(headers);
                } catch (ClientException ex) {
                    log.error("{}", ex.getMessage());
                    params.put("exceptionMessage", ex.getMessage());
                    continue;
                }

                properties = cloudData.getProperties();

                if (properties == null) {
                    continue;
                }

                // If it's a browser add one to the browsers count
                if (properties.containsKey("isBrowser") && properties.get("isBrowser").asBoolean()) {
                    trafficDesktopBrowsers++;

                // If it's a mobile device check the type
                } else if (properties.containsKey("mobileDevice") && properties.get("mobileDevice").asBoolean()) {
                    totalMobileTraffic++;

                    // aggregate mobile device types
                    String primaryDeviceType = properties.containsKey("primaryHardwareType")?
                        properties.get("primaryHardwareType").asString(): "Other";

                    trafficMobileDevices.put(primaryDeviceType,
                        trafficMobileDevices.containsKey(primaryDeviceType)?
                            trafficMobileDevices.get(primaryDeviceType) + 1: 1);

                    // detect device vendor and aggregate it
                    String vendor = properties.containsKey("vendor")? properties.get("vendor").asString(): null;
                    if (vendor != null) {
                        trafficByMobileVendorTotal++;
                        trafficByMobileVendor.put(vendor,
                            trafficByMobileVendor.containsKey(vendor)?
                                trafficByMobileVendor.get(vendor) + 1: 1);
                    }
                }

                // detect the operating system aggregate it
                String os = properties.containsKey("osName")? properties.get("osName").asString(): null;
                if (os != null) {
                    trafficByOsTotal++;
                    trafficByOs.put(os,
                        trafficByOs.containsKey(os)?
                            trafficByOs.get(os) + 1: 1);
                }

            } // end while

            total = trafficDesktopBrowsers + totalMobileTraffic;
            if (total == 0) {
                total = 1;
            }

        } catch (IOException ex) {
            log.error("{}", ex.getMessage());
            params.put("exceptionMessage", ex.getMessage());
            return params;
        } catch (IncorrectPropertyTypeException ex) {
            log.error("{}", ex.getMessage());
            params.put("exceptionMessage", ex.getMessage());
            return params;
        } finally {
            if (userAgentsReader != null) {
                try {
                    userAgentsReader.close();
                } catch (IOException ex) {
                    log.error("{}", ex.getMessage());
                    params.put("exceptionMessage", ex.getMessage());
                }
            }

            if (userAgentsFile != null) {
                try {
                    userAgentsFile.close();
                } catch (IOException ex) {
                    log.error("{}", ex.getMessage());
                    params.put("exceptionMessage", ex.getMessage());
                }
            }
        }

        trafficMobileDevices = sortByValues(trafficMobileDevices);
        trafficByMobileVendor = sortByValues(trafficByMobileVendor);
        trafficByOs = sortByValues(trafficByOs);
        
        params.put("properties", properties);
        params.put("totalMobileTraffic", totalMobileTraffic);
        params.put("trafficDesktopBrowsers", trafficDesktopBrowsers);
        params.put("trafficMobileDevices", trafficMobileDevices);
        params.put("trafficByMobileVendor", trafficByMobileVendor);
        params.put("trafficByMobileVendorTotal", trafficByMobileVendorTotal);
        params.put("trafficByOs", trafficByOs);
        params.put("trafficByOsTotal", trafficByOsTotal);
        params.put("total", total);

        return params;
    }

    /**
     * Returns a map sorted in descending order by value.
     * 
     * @param Map
     * @return Map
     */
    private static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator = new Comparator<K>() {
            public int compare(K k1, K k2) {
                int compare = map.get(k1).compareTo(map.get(k2));
                if (compare == 0) {
                    return 1;
                } else {
                    return -compare;
                }
            }
        };
        Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
  }

}
