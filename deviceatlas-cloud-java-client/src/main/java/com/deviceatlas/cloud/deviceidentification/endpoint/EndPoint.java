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


package com.deviceatlas.cloud.deviceidentification.endpoint;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines a cloud end-point (server).
 *
 * Copyright (c) 2008-2016 by Afilias Technologies Limited. All rights reserved.
 * @author Afilias Technologies Ltd
 */
public class EndPoint {
    /** end-point host base URL */
    public String host;
    /** end-point host port */
    public String port;
    /** end-point host URL path */
    public String path = "/v1/detect/properties";
    /**
     * end-point ranking test results - when the API checks the end-point
     * test request times will be put in this list
     */
    public List<Double> latencies;
    /**
     * end-point ranking test results - when the API checks the end-point
     * the average of test request times will be put in this property
     */
    public double avg;

    /**
     * Creates DeviceAtlas EndPoint object
     */
    public EndPoint() {

    }

    /**
     * Creates DeviceAtlas EndPoint object
     *
     * @param host EndPoint host address
     * @param port EndPoint port number
     */
    public EndPoint(String host, String port) {
        this.host = host;
        this.port = port;
    }    

    /**
     * Creates DeviceAtlas EndPoint object
     *
     * @param host EndPoint host address
     * @param port EndPoint port number
     * @param path EndPoint URL path
     */
    public EndPoint(String host, String port, String path) {
        this.host = host;
        this.port = port;
        this.path = path;
    }

    /**
     * Get server connectable URL
     *
     * @return end-point full URL (host+port+path)
     */
    public String getUrl() {
        return host + ":" + port + path;    
    }

    /**
     * Return a HashMap of EndPoint object data
     *
     * @return HashMap of EndPoint object data
     */
    public Map toMap() {
        Map <String, Object> server = new HashMap <String, Object>();
        server.put("host", host);
        server.put("port", port);
        server.put("path", path);
        server.put("avg",  avg);
        server.put("latencies", latencies);
        return server;
    }

    /**
     * Fill EndPoint object with data from HashMap
     *
     * @param server server-data HashMap
     */
    public void fromMap(Map <String, Object> server) {
        host = (String) server.get("host");
        port = (String) server.get("port");
        path = (String) server.get("path");
        avg  = (Double) server.get("avg");
        latencies = (List<Double>) server.get("latencies");
    }
}
