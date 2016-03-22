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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains a property value. The value can be fetched as a generic Object or
 * one of the convenience asXXXX methods can be used to get the value in a 
 * specific type.
 * 
 * Copyright (c) 2008-2016 by Afilias Technologies Limited (dotMobi). All rights reserved.
 * @author Afilias Technologies Ltd
 */
public class Property {

    protected final Object  value;
    protected final byte    dataTypeId;
    protected final boolean isCollectionOfValues;

    // exception messages
    private static final String NOT_CONVERTIBLE_TO_BOOLEAN = "Property is not convertible to a boolean.";
    private static final String NOT_CONVERTIBLE_TO_INT     = "Property is not convertible to an int.";

    private static final Logger LOGGER = LoggerFactory.getLogger(Property.class);

    /**
     * Create a new Property with a value and data type.
     *
     * @param value The value to store
     * @param dataTypeId The type of the value to store. Data typess are defined in class DataType.
     */
    public Property(Object value, byte dataTypeId) {
        this(value, dataTypeId, false);
    }

    /**
     * Create a new Property with a value and data type.
     *
     * @param value The value to store
     * @param dataTypeId The type of the value to store. Data typess are defined in class DataType.
     * @param isCollectionOfValues Indicates if the Property will contain a single value or multiple values
     */
    public Property(Object value, byte dataTypeId, boolean isCollectionOfValues) {
        this.value = value;
        this.dataTypeId = dataTypeId;
        this.isCollectionOfValues = isCollectionOfValues;
    }

    /**
     * Get the data type ID for values associated with this Property
     * @return The ID of the data type
     */
    public byte getDataTypeId() {
        return dataTypeId;
    }

    /**
     * Get the data type name for values associated with this Property
     * @return The name of the data type
     */
    public String getDataType() {
        return DataType.getName(dataTypeId);
    }

    /**
     * Get the value Object. This needs to be cast to the appropriate type such
     * as Integer to be used. E.g. Integer value = (Integer)property.value();
     *
     * @return The raw Object value.
     */
    public Object value() {
        return value;
    }

    /**
     * Get the value of the property as a String. Alias for asString() . If a 
     * property has multiple possible values then the values are concatenated
     * with a comma.
     * 
     * @return The String value of the property
     */
    @Override
    public String toString() {
        if (value instanceof Boolean) {
            return (Boolean)value ? "1" : "0";
        }

        if (value instanceof String) {
            return (String)value;
        }

        if (isCollectionOfValues) {
            StringBuilder sb = new StringBuilder();
            // iterate over the set and add to the string buffer
            Iterator iterator = ((LinkedHashSet)value).iterator();
            if (iterator.hasNext()) {
                sb.append(iterator.next().toString());
            }
            // add in the remaining items and separate with a comma+space
            while (iterator.hasNext()) {
                sb.append(",");
                sb.append(iterator.next().toString());
            }
            return sb.toString();
        }

        return value.toString();
    }

    /**
     * Get the value of the property as a String. Alias for toString() .
     *
     * @return The String value of the property
     */
    public String asString() {
        return toString();
    }

    /**
     * Get the value of the property as a boolean.
     *
     * @return The boolean value of the property
     * @throws IncorrectPropertyTypeException Thrown if the type of the value is not a DataType.BOOLEAN
     */
    public boolean asBoolean() throws IncorrectPropertyTypeException {
        if (dataTypeId != DataType.BOOLEAN) {
            throw new IncorrectPropertyTypeException(NOT_CONVERTIBLE_TO_BOOLEAN);
        }

        try {
            return (Boolean)value;
        } catch (ClassCastException x) {
            LOGGER.error("asBoolean", x);
            throw new IncorrectPropertyTypeException(NOT_CONVERTIBLE_TO_BOOLEAN);
        }
    }

    /**
     * Get the value of the property as an int.
     *
     * @return The integer value of the property. 
     * @throws IncorrectPropertyTypeException  Thrown if the type of the value is not compatible with a DataType.INTEGER
     */
    public int asInteger() throws IncorrectPropertyTypeException {
        try {
            switch (dataTypeId) {
                case DataType.INTEGER:
                    return (Integer)value;
                case DataType.BYTE:
                    return (Byte)value;
                case DataType.SHORT:
                    return (Short)value;
                default:
                    throw new IncorrectPropertyTypeException(NOT_CONVERTIBLE_TO_INT);
            }
        } catch (ClassCastException x) {
            LOGGER.error("asInteger", x);
            // if a property is defined as an int but the value set to it is not an int then this is a but to know about
            throw new IncorrectPropertyTypeException(NOT_CONVERTIBLE_TO_INT);
        }
    }

    /**
     * Gets a set of possible values for this property. This is typically only
     * used when it is known that a given property name can have multiple
     * possible values. All items in the set will have the same data tyoe.
     * 
     * @return A set of values.
     */
    public Set asSet() {
        if (isCollectionOfValues) {
            return (Set)value;
        }
        // wrap the value into a set
        Set set;
        set = new LinkedHashSet();
        set.add(value);
        return set;
    }

    /**
     * Check if another object is equal to this object. It must have the same interface, data type and value.
     *
     * @param obj object to be compared against
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final Property other = (Property)obj;
        return this.dataTypeId == other.dataTypeId
            && this.isCollectionOfValues == other.isCollectionOfValues
            && value().equals(other.value());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 89 * hash + this.dataTypeId;
        hash = 89 * hash + (this.isCollectionOfValues ? 1 : 0);
        return hash;
    }
}
