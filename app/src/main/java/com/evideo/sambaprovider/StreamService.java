package com.evideo.sambaprovider;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.evideo.sambaprovider.nativefacade.httpd.NanoStreamer;

/**
 * Created by ram on 15/1/28.
 */
public class StreamService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();
//        iStreamer = new CyberSmbStreamer();
        NanoStreamer.INSTANCE().start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NanoStreamer.INSTANCE().stopStream();
    }
}
