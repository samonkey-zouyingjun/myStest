package com.evideo.sambaprovider.util;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by zouyingjun on 15/1/23.
 */
public class SambaUtil {

    public final static String TAG = "SambaUtil";
    public final static int IO_BUFFER_SIZE = 8 * 1024;
    public final static boolean DEBUG = true;
    public final static String SMB_URL_LAN = "smb://";
    public final static String SMB_URL_WORKGROUP = "smb://workgroup/";
    public static final String CONTENT_EXPORT_URI = "/smb=";


    public static final String SUPPORTED_VIDEOS = "_mp4_3gp_mkv_mov_avi_rmvb_wav_m3u8_";//mkv_mov_avi_rmvb_wav_m3u8_

    public static final String getVideoMimeType(String path) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (TextUtils.isEmpty(extension)) {
            return null;
        }
        extension = extension.toLowerCase();
        if (!SambaUtil.SUPPORTED_VIDEOS.contains(extension)) {
            return null;
        }
        return new StringBuilder("video/").append(extension).toString();
    }

    /**
     * For example, if path is xxx/yyy/zzz/AAA.bbb<p/>
     * It'll return AAA.bbb
     */
    public final static String getFileName(String path) {
        if (path == null) {
            return path;
        }
        if (!path.contains("/")) {
            return path;
        }
        int index = path.lastIndexOf("/");
        if (index < 0 || index + 1 >= path.length()) {
            return path;
        }
        return path.substring(index + 1);
    }

    /**
     * For example, if path is xxx/yyy/zzz/AAA.bbb<p/>
     * It'll return AAA
     */
    public final static String getNakedName(String path) {
        path = getFileName(path);
        if (path == null) {
            return path;
        }
        if (!path.contains(".")) {
            return path;
        }
        int index = path.lastIndexOf(".");
        if (index <= 0 || index + 1 >= path.length()) {
            return null;
        }
        return path.substring(0, index);
    }

    public final static String autoRename(String name) {
        String nakedName = getNakedName(name);
        String suffix = name.replace(nakedName, "");
        return new StringBuilder(nakedName).append("-").append(System.currentTimeMillis()).append(suffix).toString();
    }

    public final static String strsToString(String[] strs) {
        if (strs == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(strs.length);
        for (String str : strs) {
            builder.append("\n");
            builder.append("[");
            builder.append(str);
            builder.append("]");
        }
        return builder.toString();
    }

    public final static String wrapSmbFileUrl(String parent, String name) {
        if (TextUtils.isEmpty(parent) || TextUtils.isEmpty(name)) {
            return null;
        }
        StringBuilder builder = new StringBuilder(parent);
        if (!parent.endsWith("/")) {
            builder.append("/");
        }
        if (name.endsWith("/")) {
            int index = name.length() - 1;
            name = name.substring(0, index - 1);
        }
        builder.append(name);
        return builder.toString();
    }


    /**
     * Turn from <b>"/smb=XXX"</b> to <b>"smb://XXX"</b>
     */
    public final static String cropStreamSmbURL(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        if (!url.startsWith(CONTENT_EXPORT_URI)) {
            return url;
        }
        if (url.length() <= CONTENT_EXPORT_URI.length()) {
            return url;
        }
        String filePaths = SMB_URL_LAN + url.substring(CONTENT_EXPORT_URI.length());
        int indexOf = filePaths.indexOf("&");
        if (indexOf != -1) {
            filePaths = filePaths.substring(0, indexOf);
        }
        return filePaths;
    }


    public final static boolean isSmbUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        return url.trim().startsWith(SMB_URL_LAN);
    }

    /**
     * Turn from <b>"smb://XXX"</b> to <b>"http://ip:port/smb=XXX"</b>
     */
    public final static String wrapStreamSmbURL(String url, String ip, int port) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        if (!url.startsWith(SMB_URL_LAN)) {
            return null;
        }
        try {
            url = url.substring(SMB_URL_LAN.length());
            url = URLEncoder.encode(url, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder builder = new StringBuilder("http://")
                .append(ip)//
                .append(File.pathSeparator)//
                .append(port)//
                .append(CONTENT_EXPORT_URI);//
        builder.append(url);
        return builder.toString();
    }
}
