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

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import com.deviceatlas.cloud.deviceidentification.client.ClientException;
import com.deviceatlas.cloud.deviceidentification.client.Result;
import com.deviceatlas.cloud.example.web.service.DetectionService;
import com.deviceatlas.cloud.example.web.service.RedirectionService;
import java.io.IOException;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller class to handle the web example requests.
 * 
 * Here we define and map the services and paths that will let us check how the
 * DeviceAtlas Cloud Client works in a web environment.
 * 
 * @author Afilias Technologies Ltd
 */
@Controller
public class RedirectionController {

    /**
     * Services and log var.
     */
    @Autowired
    private DetectionService detectionService;
    @Autowired
    private RedirectionService exampleRedirectionService;

    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    /**
     * Redirection example.
     * 
     * This web example uses the API to get properties for the current request 
     * and then uses some basic property values to decide which website provides
     * the most suitable content for the device making the request.
     * 
     * @param reqHeaders
     * @param model
     * @param request
     * @param response 
     */
    @RequestMapping("/redirection")
    public void redirection(@RequestHeader Map<String, String> reqHeaders, Model model, HttpServletRequest request, HttpServletResponse response) {
        Result cloudData = null;
        String exceptionMessage = null;
        
        try {
            cloudData = detectionService.getCloudResponse(reqHeaders);
        } catch (ClientException ex) {
            log.error("{}", ex.getMessage());
            exceptionMessage = ex.getMessage();
        }

        try {
            exampleRedirectionService.redirect(cloudData, request, response, exceptionMessage);
        } catch (IOException ex) {
            log.error("{}", ex.getMessage());
            model.addAttribute("exceptionMessage", ex.getMessage());
        }

    }

    /**
     * Destination path to get redirected from those devices detected as a 
     * desktop device.
     * 
     * @param exceptionMessage
     * @param model
     * @return String
     */
    @RequestMapping("/redirection/desktop")
    public String clientIsADesktop(@RequestParam(value = "exceptionMessage", required = false) String exceptionMessage, Model model) {
        model.addAttribute("exceptionMessage", exceptionMessage);
        return "redirection/desktop";
    }

    /**
     * Destination path to get redirected from those devices detected as a 
     * tablet.
     * 
     * @param exceptionMessage
     * @param model
     * @return 
     */
    @RequestMapping("/redirection/tablet")
    public String clientIsATablet(@RequestParam(value = "exceptionMessage", required = false) String exceptionMessage, Model model) {
        model.addAttribute("exceptionMessage", exceptionMessage);
        return "redirection/tablet";
    }

    /**
     * Destination path to get redirected from those devices detected as a 
     * high-end mobile phone.
     * 
     * @param exceptionMessage
     * @param model
     * @return 
     */
    @RequestMapping("/redirection/mobile_highend")
    public String clientIsAMobileHighEnd(@RequestParam(value = "exceptionMessage", required = false) String exceptionMessage, Model model) {
        model.addAttribute("exceptionMessage", exceptionMessage);
        return "redirection/mobile_highend";
    }

    /**
     * Destination path to get redirected from those devices detected as a 
     * low-end mobile phone.
     * 
     * @param exceptionMessage
     * @param model
     * @return 
     */
    @RequestMapping("/redirection/mobile_lowend")
    public String clientIsAMobileLowEnd(@RequestParam(value = "exceptionMessage", required = false) String exceptionMessage, Model model) {
        model.addAttribute("exceptionMessage", exceptionMessage);
        return "redirection/mobile_lowend";
    }

    /**
     * Destination path to get redirected from those devices detected as a 
     * robot.
     * 
     * @return String
     */
    @RequestMapping("/redirection/robot")
    public String clientIsARobot() {
        return "redirection/robot";
    }

}
