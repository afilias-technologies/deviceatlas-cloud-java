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

import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

public class RequestBuilderUtils {
    private String host;
    private String licenceKey;
    private String userAgent;

    public RequestBuilderUtils(String host, String licenceKey, String userAgent) {
        this.host = host;
        this.licenceKey = licenceKey;
        this.userAgent = userAgent;
    }

    /**
     * Builds the url to reaches the cloud service
     *
     * @return String
     */
    public String buildCloudUrl() throws UnsupportedEncodingException {
        StringBuilder url = new StringBuilder();
        url.append(host);
        url.append("?licencekey=");
        url.append(URLEncoder.encode(licenceKey, "UTF-8"));
        url.append("&useragent=");
        url.append(URLEncoder.encode(userAgent, "UTF-8"));

        return url.toString();
    }
}

