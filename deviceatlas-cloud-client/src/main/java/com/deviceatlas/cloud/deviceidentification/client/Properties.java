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
 * Map String property names to Property objects. An instance of this class
 * is returned for every getProperties() lookup that matches an IP.
 * 
 * Copyright (c) 2016 by Afilias Technologies Limited (dotMobi). All rights reserved.
 * @author Afilias Technologies Ltd
 */
public class Properties extends HashMap<String, Property> {

    public Properties() {
        super();
    }

    public Properties(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public void putMap(Map<String, Object> properties) {
        for(Map.Entry<String, Object> entry : properties.entrySet()) {
            Property prop;
            Object property = entry.getValue();

            if (property instanceof Boolean) {
                prop = new Property(property, DataType.BOOLEAN);
            } else if (property instanceof Integer) {
                prop = new Property(property, DataType.INTEGER);
            } else {
                prop = new Property(property, DataType.STRING);
            }

            put(entry.getKey(), prop);
        }
    }

    /**
     * Check if a property has a specific value. Returns false if property name is invalid or
     * property does not exists or the value to check passed is invalid. The value data type
     * should be exactly the same data type as the property. For example do not use values such
     * as "0", "1", 0, 1, 2, -1, "true", "false" for boolean properties.
     *
     * @param propertyName the property to check it's value
     * @param valueToCheck the value to be checked against property's value
     * @return true if value to check matches the property's value
     */
    public <T> boolean contains(String propertyName, T valueToCheck) {
        Property prop = get(propertyName);

        if (prop == null || valueToCheck == null) {
            return false;
        }

        Object propValue = prop.value();

        // only this is guaranteed to work as expected
        if (propValue.getClass().equals(valueToCheck.getClass())) {
            return propValue.equals(valueToCheck);
        }

        // deal with user abuse

        // for booleans only accept 0 and 1 numbers any other value is not acceptable
        if (prop.dataTypeId == DataType.BOOLEAN) {
            if (valueToCheck instanceof Integer || valueToCheck instanceof Byte || valueToCheck instanceof Short) {
                return ((Boolean)propValue && ((Integer)valueToCheck).equals(1)) || (!(Boolean)propValue && ((Integer)valueToCheck).equals(0));
            }
            return false;
        }

        // convert both property and check values to string
        // if the property is of string type this makes perfect sense even though the user has abused the method
        // if the property is of number type this will work as only if the digits match true will be returned
        return prop.asString().equals(valueToCheck instanceof String? (String)valueToCheck: valueToCheck.toString());
    }
}
