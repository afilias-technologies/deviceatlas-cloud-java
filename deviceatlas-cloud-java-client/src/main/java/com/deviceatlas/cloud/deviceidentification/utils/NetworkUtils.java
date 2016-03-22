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

package com.deviceatlas.cloud.deviceidentification.utils;

import com.deviceatlas.cloud.deviceidentification.client.HeaderConstants;
import com.deviceatlas.cloud.deviceidentification.client.ClientConstants;

import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.zip.GZIPInputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkUtils {
    private   Proxy proxy;
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkUtils.class);

    public NetworkUtils(Proxy proxy) {
        this.proxy = proxy;
    }

    public StringBuilder setConnectionResponse(BufferedReader br, StringBuilder data) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            data.append(line);
        }
        
        return data;
    }

    /**
     * Set the returns data after getting the connection response
     *
     * @param connectionRet
     * @param inputStream
     * @param ok
     * @param results
     */

    public void setConnectionRet(Map<String, Object> connectionRet, InputStream inputStream,
            boolean ok, String results) {
        if (!ok) {
            connectionRet.put(ClientConstants.CLOUD_SERVICE_MESSAGE.toString(), results);
        } else if ("".equals(results) || inputStream == null) {
            connectionRet.put(ClientConstants.CLOUD_SERVICE_MESSAGE.toString(), "Returned empty!");
        } else {
            connectionRet.put(ClientConstants.CLOUD_SERVICE_RESULT.toString(), results);
        }
    }

    /**
     * Gets the connection response and set the data accordingly
     *
     * @param conn
     * @param connectionRet
     */

    public void setConnectionData(HttpURLConnection conn, Map<String, Object> connectionRet) {
        final String connectCloudError      = "connect cloud";
        BufferedReader br                   = null;
        int            status               = 0;
        // get response
        StringBuilder data                  = new StringBuilder();
        try {
            status = conn.getResponseCode();
            connectionRet.put(ClientConstants.CLOUD_SERVICE_STATUS.toString(), status);

            boolean ok = status / 100 == 2;
            InputStream inputStream = ok? conn.getInputStream(): conn.getErrorStream();

            if (inputStream != null) {
                if ("gzip".equals(conn.getContentEncoding())) {
                    inputStream = new GZIPInputStream(inputStream);
                }
                // read the body...
                br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                setConnectionResponse(br, data);
                br.close();
            }

            setConnectionRet(connectionRet, inputStream, ok, data.toString());
        } catch (IOException ex) {
            LOGGER.error(connectCloudError, ex);

        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    LOGGER.error(connectCloudError, ex);
                }
            }
        }
    }

    /**
     * Reaches the cloud service and returns the data accordingly
     *
     * @param url
     * @param headers
     * @param timeout
     * @return Map
     */

    public Map<String, Object> setConnection(String url, Map<String, String> headers, int timeout) {
        final String connectCloudError      = "connect cloud";
        Map<String, Object> connectionRet   = new HashMap<String, Object>();
        connectionRet.put(ClientConstants.CLOUD_SERVICE_STATUS.toString(), 0);
        connectionRet.put(ClientConstants.CLOUD_SERVICE_MESSAGE.toString(), "");

        try {
            URL service = new URL(url);
            HttpURLConnection conn = proxy != null ? (HttpURLConnection) service.openConnection(proxy) : (HttpURLConnection) service.openConnection();
            conn.setConnectTimeout(timeout * 1000);
            // add headers
            conn.addRequestProperty("Accept-Encoding", "gzip");
            conn.addRequestProperty("Accept",          "application/json");
            conn.addRequestProperty("User-Agent",      "Java/"+ ClientConstants.API_VERSION);

            // add the end user headers, prefixing with DA prefix
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.addRequestProperty(
                            HeaderConstants.DA_HEADER_PREFIX.toString() + entry.getKey(),
                            entry.getValue()
                            );
                }
            }

            conn.connect();
            setConnectionData(conn, connectionRet);
        } catch (IOException ex) {
            LOGGER.error(connectCloudError, ex);

        }

        return connectionRet;
    }
}
