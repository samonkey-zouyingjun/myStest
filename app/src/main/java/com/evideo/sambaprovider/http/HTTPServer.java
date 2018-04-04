package com.evideo.sambaprovider.http;

import android.util.Log;

import com.evideo.sambaprovider.http.util.Debug;
import com.evideo.sambaprovider.http.util.ListenerList;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPServer implements Runnable {
    public static final int DEFAULT_PORT = 80;
    public static final int DEFAULT_TIMEOUT = 15000;
    public static final String NAME = "CyberHTTP";
    public static final String VERSION = "1.0";
    private static final String tag = "HTTPServer";
    private InetAddress bindAddr;
    private int bindPort;
    private ListenerList httpRequestListenerList;
    private Thread httpServerThread;
    private ServerSocket serverSock;
    protected int timeout;

    public static String getName() {
        String osName = System.getProperty("os.name");
        return osName + "/" + System.getProperty("os.version") + " " + NAME + "/" + "1.0";
    }

    public HTTPServer() {
        this.serverSock = null;
        this.bindAddr = null;
        this.bindPort = 0;
        this.timeout = DEFAULT_TIMEOUT;
        this.httpRequestListenerList = new ListenerList();
        this.httpServerThread = null;
        this.serverSock = null;
    }

    public ServerSocket getServerSock() {
        return this.serverSock;
    }

    public String getBindAddress() {
        if (this.bindAddr == null) {
            return "";
        }
        return this.bindAddr.getHostAddress();
    }

    public int getBindPort() {
        return this.bindPort;
    }

    public synchronized int getTimeout() {
        return this.timeout;
    }

    public synchronized void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean open(InetAddress addr, int port) {
        if (this.serverSock != null) {
            return true;
        }
        try {
            this.serverSock = new ServerSocket(this.bindPort, 0, this.bindAddr);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean open(String addr, int port) {
        if (this.serverSock != null) {
            return true;
        }
        try {
            this.bindAddr = InetAddress.getByName(addr);
            this.bindPort = port;
            this.serverSock = new ServerSocket(this.bindPort, 0, this.bindAddr);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean close() {
        if (this.serverSock == null) {
            return true;
        }
        try {
            this.serverSock.close();
            this.serverSock = null;
            this.bindAddr = null;
            this.bindPort = 0;
            Log.e(tag, "�ر�http�������� " + this.serverSock.getInetAddress().getHostAddress());
            return true;
        } catch (Exception e) {
            Debug.warning(e);
            return false;
        }
    }

    public Socket accept() {
        if (this.serverSock == null) {
            return null;
        }
        try {
            Socket sock = this.serverSock.accept();
            sock.setSoTimeout(getTimeout());
            return sock;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isOpened() {
        return this.serverSock != null;
    }

    public void addRequestListener(HTTPRequestListener listener) {
        this.httpRequestListenerList.add(listener);
    }

    public void removeRequestListener(HTTPRequestListener listener) {
        this.httpRequestListenerList.remove(listener);
    }

    public void performRequestListener(HTTPRequest httpReq) {
        int listenerSize = this.httpRequestListenerList.size();
        for (int n = 0; n < listenerSize; n++) {
            ((HTTPRequestListener) this.httpRequestListenerList.get(n)).httpRequestRecieved(httpReq);
        }
    }

    public void run() {
        if (isOpened()) {
            Thread thisThread = Thread.currentThread();
            while (this.httpServerThread == thisThread) {
                Thread.yield();
                try {
                    Debug.message("accept ...");
                    Socket sock = accept();
                    if (sock != null) {
                        Debug.message("sock = " + sock.getRemoteSocketAddress());
                    }
                    new HTTPServerThread(this, sock).start();
                    Debug.message("httpServThread ...");
                } catch (Exception e) {
                    return;
                }
            }
        }
    }

    public boolean start() {
        StringBuffer name = new StringBuffer("Cyber.HTTPServer/");
        name.append(this.serverSock.getLocalSocketAddress());
        this.httpServerThread = new Thread(this, name.toString());
        this.httpServerThread.start();
        return true;
    }

    public boolean stop() {
        this.httpServerThread = null;
        return true;
    }
}
