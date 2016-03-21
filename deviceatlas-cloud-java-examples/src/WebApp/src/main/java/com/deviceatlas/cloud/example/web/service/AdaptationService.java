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
 * Content Adaptation Example using the DeviceAtlas Cloud API.
 *
 * This sample code uses the DeviceAtlas CloudApi to get properties for the
 * device making the current request then uses some basic property values
 * to choose a suitable template to wrap around the contents.
 *
 * Please try this example on a PC and various devices such as mobile phones,
 * tablets, etc. to see how the API works.
 *
 * The Plan: First, let's plan what we want to do and what this example is about.
 * Let's say we have different templates/css/etc. and each one is created for a
 * group of device types. When users visit our website, they will experience a
 * user-interface specially designed for their device.
 * The web-site pages will provide their contents and then they use DeviceAtlas
 * to get the device properties and choose the best user-interface for the device
 * to wrap around the contents.
 * In this example this file gets the request, then used DeviceAtlas to get
 * properties, checks few properties and selects a template to display. There
 * are five templates for desktop, ereader, mobile, tablet and low-end device
 * experience located in the /templates directory. Some of this templates use
 * the properties to further fine-tune the user-interface.
 *
 * Note: Including the DeviceAtlas Client-side Component may be used in this
 * page to give more accurate results.
 * 
 */

package com.deviceatlas.cloud.example.web.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;

import com.deviceatlas.cloud.example.web.PathResolver;
import com.deviceatlas.cloud.deviceidentification.client.Result;
import com.deviceatlas.cloud.deviceidentification.client.Properties;
import com.deviceatlas.cloud.deviceidentification.client.Property;
import com.deviceatlas.cloud.deviceidentification.client.IncorrectPropertyTypeException;

@Service
public class AdaptationService {
    private PathResolver pathResolver;

    /**
     * Example of constants to determine the web content based on the screen
     * dimensions.
     */
    private static final int MIN_SCREEN_WIDTH_LARGE = 900;
    private static final int MIN_SCREEN_WIDTH_MEDIUM = 500;
    private static final String POSTFIX_IMAGE_SMALL = "small";
    private static final String POSTFIX_IMAGE_MEDIUM = "medium";
    private static final String POSTFIX_IMAGE_LARGE = "large";

    /**
     * Returns all parameters needed to use the most appropriate template for
     * the device user.
     * 
     * @param Result cloudData
     * @return Map<String, Object>
     */
    public Map<String, Object> getParamsFromExampleAdaptationService(Result cloudData, Logger logger) {

        Map <String, Object> params = new HashMap<String, Object>();
        Properties properties;

        /* If detection has not been successful, we return the desktop page version */
        if (cloudData == null) {
            params.put("path", "desktop");
            return params;
        }

        /* Based on device properties choose the most suitable template/user-interface */
        properties = cloudData.getProperties();
        params.put("properties", properties);

        String path = pathResolver.resolvePath(properties);
        params.put("path", path);

        properties = sortPropertiesForDisplaying(properties);
        params.put("properties", properties);

        if (path.equals("mobile_highend")) {
            String imageSize = POSTFIX_IMAGE_SMALL;
            try {
                Integer width = properties.containsKey("usableDisplayWidth")?properties.get("usableDisplayWidth").asInteger(): 0;
                if (width > MIN_SCREEN_WIDTH_LARGE) {
                    imageSize = POSTFIX_IMAGE_LARGE;
                } else if (width > MIN_SCREEN_WIDTH_MEDIUM) {
                    imageSize = POSTFIX_IMAGE_MEDIUM;
                }
            } catch (IncorrectPropertyTypeException ex) {
                logger.error("Exception in WebAppService: ", ex);
            }
            params.put("imagePath", "/images/homa-" + imageSize + ".jpg");
        }

        return params;
    }

    /**
     * Rearrangement of the order of a subset of properties placed on top
     * The original order of the properties are not predictable, hence the list
     * of properties below is placed then all the rest after.
     * @param Result properties
     * @return Map<String, Object>
     */
    private Properties sortPropertiesForDisplaying(Properties properties) {

        if (properties == null) {
            return properties;
        }

        Properties sortedProperties = new Properties();

        // show this properties above others
        String[] top = {
            "vendor",
            "model",
            "marketingName",
            "yearReleased",
            "primaryHardwareType",
            "displayWidth",
            "displayHeight",
            "touchScreen"
        };

        for (int i=0, l=top.length; i<l; i++) {
            if (properties.containsKey(top[i])) {
                sortedProperties.put(top[i], properties.get(top[i]));
                properties.remove(top[i]);
            }
        }

        for (Map.Entry<String, Property> entry : properties.entrySet()) {
            sortedProperties.put(entry.getKey(), entry.getValue());
        }

        return sortedProperties;

    }
}
