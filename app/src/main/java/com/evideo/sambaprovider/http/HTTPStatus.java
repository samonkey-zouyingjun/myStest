package com.evideo.sambaprovider.http;

import com.evideo.sambaprovider.http.util.Debug;

import java.util.StringTokenizer;

public class HTTPStatus {
    public static final int BAD_REQUEST = 400;
    public static final int CONTINUE = 100;
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int INVALID_RANGE = 416;
    public static final int NOT_FOUND = 404;
    public static final int OK = 200;
    public static final int PARTIAL_CONTENT = 206;
    public static final int PRECONDITION_FAILED = 412;
    private String reasonPhrase = "";
    private int statusCode = 0;
    private String version = "";

    public static final String code2String(int code) {
        switch (code) {
            case 100:
                return "Continue";
            case 200:
                return "OK";
            case PARTIAL_CONTENT /*206*/:
                return "Partial Content";
            case BAD_REQUEST /*400*/:
                return "Bad Request";
            case NOT_FOUND /*404*/:
                return "Not Found";
            case PRECONDITION_FAILED /*412*/:
                return "Precondition Failed";
            case INVALID_RANGE /*416*/:
                return "Invalid Range";
            case 500:
                return "Internal Server Error";
            default:
                return "";
        }
    }

    public HTTPStatus() {
        setVersion("");
        setStatusCode(0);
        setReasonPhrase("");
    }

    public HTTPStatus(String ver, int code, String reason) {
        setVersion(ver);
        setStatusCode(code);
        setReasonPhrase(reason);
    }

    public HTTPStatus(String lineStr) {
        set(lineStr);
    }

    public void setVersion(String value) {
        this.version = value;
    }

    public void setStatusCode(int value) {
        this.statusCode = value;
    }

    public void setReasonPhrase(String value) {
        this.reasonPhrase = value;
    }

    public String getVersion() {
        return this.version;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    public static final boolean isSuccessful(int statCode) {
        if (200 > statCode || statCode >= 300) {
            return false;
        }
        return true;
    }

    public boolean isSuccessful() {
        return isSuccessful(getStatusCode());
    }

    public void set(String lineStr) {
        if (lineStr == null) {
            setVersion("1.1");
            setStatusCode(500);
            setReasonPhrase(code2String(500));
            return;
        }
        try {
            StringTokenizer st = new StringTokenizer(lineStr, " ");
            if (st.hasMoreTokens()) {
                setVersion(st.nextToken().trim());
                if (st.hasMoreTokens()) {
                    int code = 0;
                    try {
                        code = Integer.parseInt(st.nextToken());
                    } catch (Exception e) {
                    }
                    setStatusCode(code);
                    String reason = "";
                    while (st.hasMoreTokens()) {
                        if (reason.length() >= 0) {
                            reason = reason + " ";
                        }
                        reason = reason + st.nextToken();
                    }
                    setReasonPhrase(reason.trim());
                }
            }
        } catch (Exception e2) {
            Debug.warning(e2);
        }
    }
}
