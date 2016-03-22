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

import com.deviceatlas.cloud.deviceidentification.client.ClientConstants;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.channels.OverlappingFileLockException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File cache provider. 
 *
 * @author Afilias Technologies Ltd
 */
public class FileCacheProvider implements CacheProvider {
    private static final String SETTING_FILE        = "/deviceatlas-filecache.ini";
    private static final String TMP_DIR             = System.getProperty("java.io.tmpdir");
    private static final String CACHE_FILE_EXT      = ".dat";
    private static final String ROOT_NAME           = "DeviceAtlasCloud_" + FileCacheProvider.class.getSimpleName();
    private static final int    DEFAULT_EXPIRY      = 3600;
    private static final Logger LOGGER              = LoggerFactory.getLogger(FileCacheProvider.class);
    private Map<String, Object> cache               = new ConcurrentHashMap<String, Object>(1024);    
    private int                 expiry              = DEFAULT_EXPIRY;
    private boolean             set                 = false;
    private String              directory           = TMP_DIR;
    private Path                rootPath;
    private File                rootFile;

    public FileCacheProvider() {
        InputStream is = getClass().getResourceAsStream(SETTING_FILE);
        BufferedReader br = null;
        InputStreamReader isr = null;

        try {
            if (is != null) {
                isr = new InputStreamReader(is, "UTF-8");
                br = new BufferedReader(isr);
                parseConfig(br);
            }
        } catch (IOException ex) {
            LOGGER.error("file cache provider reading attempt", ex);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }

                if (isr != null) {
                    isr.close();
                }
            } catch (IOException ex) {
                LOGGER.error("file cache provider closing streams attempt", ex);
            }
        }

        definePaths();
    }

    /**
     * Parses the config file
     *
     * @param br
     */

    public void parseConfig(BufferedReader br) throws IOException {
        String line;

        while ((line = br.readLine()) != null) {
            String [] config = line.split("=");
            if (config.length == 2 &&
                    "directory".equalsIgnoreCase(config[0].trim())) {
                directory = config[1].trim();
            }
        }
    }

    /**
     * Define the root path
     *
     */
    public void definePaths() {
        rootPath = Paths.get(directory, FileCacheProvider.ROOT_NAME);
        rootFile = new File(rootPath.toString());

        if (!rootFile.exists()) {
            try {
                Files.createDirectories(rootPath);
            } catch (IOException ex) {
                LOGGER.error("file cache provider creating directories attempt", ex);
            }
        }

        rootFile = new File(rootPath.toString());
        if (rootFile.exists()) {
            getCacheFileEntries(rootFile);
            set = true;
        }
    }

    /**
     * Sets a file cache entry
     *
     * @param current
     * @param path
     */

    public void setCacheFileEntry(File current, Path path) {
        final String setCacheFileEntryError = "get cache file entries";
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        String pathPrefix = path.toString();
        int pathIndex = pathPrefix.indexOf(CACHE_FILE_EXT);

        pathPrefix = pathPrefix.substring(0, pathIndex);

        if (!new File(pathPrefix).exists()) {
            return;
        }

        try {
            fis = new FileInputStream(current);
            ois = new ObjectInputStream(fis);

            cache.put(pathPrefix, ois.readObject());
        } catch (IOException ex) {
            LOGGER.error(setCacheFileEntryError, ex);
        } catch (ClassNotFoundException ex) {
            LOGGER.error(setCacheFileEntryError, ex);
        } catch (Exception ex) {
            LOGGER.error(setCacheFileEntryError, ex);
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException ex) {
                    LOGGER.error("object input stream", ex);
                }
            }

            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    LOGGER.error("file input stream", ex);
                }
            }
        }
    }

    /**
     * Getting all already written cache entries from the various binary files
     */

    private void getCacheFileEntries(File dir) {
        String [] subentries = dir.list();

        if (subentries != null) {
            for (int i = 0; i < subentries.length; i ++) {
                Path path = Paths.get(dir.toString(), subentries[i]);
                File current = new File(path.toString());
                if (current.isDirectory()) {
                    getCacheFileEntries(current);
                } else if (subentries[i].endsWith(CACHE_FILE_EXT)) {
                    setCacheFileEntry(current, path);
                }
            }
        }
    }

    /**
     * Returns the path from the given key
     *
     * @param key
     * @return Path
     */

    public Path getCachePath(String key) throws IOException {
        Path cachePath;
        File dir;

        if (!key.equals(ClientConstants.CACHE_NAME_SERVERS_AUTO.toString()) &&
                !key.equals(ClientConstants.CACHE_NAME_SERVERS_MANUAL.toString())) {
            cachePath = Paths.get(rootPath.toString(), key.substring(0, 2), key.substring(2, 4));
            key = key.substring(4);
        } else {
            cachePath = rootPath;
        }

        dir = new File(cachePath.toString());
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create " + cachePath + " directory");
        }
        cachePath = Paths.get(cachePath.toString(), key + CACHE_FILE_EXT);

        return cachePath;
    }

    @Override
    public <T> T get(String key) throws CacheException {
        if (!set) {
            throw new CacheException(
                    "Failed to get cache entry in " + key +
                    ", (get) cache not set"
                    );
        }

        try {
            Path cachePath = getCachePath(key);
            File f = new File(cachePath.toString());
            if (!f.exists()) {
                return null;
            }

            BasicFileAttributes attrs = Files.readAttributes(cachePath, BasicFileAttributes.class);
            FileTime time = attrs.lastModifiedTime();
            Date now = new Date();

            if ((now.getTime() - time.toMillis()) < (this.expiry * 1000)) {
                return (T)cache.get(key);
            } else {
                remove(key);
            }
        } catch (IOException ex) {
            LOGGER.error("get key", ex);
        }

        return null;
    }

    /**
     * Applies a lock to a cache's file
     *
     * @param key
     * @param entry
     */

    public synchronized <T> void lockAndSet(String key, T entry) throws CacheException {
        final String lockAndSetError = "Failed to put cache entry in ";
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        try {
            Path cachePath = getCachePath(key);

            fos = new FileOutputStream(cachePath.toString());
            FileChannel fch = fos.getChannel();
            FileLock flk = fch.lock();
            oos = new ObjectOutputStream(fos);

            oos.writeObject(entry);

            flk.release();
        } catch (IOException ex) {
            throw new CacheException(
                    lockAndSetError + key, ex
                    );
        } catch (OverlappingFileLockException ex) {
            throw new CacheException(
                    lockAndSetError + key, ex
                    );
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException ex) {
                    LOGGER.error("object output stream", ex);
                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    LOGGER.error("file output stream", ex);
                }
            }
        }
    }

    @Override
    public <T> void set(String key, T entry) throws CacheException {
        if (!set) {
            throw new CacheException(
                    "Failed to put cache entry in " + key +
                    ", (set) cache not set"
                    );
        }

        cache.put(key, entry);
        lockAndSet(key, entry);
    }

    @Override
    public void remove(String key) throws CacheException {
        if (!set) {
            throw new CacheException(
                    "Failed to remove cache entry in " + key +
                    ", (remove) cache not set"
                    );
        }
        cache.remove(key);

        try {
            Path cachePath = getCachePath(key);
            File cacheFile = new File(cachePath.toString());
            if (cacheFile.exists() && !cacheFile.delete()) {
                throw new CacheException(
                        "Failed to remove entry " + key
                        );
            }
        } catch (IOException ex) {
            LOGGER.error("remove", ex);
        }
    }

    @Override
    public void clear() {
        for (String key : cache.keySet()) {
            try {
                remove(key);
            } catch (CacheException ex) {
                LOGGER.error("clear", ex);
            }
        }
    }

    /**
     * No shutdown operation for this class
     *
     */
    @Override
    public void shutdown() {
        return;
    }

    @Override
    public List <String> getKeys() {
        List <String> keys = new ArrayList<String>(cache.keySet().size());
        for (String key : cache.keySet()) {
            keys.add(key);
        }

        return keys;
    }

    @Override
    public void setExpiry(int expiry) {
        this.expiry = expiry;
    }
}
