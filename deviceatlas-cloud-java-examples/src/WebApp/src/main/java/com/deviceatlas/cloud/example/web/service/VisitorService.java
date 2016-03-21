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
import org.slf4j.Logger;
import com.deviceatlas.cloud.deviceidentification.client.Result;
import com.deviceatlas.cloud.deviceidentification.client.Properties;

@Service
public class VisitorService {

    public Properties getProperties(
            DetectionService detectionService,
            Result cloudData,
            Logger log) {

        // You already have all properties in 
        // properties = cloudData.getProperties();
        
        // However, if you want to modify your settings to get properties again,
        // you could do the following:
        // Client client  = detectionService.getClient();
        
        /* device data cache - turn off */
        // client.setUseCache(false);
 
        /* END POINT SETTINGS > > > */

        /* Set cloud end points manually */
        // Server[] servers = {
        //  new Server("http://region0.deviceatlascloud.com", "80"),
        //  new Server("http://region1.deviceatlascloud.com", "80"),
        //  new Server("http://region2.deviceatlascloud.com", "80"),
        //  new Server("http://region3.deviceatlascloud.com", "80"),
        // };
        // client.setServers(servers);
        
        /* turn off auto ranking */
        // client.setAutoServerRanking(false);

        /* if auto ranking is off and top server fails move the faulty server to the end of the servers list for this amount of minutes */
        // client.setCloudServiceTimeout(180);

        /* if auto ranking is on re rank list after each 1000 minutes */
        // client.setServerRankingLifetime(1000);

        /* if auto ranking is on - rank end points manually and put cache the list */
        // try {
        //  cient.rankServers();
        // } catch (DaClientException ex) {
        //  log.error("{}", ex.getMessage());
        // }

        /* < < < END POINT SETTINGS */

        Properties properties = null;

        if (cloudData != null) {
            properties = cloudData.getProperties();
        }

        return properties;

    }

}
