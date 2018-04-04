package com.evideo.sambaprovider.http;

import com.evideo.sambaprovider.http.util.Debug;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.StringTokenizer;

public class HTTPRequest extends HTTPPacket {
    private static final String TAG = "org.cybergarage.http.HTTPRequest";
    private HTTPSocket httpSocket;
    private String method;
    private Socket postSocket;
    private String requestHost;
    private int requestPort;
    private String uri;

    public HTTPRequest() {
        this.method = null;
        this.uri = null;
        this.requestHost = "";
        this.requestPort = -1;
        this.httpSocket = null;
        this.postSocket = null;
        setVersion("1.0");
    }

    public HTTPRequest(InputStream in) {
        super(in);
        this.method = null;
        this.uri = null;
        this.requestHost = "";
        this.requestPort = -1;
        this.httpSocket = null;
        this.postSocket = null;
    }

    public HTTPRequest(HTTPSocket httpSock) {
        this(httpSock.getInputStream());
        setSocket(httpSock);
    }

    public void setMethod(String value) {
        this.method = value;
    }

    public String getMethod() {
        if (this.method != null) {
            return this.method;
        }
        return getFirstLineToken(0);
    }

    public boolean isMethod(String method) {
        String headerMethod = getMethod();
        if (headerMethod == null) {
            return false;
        }
        return headerMethod.equalsIgnoreCase(method);
    }

    public boolean isGetRequest() {
        return isMethod(HTTP.GET);
    }

    public boolean isPostRequest() {
        return isMethod(HTTP.POST);
    }

    public boolean isHeadRequest() {
        return isMethod(HTTP.HEAD);
    }

    public boolean isSubscribeRequest() {
        return isMethod(HTTP.SUBSCRIBE);
    }

    public boolean isUnsubscribeRequest() {
        return isMethod(HTTP.UNSUBSCRIBE);
    }

    public boolean isNotifyRequest() {
        return isMethod(HTTP.NOTIFY);
    }

    public void setURI(String value, boolean isCheckRelativeURL) {
        this.uri = value;
        if (isCheckRelativeURL) {
            this.uri = HTTP.toRelativeURL(this.uri);
        }
    }

    public void setURI(String value) {
        setURI(value, false);
    }

    public String getURI() {
        if (this.uri != null) {
            return this.uri;
        }
        return getFirstLineToken(1);
    }

    public ParameterList getParameterList() {
        ParameterList paramList = new ParameterList();
        String uri = getURI();
        if (uri != null) {
            int paramIdx = uri.indexOf(63);
            if (paramIdx >= 0) {
                while (paramIdx > 0) {
                    int i;
                    int eqIdx = uri.indexOf(61, paramIdx + 1);
                    String name = uri.substring(paramIdx + 1, eqIdx);
                    int nextParamIdx = uri.indexOf(38, eqIdx + 1);
                    int i2 = eqIdx + 1;
                    if (nextParamIdx > 0) {
                        i = nextParamIdx;
                    } else {
                        i = uri.length();
                    }
                    paramList.add(new Parameter(name, uri.substring(i2, i)));
                    paramIdx = nextParamIdx;
                }
            }
        }
        return paramList;
    }

    public String getParameterValue(String name) {
        return getParameterList().getValue(name);
    }

    public boolean isSOAPAction() {
        return hasHeader(HTTP.SOAP_ACTION);
    }

    public void setRequestHost(String host) {
        this.requestHost = host;
    }

    public String getRequestHost() {
        return this.requestHost;
    }

    public void setRequestPort(int host) {
        this.requestPort = host;
    }

    public int getRequestPort() {
        return this.requestPort;
    }

    public void setSocket(HTTPSocket value) {
        this.httpSocket = value;
    }

    public HTTPSocket getSocket() {
        return this.httpSocket;
    }

    public String getLocalAddress() {
        return getSocket().getLocalAddress();
    }

    public int getLocalPort() {
        return getSocket().getLocalPort();
    }

    public boolean parseRequestLine(String lineStr) {
        StringTokenizer st = new StringTokenizer(lineStr, " ");
        if (!st.hasMoreTokens()) {
            return false;
        }
        setMethod(st.nextToken());
        if (!st.hasMoreTokens()) {
            return false;
        }
        setURI(st.nextToken());
        if (!st.hasMoreTokens()) {
            return false;
        }
        setVersion(st.nextToken());
        return true;
    }

    public String getHTTPVersion() {
        if (hasFirstLine()) {
            return getFirstLineToken(2);
        }
        return "HTTP/" + super.getVersion();
    }

    public String getFirstLineString() {
        return getMethod() + " " + getURI() + " " + getHTTPVersion() + HTTP.CRLF;
    }

    public String getHeader() {
        StringBuffer str = new StringBuffer();
        str.append(getFirstLineString());
        str.append(getHeaderString());
        return str.toString();
    }

