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


package com.deviceatlas.cloud.deviceidentification.parser;

/**
 * The JsonException is thrown by the JsonParser class when there is an error
 * parsing the JSON data.
 *
 * @author Afilias Technologies Ltd
 */
public class JsonException extends Exception {
    /**
     * This is a Java-specific version management feature to deal with
     * serialisation and deserialisation of class instances.
     */
    static final long serialVersionUID = 2612345665573111548L;

    /**
     * The data stored in the JSON cannot be used to build a valid Device Atlas
     * data tree.
     */
    public static final int BAD_DATA = 100;

    /**
     * The JSON data you are using is to old for this API. Download a more
     * recent version of the data.
     */
    public static final int JSON_VERSION = 200;

    /**
     * The path to the JSON file that was given cannot be resolved. Ensure you
     * have supplied the correct path. Use an absolute pathname where you are
     * unsure of the current working directory.
     */
    public static final int FILE_NOT_FOUND_ERROR = 300;

    private final Throwable cause;
    private final int code;

    public JsonException(int code, String message) {
        super(message);
        this.code = code;
        this.cause = new Throwable(message);
    }

    /**
     * Retrieves the throwable, which determines the nature/cause of the
     * exception.
     *
     * @return Explanatory code.
     */
    @Override
    public Throwable getCause() {
        return this.cause;
    }

    /**
     * Retrieves the exception code, which determines the nature/cause of the
     * exception.
     *
     * @return Explanatory code.
     */
    public int getCode() {
        return code;
    }
}
