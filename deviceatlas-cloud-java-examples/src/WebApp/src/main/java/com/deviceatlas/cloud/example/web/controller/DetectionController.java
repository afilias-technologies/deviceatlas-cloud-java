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

package com.deviceatlas.cloud.example.web.controller;

import com.deviceatlas.cloud.deviceidentification.client.Client;
import com.deviceatlas.cloud.deviceidentification.client.ClientConstants;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

import com.deviceatlas.cloud.deviceidentification.client.ClientException;
import com.deviceatlas.cloud.deviceidentification.client.Result;
import com.deviceatlas.cloud.deviceidentification.endpoint.EndPoint;
import com.deviceatlas.cloud.example.web.service.DetectionService;

/**
 * Controller class to handle the web example requests.
 * 
 * Here we define and map the services and paths that will let us check how the
 * DeviceAtlas Cloud Client works in a web environment.
 * 
 * @author Afilias Technologies Ltd
 */
@Controller
public class DetectionController {

    /**
     * Services
     */
    @Autowired
    private DetectionService detectionService;
    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    /**
     * Index page handler.
     * 
     * Method to map relevant client details such as the API version, licence 
     * key, selected Cloud endpoint and other details that will be visible from 
     * the index page.
     * 
     * All page content will be accessible through the Thymeleaf templates
     * defined in the resources dir.
     * 
     * @param reqHeaders
     * @param model
     * @return 
     */
    @RequestMapping("/")
    public String index(@RequestHeader Map<String, String> reqHeaders, Model model) {

        Result cloudData;
        Map <String, String> headers = new HashMap<String, String>();
        String source = "-";

        try {
            cloudData = detectionService.getCloudResponse(reqHeaders);
            headers = cloudData.getHeaders();
            source = cloudData.getSource();
            source = source.substring(0, 1).toUpperCase() + source.substring(1);
        } catch (ClientException ex) {
            log.error("{}", ex.getMessage());
            model.addAttribute("exceptionMessage", ex.getMessage());
        }

        Client c = detectionService.getClient();
        EndPoint e[] = null;

        try {
            e = c.getEndPoints();
        } catch (ClientException ex) {
            log.error("{}", ex.getMessage());
        }

        String cloudUrl = c.getCloudUrl();
        if (cloudUrl != null) {
            cloudUrl = cloudUrl.replace("/v1/detect/properties", "").replace("http://", "").replace(":80", "");
        } else {
            cloudUrl = "-";
        }

        model.addAttribute("headers", headers);
        model.addAttribute("source", source);
        model.addAttribute("endpoints", e);
        model.addAttribute("cloudUrl", cloudUrl);
        model.addAttribute("apiVersion", c.getVersion());
        model.addAttribute("licenceKey", c.getLicenceKey());
        model.addAttribute("useCache", c.getUseCache());
        model.addAttribute("sendExtraHeaders", c.getSendExtraHeaders());
        model.addAttribute("useClientCookie", c.getUseClientCookie());

        return "index";
    }

}
