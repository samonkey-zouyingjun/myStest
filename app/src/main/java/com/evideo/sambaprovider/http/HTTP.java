package com.evideo.sambaprovider.http;

import java.net.URL;

public class HTTP {
    public static final String CACHE_CONTROL = "Cache-Control";
    public static final String CALLBACK = "CALLBACK";
    public static final String CHARSET = "charset";
    public static final String CHUNKED = "Chunked";
    public static final String CLOSE = "close";
    public static final String CONNECTION = "Connection";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_RANGE = "Content-Range";
    public static final String CONTENT_RANGE_BYTES = "bytes";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final byte CR = (byte) 13;
    public static final String CRLF = "\r\n";
    public static final String DATE = "Date";
    public static final int DEFAULT_CHUNK_SIZE = 524288;
    public static final int DEFAULT_PORT = 80;
    public static final int DEFAULT_TIMEOUT = 15;
    public static final String EXT = "EXT";
    public static final String GET = "GET";
    public static final String HEAD = "HEAD";
    public static final String HEADER_LINE_DELIM = " :";
    public static final String HOST = "HOST";
    public static final String KEEP_ALIVE = "Keep-Alive";
    public static final byte LF = (byte) 10;
    public static final String LOCATION = "Location";
    public static final String MAN = "MAN";
    public static final String MAX_AGE = "max-age";
    public static final String MX = "MX";
    public static final String MYNAME = "MYNAME";
    public static final String M_SEARCH = "M-SEARCH";
    public static final String NOTIFY = "NOTIFY";
    public static final String NO_CACHE = "no-cache";
    public static final String NT = "NT";
    public static final String NTS = "NTS";
    public static final String POST = "POST";
    public static final String RANGE = "Range";
    public static final String REQEST_LINE_DELIM = " ";
    public static final String SEQ = "SEQ";
    public static final String SERVER = "Server";
    public static final String SID = "SID";
    public static final String SOAP_ACTION = "SOAPACTION";
    public static final String ST = "ST";
    public static final String STATUS_LINE_DELIM = " ";
    public static final String SUBSCRIBE = "SUBSCRIBE";
    public static final String TAB = "\t";
    public static final String TIMEOUT = "TIMEOUT";
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";
    public static final String UNSUBSCRIBE = "UNSUBSCRIBE";
    public static final String USN = "USN";
    public static final String VERSION = "1.1";
    public static final String VERSION_10 = "1.0";
    public static final String VERSION_11 = "1.1";
    private static int chunkSize = 524288;

    public static final boolean isAbsoluteURL(String urlStr) {
        try {
            URL url = new URL(urlStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static final String getHost(String urlStr) {
        try {
            return new URL(urlStr).getHost();
        } catch (Exception e) {
            return "";
        }
    }

    public static final int getPort(String urlStr) {
        try {
            int port = new URL(urlStr).getPort();
            if (port <= 0) {
                return 80;
            }
            return port;
        } catch (Exception e) {
            return 80;
        }
    }

    public static final String getRequestHostURL(String host, int port) {
        return "http://" + host + ":" + port;
    }

    public static final String toRelativeURL(String urlStr, boolean withParam) {
        String uri = urlStr;
        System.out.println("33333---" + uri);
        if (isAbsoluteURL(urlStr)) {
            try {
                URL url = new URL(urlStr);
                uri = url.getPath();
                System.out.println("22222222---" + uri);
                if (withParam) {
                    String queryStr = url.getQuery();
                    if (!"".equals(queryStr)) {
                        uri = uri + "?" + queryStr;
                    }
                }
                System.out.println("22---" + uri);
                if (uri.endsWith("/")) {
                    uri = uri.substring(0, uri.length() - 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("2---" + uri);
        } else {
            if (urlStr.length() > 0 && urlStr.charAt(0) != '/') {
                uri = "/" + urlStr;
            }
            System.out.println("1---" + uri);
        }
        return uri;
    }

    public static final String toRelativeURL(String urlStr) {
        return toRelativeURL(urlStr, true);
    }

    public static final String getAbsoluteURL(String baseURLStr, String relURlStr) {
        try {
            URL baseURL = new URL(baseURLStr);
            return baseURL.getProtocol() + "://" + baseURL.getHost() + ":" + baseURL.getPort() + toRelativeURL(relURlStr);
        } catch (Exception e) {
            return "";
        }
    }

    public static final void setChunkSize(int size) {
        chunkSize = size;
    }

    public static final int getChunkSize() {
        return chunkSize;
    }
}
