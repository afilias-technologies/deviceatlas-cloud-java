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


/**
 * Redirection Example using the DeviceAtlas Cloud API.
 *
 * This example code uses the DeviceAtlas Cloud API to get properties for the
 * current request then uses some basic property values to decide which
 * URL provides the most suitable content for the device making the request.
 *
 * Please try this example on a PC and various devices such as mobile phones,
 * tablets, etc. to see how the API works.
 *
 * Scenario: First, let's plan what we want to do and what this example is about.
 * Let's say we have different web sites and each one is created to give the best
 * content and user experience to a specific device type. Users will enter our
 * main web-site URL in their browsers, the source code which gets this first
 * request will use DeviceAtlas DeviceApi to detect the device type, then based
 * on the conclusions, the user will be redirected to the web-site which provides
 * the best experience for her/his device.
 *
 * Note: Including the DeviceAtlas Client-side Component to this page will give
 * more accurate results.
 *
 */

package com.deviceatlas.cloud.example.web.service;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.io.IOException;

import com.deviceatlas.cloud.example.web.PathResolver;
import com.deviceatlas.cloud.deviceidentification.client.Result;
import com.deviceatlas.cloud.deviceidentification.client.Properties;

@Service
public class RedirectionService {
    private PathResolver pathResolver;

    public void redirect(Result cloudData, HttpServletRequest request, HttpServletResponse response, String exceptionMessage) throws IOException {
        String redirectionPath = "desktop";
        if (cloudData != null) {
            Properties properties = cloudData.getProperties();
            redirectionPath = pathResolver.resolvePath(properties);
        }
        String url = request.getContextPath() + "/redirection/" + redirectionPath;
        if (exceptionMessage != null) {
            url += "?exceptionMessage=" + URLEncoder.encode(exceptionMessage, "UTF-8");
        }
        response.sendRedirect(url);
    }

}
