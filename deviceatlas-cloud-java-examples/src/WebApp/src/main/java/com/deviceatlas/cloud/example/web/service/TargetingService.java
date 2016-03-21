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
 * Content Targeting Example using the DeviceAtlas Cloud API.
 *
 * Scenario: First, let's plan what we want to do and what this example is about.
 * In this example we want to show two samples of content targeting.
 *
 * (1) Advertising, we have a database which contains our ads and a knowledge base
 *     that includes some logic that relates the ads to property values. For example
 *     our knowledge base would tell us "people who have old and low-end phones
 *     are more likely to be interested in new high-end smart phones" or "those
 *     who have specific devices (brand, model, type of device, etc.) would probably
 *     be interested in specific devices or accessories". DeviceAtlas can be used
 *     to get device properties, then used against the knowledge base to get the
 *     set of ads the user would probably be interested in. In this example we
 *     use a small array and a few if conditions to mimic the knowledge base.
 *
 * (2) Downloading an app, we have an app written in different platforms, when a
 *     user comes to download the app we can use DeviceAtlas to detect user's
 *     operating system and automatically show him the link to the app which is
 *     created for her/his device.
 * 
 */

package com.deviceatlas.cloud.example.web.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.slf4j.Logger;

import com.deviceatlas.cloud.deviceidentification.client.IncorrectPropertyTypeException;
import com.deviceatlas.cloud.deviceidentification.client.Result;
import com.deviceatlas.cloud.deviceidentification.client.Properties;

@Service
public class TargetingService {

    public Map<String, Object> getParamsFromExampleTargetingService(Result cloudData, Logger log) {

        Set<String> toAdvertise = new HashSet<String>();
        Map<String, String> downloadLinks = new HashMap<String, String>();
        Map<String, Object> params = new HashMap<String, Object>();
        boolean cloudError = (cloudData == null);

        /* 
         * Advertise the selected ads to be shown to the user will be placed in 
         * this array
         */        
        setAdvertisements(toAdvertise, cloudData, cloudError, log);
        params.put("toAdvertise", toAdvertise);
        
        /* 
         * Set the most sutable App download links for the device 
         */
        setDownloadLinks(downloadLinks, cloudData, cloudError);
        params.put("downloadLinks", downloadLinks);

        return params;
    }

    private void setAdvertisements(Set<String> toAdvertise, Result cloudData, boolean cloudError, Logger log) {
        if (cloudData != null && !cloudError) {
            Properties properties = cloudData.getProperties();
            advertise(toAdvertise, properties, log);
        }
    }

    private void advertise(Set<String> toAdvertise, Logger log) {
        advertise(toAdvertise, null, log);
    }

    private void advertise(Set<String> toAdvertise, Properties properties, Logger log) {

        if (properties == null) {
            putAdvertise(toAdvertise, "random");
            return;
        }

        // get properties from DeviceAtlas
        try {
            String vendor = properties.containsKey("vendor")? properties.get("vendor").asString().toLowerCase(): "";
            String marketingName = properties.containsKey("marketingName")? properties.get("marketingName").asString(): "";
            int yearReleased = properties.containsKey("yearReleased")? properties.get("yearReleased").asInteger(): 0;

            // lets target Samsung devices
            if (vendor.equals("samsung")) {

                // if the device is an older model smart-phone, lets add the newer similar devices 
                if (marketingName.contains("galaxy s") && !marketingName.contains("galaxy s5")) {
                    putAdvertise(toAdvertise, "samsung", "s5");
                    // probably an Android guy, lets advertise similar Android devices
                    putAdvertise(toAdvertise, "android");

                    // this guy probably likes big mobiles, he has an older one, lets add the newer model
                } else if (marketingName.contains("galaxy note") && !marketingName.contains("galaxy note 3")) {
                    putAdvertise(toAdvertise, "samsung", "note3");

                    // no more clues! let's add Samsung devices
                } else {
                    putAdvertise(toAdvertise, "samsung");

                }

                // lets target Apple devices
            } else if (vendor.equals("apple")) {

                // if the device is an older model lets add the newer models
                if (marketingName.contains("iphone") && !marketingName.contains("iphone 5")) {
                    putAdvertise(toAdvertise, "apple", "iphone5");
                    // this guy is probably not interested in Android, but lets offer an HTC
                    putAdvertise(toAdvertise, "htc");

                    // no more clues! let's add Apple devices
                } else {
                    putAdvertise(toAdvertise, "apple");

                }

                // is the device to old? lets show some our new smart phones 
            } else if (yearReleased !=0 && yearReleased < 2011) {
                putAdvertise(toAdvertise, "newest");

                // not included in our target, lets show some random ads
            } else {
                putAdvertise(toAdvertise, "random");

            }
        } catch (IncorrectPropertyTypeException ex) {
            log.error("advertise", ex);
        }

    }

