package com.evideo.sambaprovider.http;

import java.net.Socket;

public class HTTPServerThread extends Thread {
    private static final String tag = "HTTPServerThread";
    private HTTPServer httpServer;
    private Socket sock;

    public HTTPServerThread(HTTPServer httpServer, Socket sock) {
        super("Cyber.HTTPServerThread");
        this.httpServer = httpServer;
        this.sock = sock;
    }

    public void run() {
        HTTPSocket httpSock = new HTTPSocket(this.sock);
        if (httpSock.open()) {
            HTTPRequest httpReq = new HTTPRequest();
            httpReq.setSocket(httpSock);
            while (httpReq.read()) {
                this.httpServer.performRequestListener(httpReq);
                if (!httpReq.isKeepAlive()) {
                    break;
                }
            }
            httpSock.close();
        }
    }
}