    public boolean isKeepAlive() {
        if (isCloseConnection()) {
            return false;
        }
        if (isKeepAliveConnection()) {
            return true;
        }
        boolean isHTTP10;
        if (getHTTPVersion().indexOf("1.0") > 0) {
            isHTTP10 = true;
        } else {
            isHTTP10 = false;
        }
        if (!isHTTP10) {
            return true;
        }
        return false;
    }

    public boolean read() {
        return super.read(getSocket());
    }

    public boolean post(HTTPResponse httpRes) {
        HTTPSocket httpSock = getSocket();
        long offset = 0;
        long length = httpRes.getContentLength();
        if (hasContentRange()) {
            long firstPos = getContentRangeFirstPosition();
            long lastPos = getContentRangeLastPosition();
            if (lastPos <= 0) {
                lastPos = length - 1;
            }
            if (firstPos > length || lastPos > length) {
                return returnResponse(HTTPStatus.INVALID_RANGE);
            }
            httpRes.setContentRange(firstPos, lastPos, length);
            httpRes.setStatusCode(HTTPStatus.PARTIAL_CONTENT);
            offset = firstPos;
            length = (lastPos - firstPos) + 1;
        }
        return httpSock.post(httpRes, offset, length, isHeadRequest());
    }

    public HTTPResponse post(String host, int port, boolean isKeepAlive) {
        HTTPResponse httpRes = new HTTPResponse();
        setHost(host);
        setConnection(isKeepAlive ? HTTP.KEEP_ALIVE : HTTP.CLOSE);
        boolean isHeaderRequest = isHeadRequest();
        OutputStream out = null;
        InputStream in = null;
        try {
            if (this.postSocket == null) {
                this.postSocket = new Socket();
                this.postSocket.connect(new InetSocketAddress(host, port), HTTPServer.DEFAULT_TIMEOUT);
            }
            out = this.postSocket.getOutputStream();
            PrintStream pout = new PrintStream(out);
            pout.print(getHeader());
            pout.print(HTTP.CRLF);
            boolean isChunkedRequest = isChunked();
            String content = getContentString();
            int contentLength = 0;
            if (content != null) {
                contentLength = content.length();
            }
            if (contentLength > 0) {
                if (isChunkedRequest) {
                    pout.print(Long.toHexString((long) contentLength));
                    pout.print(HTTP.CRLF);
                }
                pout.print(content);
                if (isChunkedRequest) {
                    pout.print(HTTP.CRLF);
                }
            }
            if (isChunkedRequest) {
                pout.print("0");
                pout.print(HTTP.CRLF);
            }
            pout.flush();
            in = this.postSocket.getInputStream();
            httpRes.set(in, isHeaderRequest);
            if (!isKeepAlive) {
                try {
                    in.close();
                } catch (Exception e) {
                }
                if (in != null) {
                    try {
                        out.close();
                    } catch (Exception e2) {
                    }
                }
                if (out != null) {
                    try {
                        this.postSocket.close();
                    } catch (Exception e3) {
                    }
                }
                this.postSocket = null;
            }
        } catch (Exception e4) {
            httpRes.setStatusCode(500);
            Debug.warning(e4);
            if (!isKeepAlive) {
                try {
                    in.close();
                } catch (Exception e5) {
                }
                if (in != null) {
                    try {
                        out.close();
                    } catch (Exception e6) {
                    }
                }
                if (out != null) {
                    try {
                        this.postSocket.close();
                    } catch (Exception e7) {
                    }
                }
                this.postSocket = null;
            }
        } catch (Throwable th) {
            if (!isKeepAlive) {
                try {
                    in.close();
                } catch (Exception e11) {
                }
                if (in != null) {
                    try {
                        out.close();
                    } catch (Exception e12) {
                    }
                }
                if (out != null) {
                    try {
                        this.postSocket.close();
                    } catch (Exception e13) {
                    }
                }
                this.postSocket = null;
            }
        }
        return httpRes;
    }

    public HTTPResponse post(String host, int port) {
        return post(host, port, false);
    }

    public void set(HTTPRequest httpReq) {
        set((HTTPPacket) httpReq);
        setSocket(httpReq.getSocket());
    }

    public boolean returnResponse(int statusCode) {
        HTTPResponse httpRes = new HTTPResponse();
        httpRes.setStatusCode(statusCode);
        httpRes.setContentLength(0);
        return post(httpRes);
    }

    public boolean returnOK() {
        return returnResponse(200);
    }

    public boolean returnBadRequest() {
        return returnResponse(HTTPStatus.BAD_REQUEST);
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append(getHeader());
        str.append(HTTP.CRLF);
        str.append(getContentString());
        return str.toString();
    }

    public void print() {
        System.out.println(toString());
    }
}
