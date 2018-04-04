package com.evideo.sambaprovider.nativefacade;

import android.system.StructStat;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

/**
 * Created by zouyingjun on 2018/3/30.
 */

public class SmbFileHttpWrapper extends SambaFile {

    private String mUrl;

    public SmbFileHttpWrapper(long nativeHandler, int nativeFd) {
        super(nativeHandler, nativeFd);
    }


    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    private static final String TAG = "SmbFileHttpWrapper";

    public String getName() {

        if(TextUtils.isEmpty(mUrl)){
            return null;
        }
        String name = mUrl.substring(mUrl.lastIndexOf("/")+1, mUrl.lastIndexOf("."));
        Log.d(TAG, "getName: "+name);

        return name;
    }

    public long getLength() {
        StructStat fstat = null;
        try {
            fstat = fstat();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(fstat == null){
            return 0;
        }
        return fstat.st_size;
    }

    public long getLastModified(){

        StructStat fstat = null;
        try {
            fstat = fstat();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(fstat == null){
            return 0;
        }

        return fstat.st_mtime;
    }

}
