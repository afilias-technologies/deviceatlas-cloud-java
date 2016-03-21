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

package com.deviceatlas.cloud.example.web;

import com.deviceatlas.cloud.deviceidentification.client.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PathResolver {

    private PathResolver() {
    }

    public static String resolvePath(Properties properties) {

        Logger logger = LoggerFactory.getLogger(PathResolver.class);

        // Is it a non-human request?
        if (properties.contains("isRobot", true) || 
                properties.contains("isChecker", true) || 
                properties.contains("isDownloader", true) || 
                properties.contains("isFilter", true) || 
                properties.contains("isSpam", true) ||
                properties.contains("isFeedReader", true)) {
            return "nonhuman";

        // Is it a tablet?
        } else if (properties.contains("isTablet", true)) {
            return "tablet";

        // Is it an eReader?
        } else if (properties.contains("isEReader", true)) {
            return "ereader";

        // Is it a low-end or high-end mobile device?
        } else {
            String primaryHardwareType = properties.get("primaryHardwareType").asString();
            if (primaryHardwareType.equals("Mobile Phone")) {
                // You can create conditions on various properties to distinguish between
                // low-end and high-end devices
                // * low-end devices which only support WML but not basic XHTML which provides contents wrapped in WML
                Boolean supportsWml = properties.contains("markup.wml1", true);
                Boolean supportsXhtmlBasic = properties.contains("markup.xhtmlBasic10", true);
                // * it's a low-end device
                if (supportsWml && !supportsXhtmlBasic) {
                    return "mobile_lowend";
                    // * it's a high-end mobile device
                } else {
                    return "mobile_highend";
                }
            }

        }

        // If it is none of the device types above, we return the desktop
        // content
        return "desktop";

    }

}
