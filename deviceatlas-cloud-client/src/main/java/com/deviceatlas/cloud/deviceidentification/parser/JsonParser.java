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

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to parse the recognition tree from supplied JSON encoded data.
 *
 * @author Afilias Technologies Ltd
 */
public class JsonParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonParser.class);

    /**
     * A JSON parser.
     *
     */
    private static class Tokeniser {

        private String s;
        private int p;

        /**
         * Create an instance of the Tokeniser with the supplied JSON string.
         *
         * @param s JSON data.
         */
        public Tokeniser(String s) {
            this.s = s;
            this.p = 0;
        }

        /**
         * Return a throwable JsonException
         *
         * @param message The text to display.
         * @return throwable exception
         */
        public JsonException syntaxError(String message) {
            return new JsonException(JsonException.BAD_DATA, message);
        }

        /**
         * Determine if there are more characters to consume by next().
         *
         * @return true If not yet at the end.
         */
        public boolean more() {
            return p < s.length();
        }

        /**
         * Consume the next character and return or 0 if at the end of the
         * string, and change the position.
         *
         * @return The character.
         */
        public char next() {
            if (more()) {
                char c = s.charAt(p);
                p++;
                return c;
            } else {
                return 0;
            }
        }

        /**
         * Consume the next character, and check that it matches a specified
         * character.
         *
         * @param c The character to match.
         * @return The character.
         * @throws JsonException if the character does not match.
         */
        public char next(char c) throws JsonException {
            char n = next();
            if (n != c) {
                throw syntaxError("Expected '" + c + "' and instead saw '"
                        + n + "'");
            }
            return n;
        }

        /**
         * Get the next n characters.
         *
         * @param n The number of characters to take.
         * @return A string of n characters.
         * @throws JsonException Substring bounds error if there are not n
         * characters remaining in the source string.
         */
        public String next(int n) throws JsonException {
            int i = this.p;
            int j = i + n;
            if (j >= this.s.length()) {
                throw syntaxError("Substring bounds error");
            }
            this.p += n;
            return this.s.substring(i, j);
        }

        /**
         * Back up one character. This provides a sort of lookahead capability,
         * so that you can test for a digit or letter before attempting to parse
         * the next number or identifier.
         */
        public void back() {
            if (p > 0) {
                p--;
            }
        }

        /**
         * Extracts the \\ characters from the json
         *
         * @parameter sb
         */

        public void nextEscapedString(StringBuilder sb) throws JsonException {
            char c = next();
            switch (c) {
                case 'b':
                    sb.append('\b');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'n':
                    sb.append('\n');
                    break;
                case 'f':
                    sb.append('\f');
                    break;
                case 'r':
                    sb.append('\r');
                    break;
                case 'u':
                    sb.append((char) Integer.parseInt(next(4), 16));
                    break;
                case 'x':
                    sb.append((char) Integer.parseInt(next(2), 16));
                    break;
                default:
                    sb.append(c);
            }
        }

        /**
         * Return the characters up to the next close quote character. Backslash
         * processing is done. The formal JSON format does not allow strings in
         * single quotes, but an implementation is allowed to accept them.
         *
         * @param quote The quoting character, either
         * <code>"</code>&nbsp;<small>(double quote)</small> or
         * <code>'</code>&nbsp;<small>(single quote)</small>.
         * @return A String.
         * @throws JsonException Unterminated string.
         */
        public String nextString(char quote) throws JsonException {
            char c;
            StringBuilder sb = new StringBuilder();
            while (true) {
                c = next();
                switch (c) {
                    case 0:
                    case '\n':
                    case '\r':
                        throw syntaxError("Unterminated string");
                    case '\\':
                        nextEscapedString(sb);
                        break;
                    default:
                        if (c == quote) {
                            return sb.toString();
                        }
                        sb.append(c);
                }
            }
        }

        /**
         * Get the text up but not including the specified character.
         *
         * @param d A delimiter character.
         * @return A string.
         */
        public String nextTo(char d) {
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = next();
                if (c == d || c == 0) {
                    if (c != 0) {
                        back();
                    }
                    return sb.toString().trim();
                }
                sb.append(c);
            }
        }

        /**
         * Get the text up but not including one of the specified delimeter
         * characters.
         *
         * @param delimiters A set of delimiter characters.
         * @return A string, trimmed.
         */
        public String nextTo(String delimiters) {
            char c;
            StringBuilder sb = new StringBuilder();
            for (;;) {
                c = next();
                if (delimiters.indexOf(c) >= 0 || c == 0) {
                    if (c != 0) {
                        back();
                    }
                    return sb.toString().trim();
                }
                sb.append(c);
            }
        }

        /**
         * Treats the default value's case
         *
         * @param sb
         * @param c
         */

        public void nextDefaultValue(StringBuilder sb, char c) {
            char d = c;
            while (d >= ' ' && ",:]}/\\\"[{;=#".indexOf(d) < 0) {
                sb.append(d);
                d = next();
            }
        }

        /**
         * Treats the numeric value's case
         *
         * @param sb
         * @param b
         * @return Object
         */

        public Object nextIntegerValue(String str, char b) {
            /*
             * If it might be a number, try converting it. We support the 0- and 0x-
             * conventions. If a number cannot be produced, then the value will just
             * be a string. Note that the 0-, 0x-, plus, and implied string
             * conventions are non-standard. A JSON parser is free to accept
             * non-JSON forms as long as it accepts all correct JSON forms.
             */
            final String nextValue = "nextValue";
            boolean isDigit = b >= '0' && b <= '9';
            boolean isSign = b == '.' || b == '-' || b =='+';
            boolean isHex = str.length() > 2 && (str.charAt(1) == 'x' || str.charAt(1) == 'X');
            if (isDigit || isSign) {
                if (b == '0' && isHex) {
                    try {
                        return Integer.parseInt(s.substring(2),
                                16);
                    } catch (Exception e) {
                        /* Ignore the error */
                        LOGGER.error(nextValue, e);
                    }
                } else if (b == '0' && !isHex) {
                    try {
                        return Integer.parseInt(str, 8);
                    } catch (Exception e) {
                        /* Ignore the error */
                        LOGGER.error(nextValue, e);
                    }
                }
                try {
                    return Integer.parseInt(str);
                } catch (Exception e) {
                    LOGGER.error(nextValue, e);
                    try {
                        return Long.parseLong(str);
                    } catch (Exception f) {
                        LOGGER.error(nextValue, f);
                        try {
                            return Double.parseDouble(str);
                        } catch (Exception g) {
                            LOGGER.error(nextValue, g);
                            return s;
                        }
                    }
                }
            }

            return null;
        }

        /**
         * Get the next value. The value can be a Boolean, Double, Integer,
         * ArrayList, HashMap, Long, or String.
         *
         * @throws JsonException If syntax error.
         *
         * @return An object.
         */
        public Object nextValue() throws JsonException {
            char c = next();
            Object numValue;
            StringBuilder sb;
            String str;
            char b;

            switch (c) {
                case '"':
                case '\'':
                    return nextString(c);
                case '{':
                    back();
                    return new JsonParser(this).getHashMap();
                case '[':
                case '(':
                    back();
                    return new JsonParser(this).getArray();
                default:
                    sb = new StringBuilder();
                    b = c;
                    nextDefaultValue(sb, c);
                    back();
            }

            /*
             * Handle unquoted text. This could be the values true, false, or
             * null, or it can be a number. An implementation (such as this one)
             * is allowed to also accept non-standard forms.
             *
             * Accumulate characters until we reach the end of the text or a
             * formatting character.
             */

            /*
             * If it is true, false, or null, return the proper value.
             */
            str = sb.toString().trim();
            if ("".equals(str)) {
                throw syntaxError("Missing value");
            }
            if ("true".equalsIgnoreCase(str)) {
                return Boolean.TRUE;
            }
            if ("false".equalsIgnoreCase(str)) {
                return Boolean.FALSE;
            }
            if ("null".equalsIgnoreCase(str)) {
                return null;
            }

            if ((numValue = nextIntegerValue(str, b)) != null) {
                return numValue;
            }
            return str;
        }
    }

    private Tokeniser json;

    /**
     * Construct a JSON Parser from the supplied JSON string
     *
     * @param str JSON encoded data
     */
    public JsonParser(String str) {
        json = new Tokeniser(str);
    }

    /**
     * Construct a JSON Parser from the supplied JSON tokeniser
     *
     * @param jsonTokeniser JSON tokeniser
     */
    public JsonParser(Tokeniser jsonTokeniser) {
        this.json = jsonTokeniser;
    }

    /**
     * Extracts the key from a json object
     *
     * @return String
     */

    public String hashMapKey() throws JsonException {
        char c = this.json.next();
        switch (c) {
            case 0:
                throw this.json.syntaxError("A Json object text must end with '}'");
            case '}':
                return null;
            default:
                this.json.back();
                return this.json.nextValue().toString();
        }
    }

    /**
     * Extracts the key/value character separator from a map
     *
     * @param tree
     * @param key
     */

    public void hashMapSkipSeparator(Map<String, Object> tree, String key) throws JsonException {
        /*
         * The key is followed by ':'
         */
        char c = this.json.next();
        if (c != ':') {
            throw this.json.syntaxError("Expected a ':' after a key");
        }
        tree.put(key, this.json.nextValue());
    }

    /**
     * Checks if the objects finishes with a valid '}'
     *
     * @return int
     */

    public int hashMapPair() throws JsonException {
        /*
         * Pairs are separated by ','.
         */
        switch (this.json.next()) {
            case ',':
                if (this.json.next() == '}') {
                    return -1;
                }
                this.json.back();
                break;
            case '}':
                return -1;
            default:
                throw this.json.syntaxError("Expected a ',' or '}'");
        }

        return 0;
    }

    /**
     * Return a HashMap generated from processing this objects json tokeniser
     *
     * @return Data tree in the form of a string indexed HashMap
     * @throws JsonException See {@link JsonException}
     */
    public Map <String, Object> getHashMap() throws JsonException {
        Map <String, Object> tree = new HashMap<String, Object>();
        String key;

        if (this.json.next() != '{') {
            throw this.json.syntaxError("A Json object text must begin with '{'");
        }
        while (true) {
            if ((key = hashMapKey()) == null) {
                return tree;
            }

            hashMapSkipSeparator(tree, key);
            if (hashMapPair() == -1) {
                return tree;
            }
        }
    }

    /**
     * Returns the array/object last token
     *
     * @return char
     */

    public char getArrayLastToken() throws JsonException {
        char c = this.json.next();
        if (c == '[') {
            return ']';
        } else if (c == '(') {
            return ')';
        } else {
            throw this.json.syntaxError("A Json Array text must start with '['");
        }
    }

    /**
     * Puts tp the list the next or empty value
     *
     * @param list
     */

    public void getArrayPair(Map<String, Object> list) throws JsonException {
        int key = 0;
        if (this.json.next() == ',') {
            this.json.back();
            list.put(String.valueOf(key++), null);
        } else {
            this.json.back();
            list.put(String.valueOf(key++), this.json.nextValue());
        }
    }

    /**
     * Checks the proper array data consistency
     *
     * @param q
     * @return int
     */

    public int getArraySeek(char q) throws JsonException {
        char c = this.json.next();
        switch (c) {
            case ',':
                if (this.json.next() == ']') {
                    return 0;
                }
                this.json.back();
                break;
            case ']':
            case ')':
                if (q != c) {
                    throw this.json.syntaxError("Expected a '" + q + "'");
                }
                return 0;
            default:
                throw this.json.syntaxError("Expected a ',' or ']'");
        }

        return -1;
    }

    /**
     * Fills the list of pair
     * 
     * @param list
     * @param q
     * @return Map
     */

    public Map<String, Object> getArrayFillList(Map<String, Object> list, char q) throws JsonException {
        while (true) {
            getArrayPair(list);
            if (getArraySeek(q) == 0) {
                return list;
            }
        }
    }

    /**
     * Returns a json array
     *
     * @return Map
     */

    public Map <String, Object> getArray() throws JsonException {
        Map <String, Object> list = new HashMap <String, Object>();

        this.json.next();
        char q = getArrayLastToken();
        if (this.json.next() == ']') {
            return list;
        }
        this.json.back();
        return getArrayFillList(list, q);
    }
}
