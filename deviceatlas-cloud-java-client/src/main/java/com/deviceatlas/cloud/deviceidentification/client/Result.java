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

import java.util.Map;

public class Result {
    private Map<String, String> headers;
    private String source;
    private Properties properties;

    /**
     * Returns the list of used headers used against the cloud endpoints
     *
     * @return Map&lt;String, String&gt;
     */

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Set the list of headers from the cloud endpoints request
     *
     * @param headers
     */

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Returns the source of the properties either cloud or cache
     *
     * @return String
     */

    public String getSource() {
        return source;
    }

    /**
     * Sets the source of the properties
     *
     * @param source
     */

    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Returns the properties's list
     *
     * @return Properties
     */

    public Properties getProperties() {
        return properties;
    }

    /**
     * Set the properties's list from the cloud endpoint's response
     *
     * @param properties
     */

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
