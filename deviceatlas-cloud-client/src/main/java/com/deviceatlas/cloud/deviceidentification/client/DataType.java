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

package com.deviceatlas.cloud.deviceidentification.client;

import java.util.HashMap;
import java.util.Map;

/**
 * The data types for various properties. Each Property object returned has a
 * getDataType() method.
 * 
 * Copyright (c) 2008-2016 by Afilias Technologies Limited (dotMobi). All rights reserved.
 * @author Afilias Technologies Ltd
 */
// INTERNAL note: The ID is important and is used by the API when reading a 
// property. A data type can either be a known fixed size or a variable length. 
public class DataType {

    private static final Map<Byte, String> names;
    /** Type boolean */
    public static final byte BOOLEAN = 0;
    /** Type byte */
    public static final byte BYTE    = 1;
    /** Type short int */
    public static final byte SHORT   = 2;
    /** Type int */
    public static final byte INTEGER = 3;
    /** Type long int */
    public static final byte LONG    = 4;
    /** Type float */
    public static final byte FLOAT   = 5;
    /** Type double */
    public static final byte DOUBLE  = 6;
    /** Type string */
    public static final byte STRING  = 7;
    /** Unknown type */
    public static final byte UNKNOWN = 8;

    private DataType() {
    }

    static {
        names = new HashMap<Byte, String>();
        names.put(Byte.valueOf(BOOLEAN), "Boolean");
        names.put(Byte.valueOf(BYTE),    "Byte");
        names.put(Byte.valueOf(SHORT),   "Short");
        names.put(Byte.valueOf(INTEGER), "Integer");
        names.put(Byte.valueOf(LONG),    "Long");
        names.put(Byte.valueOf(FLOAT),   "Float");
        names.put(Byte.valueOf(DOUBLE),  "Double");
        names.put(Byte.valueOf(STRING),  "String");
        names.put(Byte.valueOf(UNKNOWN), "Unknown");
    }

    /**
     * Get the name of a given data type.
     * @param dataTypeID The id to lookup the name for
     * @return The name of the data type.
     */
    public static String getName(byte dataTypeID) {
        return names.get(Byte.valueOf(dataTypeID));
    }

}
