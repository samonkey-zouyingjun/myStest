/*
 * Copyright 2018 Google Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.evideo.sambaprovider.util;

import android.util.Log;

import com.evideo.sambaprovider.BuildConfig;

/**
 * Created by zouyingjun on 2018/3/14.
 */

public class Logs {
    public static final String TAG_BROWSING = "tag_browsing";
    private static final boolean mIsDebug = BuildConfig.DEBUG;
    private static final String TAG = "zouyingjun";
    /*debug log*/
    public static void d(String msg){
        if(mIsDebug){
            Log.d(TAG, msg);
        }
    }
    public static void w(String msg){
        if(mIsDebug){
            Log.w(TAG, msg);
        }
    }
    public static void e(String msg){
        if(mIsDebug){
            Log.e(TAG, msg);
        }
    }

    public static void i(String msg){
        if(mIsDebug){
            Log.i(TAG, msg);
        }
    }
    /*自定义log*/
    public static void i(String tag, String s) {
        Log.i(tag, s);
    }
    public static void e(String tag, String s) {
        Log.e(tag, s);
    }

    /*搜索网络模块 log*/
    public static void eBrowsing(String tag, String msg){
        Log.e(TAG_BROWSING, msg+"      "+tag);
    }
    public static void dBrowsing(String tag, String msg){
        Log.d(TAG_BROWSING, msg+"      "+tag);
    }
    public static void iBrowsing(String tag, String msg){
        Log.i(TAG_BROWSING, msg+"      "+tag);
    }
    public static void wBrowsing(String tag, String msg){
        Log.w(TAG_BROWSING, msg+"      "+tag);
    }

}
