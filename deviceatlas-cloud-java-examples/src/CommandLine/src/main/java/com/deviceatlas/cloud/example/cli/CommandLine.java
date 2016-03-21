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

package com.deviceatlas.cloud.example.cli;

import com.deviceatlas.cloud.deviceidentification.client.Client;
import com.deviceatlas.cloud.deviceidentification.client.ClientException;
import com.deviceatlas.cloud.deviceidentification.client.Result;
import com.deviceatlas.cloud.deviceidentification.client.Properties;
import com.deviceatlas.cloud.deviceidentification.client.Property;
import com.deviceatlas.cloud.deviceidentification.service.CacheService;
import com.deviceatlas.cloud.deviceidentification.cacheprovider.CacheException;
import com.deviceatlas.cloud.deviceidentification.cacheprovider.FileCacheProvider;

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLine {
    private static Client client                    = null;
    private static Result response        = null;
    private static int cnt                          = 0;

    private static final String reqUa               = "Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaN95_8GB/15.0.015; Profile/MIDP-2.0 Configuration/CLDC-1.1 )AppleWebKit/413 (KHTML, like Gecko) Safari/413";
    private static Map<String, String> reqHeaders   = new HashMap<String, String>();
    private static String licencekey                = null;
    private static final Logger LOGGER              = LoggerFactory.getLogger(CommandLine.class);

    static {
        reqHeaders.put("User-Agent", "Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaN95_8GB/15.0.015; Profile/MIDP-2.0 Configuration/CLDC-1.1 )AppleWebKit/413 (KHTML, like Gecko) Safari/413");
        reqHeaders.put("Accept-Language", "en-US, en; q=0.5");
        reqHeaders.put("X-Profile", "http://nds.nokia.com/uaprof/NN95_8GB-1r100.xml");
    }

    private static void displayResponse() {
        Map<String, String> headers = response.getHeaders();
        String source = response.getSource();
        Properties properties = response.getProperties();
        LOGGER.info(headers.toString());
        LOGGER.info(properties.toString());
        LOGGER.info("source = {}", source);
    }

    private static void requestSingleUa() throws ClientException {
        response = client.getResult(reqUa);
        displayResponse();
    }

    private static void requestMapHeaders() throws ClientException {
        response = client.getResult(reqHeaders);
        displayResponse();
    }

    private static void clientSetup() throws CacheException {
        client = Client.getInstance(new FileCacheProvider());
        client.setLicenceKey(licencekey);
    }

    public static void main(String [] args) {
        licencekey = System.getProperty("licencekey");
        if (licencekey == null || licencekey.length() == 0) {
            LOGGER.error("licence key must be provided");
        } else {
            try {
                clientSetup();
                requestSingleUa();
                requestMapHeaders();
            } catch (ClientException ex) {
                LOGGER.error("client error: {}", ex.getMessage());
            } catch (CacheException ex) {
                LOGGER.error("cache error: {}", ex.getMessage());
            }
        }
    }
}
