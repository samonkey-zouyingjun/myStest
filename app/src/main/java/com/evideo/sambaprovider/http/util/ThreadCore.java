package com.evideo.sambaprovider.http.util;

public class ThreadCore implements Runnable {
    private Thread mThreadObject = null;

    public void setThreadObject(Thread obj) {
        this.mThreadObject = obj;
    }

    public Thread getThreadObject() {
        return this.mThreadObject;
    }

    public void start() {
        if (getThreadObject() == null) {
            Thread threadObject = new Thread(this, "Cyber.ThreadCore");
            setThreadObject(threadObject);
            threadObject.start();
        }
    }

    public void run() {
    }

    public boolean isRunnable() {
        return Thread.currentThread() == getThreadObject();
    }

    public void stop() {
        Thread threadObject = getThreadObject();
        if (threadObject != null) {
            threadObject.interrupt();
            setThreadObject(null);
        }
    }

    public void restart() {
        stop();
        start();
    }
}
