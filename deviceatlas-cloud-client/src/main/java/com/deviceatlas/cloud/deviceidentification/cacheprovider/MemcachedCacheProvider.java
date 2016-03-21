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


package com.deviceatlas.cloud.deviceidentification.cacheprovider;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.net.SocketAddress;
import java.net.InetSocketAddress;

import net.spy.memcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Memcached cache provider.
 *
 * @author Afilias Technologies Ltd
 */

class MemcacheConfig {
    private String host;
    private int port;
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

public class MemcachedCacheProvider implements CacheProvider {
    private static final String   SETTING_FILE      = "/deviceatlas-memcached.ini";
    private MemcachedClient mClient                 = null;
    private static final String  DEFAULT_HOST       = "127.0.0.1";
    private static final int  DEFAULT_EXPIRY        = 3600;
    private static final int  DEFAULT_PORT          = 11211;
    private int                     expiry          = DEFAULT_EXPIRY;
    private List<InetSocketAddress> servers         = new ArrayList<InetSocketAddress>();
    private static final Logger LOGGER              = LoggerFactory.getLogger(MemcachedCacheProvider.class);

    public MemcachedCacheProvider(int expiry) {
        InputStream is = getClass().getResourceAsStream(SETTING_FILE);
        BufferedReader br = null;
        InputStreamReader isr = null;
        if (expiry > 0) {
            this.expiry = expiry;
        }

        try {
            if (is != null) {
                isr = new InputStreamReader(is, "UTF-8");
                br = new BufferedReader(isr);

                parseConfig(br);
            }
        } catch (IOException ex) {
            LOGGER.error("memcached provider reading config attempt", ex);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }

                if (isr != null) {
                    isr.close();
                }
            } catch (IOException ex) {
                LOGGER.error("memcached provider closing streams attempt", ex);
            }
        }

        if (servers.isEmpty()) {
            servers.add(new InetSocketAddress(DEFAULT_HOST, DEFAULT_PORT));
        }

        try {
            mClient = new MemcachedClient(servers);
        } catch (IOException ex) {
            LOGGER.error("memcached provider instantiation of the client attempt", ex);
        }
    }

    public MemcachedCacheProvider() {
        this(0);
    }

    /**
     * Parses a config line
     *
     * @param line
     * @return MecacheConfig
     */

    public MemcacheConfig parseLine(String line) {
        MemcacheConfig mconfig = new MemcacheConfig();
        mconfig.setHost(DEFAULT_HOST);
        mconfig.setPort(DEFAULT_PORT);
        String [] configpart = line.split(" ");

        for (int i = 0; i < configpart.length; i ++) {
            String [] config = configpart[i].split("=");
            if (config.length == 2) {
                String configkey = config[0].trim();
                String configval = config[1].trim();
                if ("host".equalsIgnoreCase(configkey))
                    mconfig.setHost(configval);
                else if ("port".equalsIgnoreCase(configkey)) {
                    mconfig.setPort(Integer.parseInt(configval));
                }
            }
        }

        return mconfig;
    }

    /**
     * Parses config file
     *
     * @param br
     */

    public void parseConfig(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            MemcacheConfig mconfig = parseLine(line);

            servers.add(new InetSocketAddress(mconfig.getHost(), mconfig.getPort()));
        }
    }


    @Override
    public <T> T get(String key) throws CacheException {
        if (mClient != null) {
            try {
                return (T)mClient.get(key);
            } catch (Exception ex) {
                LOGGER.error("get", ex);
            }

            return null;
        }

        throw new CacheException(
                "Failed to get cache entry " + key
                );
    }

    @Override
    public <T> void set(String key, T entry) throws CacheException {
        if (mClient != null) {
            try {
                mClient.set(key, expiry, entry);
            } catch (Exception ex) {
                throw new CacheException(
                        "Failed to put cache entry in " + key, ex
                        );
            }
        }
    }

    @Override
    public void remove(String key) throws CacheException {
        if (mClient != null) {
            try {
                mClient.delete(key);
            } catch (Exception ex) {
                LOGGER.error("remove", ex);
                throw new CacheException(
                        "Failed to remove cache entry in " + key
                        );
            }
        }
    }

    @Override
    public void clear() {
        if (mClient != null) {
            try {
                mClient.flush();
            } catch (Exception ex) {
                LOGGER.error("clear", ex);
            }
        }
    }

    @Override
    public void shutdown() {
        if (mClient != null) {
            mClient.shutdown();
        }
    }

    @Override
    public List <String> getKeys(){
        List keys = null;
        if (mClient != null) {
            keys = new ArrayList();
            Map<SocketAddress,Map<String, String>> stats = mClient.getStats();

            for(Map.Entry<SocketAddress, Map<String, String>> hosts : stats.entrySet()) {
                Map<String, String> listServers = hosts.getValue();
                for (String key : listServers.keySet()) {
                    keys.add(key);
                }
            }
        }

        return keys;
    }

    @Override
    public void setExpiry(int expiry) {
        this.expiry = expiry;
    }
}
