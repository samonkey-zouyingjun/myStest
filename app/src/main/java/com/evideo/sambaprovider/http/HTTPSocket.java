package com.evideo.sambaprovider.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;

public class HTTPSocket {
    private static final String TAG = "org.cybergarage.http.HTTPSocket";
    private InputStream sockIn = null;
    private OutputStream sockOut = null;
    private Socket socket = null;

    public HTTPSocket(Socket socket) {
        setSocket(socket);
        open();
    }

    public HTTPSocket(HTTPSocket socket) {
        setSocket(socket.getSocket());
        setInputStream(socket.getInputStream());
        setOutputStream(socket.getOutputStream());
    }

    public void finalize() {
        close();
    }

    private void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public String getLocalAddress() {
        return getSocket().getLocalAddress().getHostAddress();
    }

    public int getLocalPort() {
        return getSocket().getLocalPort();
    }

    private void setInputStream(InputStream in) {
        this.sockIn = in;
    }

    public InputStream getInputStream() {
        return this.sockIn;
    }

    private void setOutputStream(OutputStream out) {
        this.sockOut = out;
    }

    private OutputStream getOutputStream() {
        return this.sockOut;
    }

    public boolean open() {
        Socket sock = getSocket();
        try {
            this.sockIn = sock.getInputStream();
            this.sockOut = sock.getOutputStream();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean close() {
        try {
            if (this.sockIn != null) {
                this.sockIn.close();
            }
            if (this.sockOut != null) {
                this.sockOut.close();
            }
            getSocket().close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean post(HTTPResponse httpRes, byte[] content, long contentOffset, long contentLength, boolean isOnlyHeader) {
        httpRes.setDate(Calendar.getInstance());
        OutputStream out = getOutputStream();
        try {
            httpRes.setContentLength(contentLength);
            out.write(httpRes.getHeader().getBytes());
            out.write(HTTP.CRLF.getBytes());
            if (isOnlyHeader) {
                out.flush();
                return true;
            }
            boolean isChunkedResponse = httpRes.isChunked();
            if (isChunkedResponse) {
                out.write(Long.toHexString(contentLength).getBytes());
                out.write(HTTP.CRLF.getBytes());
            }
            out.write(content, (int) contentOffset, (int) contentLength);
            if (isChunkedResponse) {
                out.write(HTTP.CRLF.getBytes());
                out.write("0".getBytes());
                out.write(HTTP.CRLF.getBytes());
            }
            out.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean post(HTTPResponse httpRes, InputStream in, long contentOffset, long contentLength, boolean isOnlyHeader) {
        try {
            httpRes.setDate(Calendar.getInstance());
            OutputStream out = getOutputStream();
            httpRes.setContentLength(contentLength);
            out.write(httpRes.getHeader().getBytes());
            out.write(HTTP.CRLF.getBytes());
            if (isOnlyHeader) {
                out.flush();
                return true;
            }
            long readSize;
            boolean isChunkedResponse = httpRes.isChunked();
            if (0 < contentOffset) {
                in.skip(contentOffset);
            }
            int chunkSize = HTTP.getChunkSize();
            byte[] readBuf = new byte[chunkSize];
            long readCnt = 0;
            if (((long) chunkSize) < contentLength) {
                readSize = (long) chunkSize;
            } else {
                readSize = contentLength;
            }
            int readLen = in.read(readBuf, 0, (int) readSize);
            while (readLen > 0 && readCnt < contentLength) {
                if (isChunkedResponse) {
                    out.write(Long.toHexString((long) readLen).getBytes());
                    out.write(HTTP.CRLF.getBytes());
                }
                out.write(readBuf, 0, readLen);
                if (isChunkedResponse) {
                    out.write(HTTP.CRLF.getBytes());
                }
                readCnt += (long) readLen;
                readLen = in.read(readBuf, 0, (int) (((long) chunkSize) < contentLength - readCnt ? (long) chunkSize : contentLength - readCnt));
            }
            if (isChunkedResponse) {
                out.write("0".getBytes());
                out.write(HTTP.CRLF.getBytes());
            }
            out.flush();
            return true;
        } catch (IOException e) {
        }
        return false;
    }

    public boolean post(HTTPResponse httpRes, long contentOffset, long contentLength, boolean isOnlyHeader) {
        if (httpRes.hasContentInputStream()) {
            return post(httpRes, httpRes.getContentInputStream(), contentOffset, contentLength, isOnlyHeader);
        }
        return post(httpRes, httpRes.getContent(), contentOffset, contentLength, isOnlyHeader);
    }
}
