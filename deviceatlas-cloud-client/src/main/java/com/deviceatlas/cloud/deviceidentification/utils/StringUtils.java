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

import com.deviceatlas.cloud.deviceidentification.client.ClientException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;

import java.util.HashMap;
import java.util.Map;

public class StringUtils {
    private StringUtils() {
    }
    /**
     * Creates an MD5 hash from a string.
     */
    public static String md5(String toDigest) throws ClientException {
        StringBuilder md5String = new StringBuilder();

        try {
            byte[] input = toDigest.getBytes("UTF-8");
            MessageDigest digester = MessageDigest.getInstance("MD5");
            digester.reset();
            digester.update(input);
            byte[] messageDigest = digester.digest();
            for (int i = 0; i < messageDigest.length; i++) {
                md5String.append(Integer.toHexString(0xFF & messageDigest[i]));
            }

        } catch (NoSuchAlgorithmException ex) {
            throw new ClientException("Error creating Md5 from user-agent: ", ex);
        } catch (UnsupportedEncodingException ex) {
            throw new ClientException("Error creating Md5 from user-agent: ", ex);
        }

        return md5String.toString();
    }

    /**
     * Normalize the keys in the passed in key value map. This lower-cases the
     * keys, replaces "_" with "-" and removes any HTTP_ prefix.
     * @param keyVals - Key Values
     * @return Map
     */
    public static Map<String, String> normaliseKeys(Map<String, String> keyVals) {
        Map<String, String> normalised = new HashMap<String, String>();

        for (Map.Entry<String, String> keys : keyVals.entrySet()) {
            String value = keys.getValue();
            String key   = keys.getKey().toLowerCase().replace("_", "-");
            // if it was routed through Apache it may have a HTTP_ prefix
            // remove this... it was already lowercased and _ replaced with -
            if(key.startsWith("http-")) {
                key = key.substring(5);
            }
            normalised.put(key, value);
        }

        return normalised;
    }
}
