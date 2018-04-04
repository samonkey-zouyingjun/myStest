package com.evideo.sambaprovider.http;

import com.evideo.sambaprovider.http.util.Debug;

import java.io.LineNumberReader;
import java.io.StringReader;

public class HTTPHeader {
    private static int MAX_LENGTH = 1024;
    private String name;
    private String value;

    public HTTPHeader(String name, String value) {
        setName(name);
        setValue(value);
    }

    public HTTPHeader(String lineStr) {
        setName("");
        setValue("");
        if (lineStr != null) {
            int colonIdx = lineStr.indexOf(58);
            if (colonIdx >= 0) {
                String name = new String(lineStr.getBytes(), 0, colonIdx);
                String value = new String(lineStr.getBytes(), colonIdx + 1, (lineStr.length() - colonIdx) - 1);
                setName(name.trim());
                setValue(value.trim());
            }
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public boolean hasName() {
        if (this.name == null || this.name.length() <= 0) {
            return false;
        }
        return true;
    }

    public static final String getValue(LineNumberReader reader, String name) {
        String bigName = name.toUpperCase();
        try {
            String lineStr = reader.readLine();
            while (lineStr != null && lineStr.length() > 0) {
                HTTPHeader header = new HTTPHeader(lineStr);
                if (!header.hasName()) {
                    lineStr = reader.readLine();
                } else if (header.getName().toUpperCase().equals(bigName)) {
                    return header.getValue();
                } else {
                    lineStr = reader.readLine();
                }
            }
            return "";
        } catch (Exception e) {
            Debug.warning(e);
            return "";
        }
    }

    public static final String getValue(String data, String name) {
        return getValue(new LineNumberReader(new StringReader(data), Math.min(data.length(), MAX_LENGTH)), name);
    }

    public static final String getValue(byte[] data, String name) {
        return getValue(new String(data), name);
    }

    public static final int getIntegerValue(String data, String name) {
        try {
            return Integer.parseInt(getValue(data, name));
        } catch (Exception e) {
            return 0;
        }
    }

    public static final int getIntegerValue(byte[] data, String name) {
        try {
            return Integer.parseInt(getValue(data, name));
        } catch (Exception e) {
            return 0;
        }
    }
}
