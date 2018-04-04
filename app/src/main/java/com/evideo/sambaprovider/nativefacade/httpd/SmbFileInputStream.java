package com.evideo.sambaprovider.nativefacade.httpd;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by zouyingjun on 2018/3/30.
 */

public class SmbFileInputStream extends InputStream {
    ByteBuffer mBuf;
    public SmbFileInputStream(ByteBuffer buf) {
        this.mBuf = buf;
    }

    @Override
    public synchronized int read() throws IOException {
        if (!mBuf.hasRemaining()) {
            return -1;
        }
        return mBuf.get();
    }
    public synchronized int read(byte[] bytes, int off, int len) throws IOException {
        len = Math.min(len, mBuf.remaining());
        mBuf.get(bytes, off, len);
        return len;
    }
}