    /**
     * This function simulates the knowledge base and logics which select ads based
     * on property values.
     * Puts advertise into toAdvertise, relevant to the keywords
     */
    private void putAdvertise(Set toAdvertise, String keyword1) {
        putAdvertise(toAdvertise, keyword1, null);
    }

    private void putAdvertise(Set toAdvertise, String keyword1, String keyword2) {
        // let's mimic the data base in a Map

        Map<String, Map<String, String>> ads = new HashMap<String, Map<String, String>>();

        Map<String, String> apple = new HashMap<String, String>();
        apple.put("iphone5s", "Apple iPhone 5s");
        apple.put("ipadmini2", "Apple iPad mini 2");

        Map<String, String> htc = new HashMap<String, String>();
        htc.put("htc1", "HTC One");
        htc.put("desire", "HTC Desire 310");

        Map<String, String> samsung = new HashMap<String, String>();
        samsung.put("s5", "Samsung Galaxy S5");
        samsung.put("note3", "Samsung Galaxy Note 3");

        Map<String, String> sony = new HashMap<String, String>();
        sony.put("z", "Sony Xperia Z");
        sony.put("m", "Sony Xperia M");

        Map<String, String> android = new HashMap<String, String>();
        android.put("htc1", "HTC One");
        android.put("desire", "HTC Desire 310");
        android.put("s5", "Samsung Galaxy S5");
        android.put("note3", "Samsung Galaxy Note 3");
        android.put("z", "Sony Xperia Z");
        android.put("m", "Sony Xperia M");

        Map<String, String> newest = new HashMap<String, String>();
        newest.put("0", "HTC One");
        newest.put("2", "Samsung Galaxy S5");
        newest.put("4", "Sony Xperia Z");
        newest.put("6", "Apple iPhone 5s");
        newest.put("7", "Apple iPad mini 2");

        ads.put("apple", apple);
        ads.put("htc", htc);
        ads.put("samsung", samsung);
        ads.put("sony", sony);
        ads.put("android", android);
        ads.put("newest", newest);

        String[] randomAds = {
            "HTC One",
            "Samsung Galaxy S5",
            "Sony Xperia Z",
            "Apple iPhone 5s",
            "Apple iPad mini 2",
            "Sony Xperia M",
            "Samsung Galaxy Note 3"
        };

        // let's make some simple logics with ifs
        boolean adsContainsKeyword1 = ads.containsKey(keyword1);

        if (keyword2 != null && adsContainsKeyword1 && ads.get(keyword1).containsKey(keyword2)) {
            String item = ads.get(keyword1).get(keyword2);
            toAdvertise.add(item);
        } 

        if (adsContainsKeyword1) {
            for (Map.Entry<String, String> entry : ads.get(keyword1).entrySet()) {
                toAdvertise.add(entry.getValue());
            }
        } else {
            // pick randomly
            for (byte i=0; i<4; i++) {
                int j = new Random().nextInt(randomAds.length);
                toAdvertise.add(randomAds[j]);
            }
        }
    }

    private void setDownloadLinks(Map<String, String> downloadLinks, Result cloudData, boolean cloudError) {

        // all available download links for our app
        Map<String, String> allDownloadLinks = new HashMap<String, String>();
        allDownloadLinks.put("Android" , "#download-android-app");
        allDownloadLinks.put("Bada" , "#download-bada-app");
        allDownloadLinks.put("iOS" , "#download-ios-app");
        allDownloadLinks.put("RIM" , "#download-rim-app");
        allDownloadLinks.put("Symbian" , "#download-symbian-app");
        allDownloadLinks.put("Windows Mobile" , "#download-windows-mobile-app");
        allDownloadLinks.put("Windows Phone" , "#download-windows-phone-app");
        allDownloadLinks.put("Windows RT" , "#download-windows-rt-app");
        allDownloadLinks.put("webOS" , "#download-webos-app");
        allDownloadLinks.put("Windows" , "#download-desktop-windows-app");
        allDownloadLinks.put("Linux" , "#download-desktop-linux-app");
        allDownloadLinks.put("Mac" , "#download-desktop-mac-app");

        if (cloudData == null || cloudError) {
            downloadLinks = allDownloadLinks;
        } else {
            Properties properties = cloudData.getProperties();
            downloadLinks(downloadLinks, allDownloadLinks, properties);
        }

    }

    private void downloadLinks(Map<String, String> downloadLinks, Map<String, String> allDownloadLinks, Properties properties) {

        // if osName is detected
        if (properties.containsKey("osName")) {

            String osName = properties.get("osName").asString();

            // try to find desktop os names
            if (osName.contains("linux")) {
                osName = "Linux";
            } else if (osName.contains("mac") || osName.contains("apple")) {
                osName = "Mac";
            } else if (osName.contains("win")) {
                osName = "Windows";
            }
            // get the download link for the os
            String link = allDownloadLinks.get(osName);
            if (link != null) {
                downloadLinks.put(osName, link);
            }
        }

    }

}
