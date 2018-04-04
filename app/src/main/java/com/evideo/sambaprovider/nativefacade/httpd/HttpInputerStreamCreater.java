package com.evideo.sambaprovider.nativefacade.httpd;

import android.os.AsyncTask;

import com.evideo.sambaprovider.nativefacade.SmbClient;
import com.evideo.sambaprovider.provider.ByteBufferPool;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by zouyingjun on 2018/3/31.
 */

public class HttpInputerStreamCreater extends AsyncTask<Void, Void, Void> {

    private final String mUri;
    private final SmbClient mClient;
    private final ByteBufferPool mBufferPool;
    private InputStream mInputStream;
    private ByteBuffer mBuffer;

    public HttpInputerStreamCreater(String uri, SmbClient client, ByteBufferPool bufferPool) {
        mUri = uri;
        mClient = client;
        mBufferPool = bufferPool;
    }

    public InputStream getBufferedInputStream() {
        return new BufferedInputStream(mInputStream);
    }

    @Override
    protected void onPreExecute() {
        mBuffer = mBufferPool.obtainBuffer();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
