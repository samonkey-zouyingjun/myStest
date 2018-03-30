/*
 * Copyright 2017 Google Inc.
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

package com.evideo.sambaprovider.browsing;

import android.net.Uri;
import android.text.TextUtils;

import com.evideo.sambaprovider.base.DirectoryEntry;
import com.evideo.sambaprovider.browsing.broadcast.BroadcastBrowsingProvider;
import com.evideo.sambaprovider.browsing.broadcast.IpSearchBrowsingProvider;
import com.evideo.sambaprovider.nativefacade.SmbClient;
import com.evideo.sambaprovider.nativefacade.SmbDir;
import com.evideo.sambaprovider.util.Logs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 发现Samba服务器及其下的本地网络上可用的共享
 * 开启6条线程去搜索可用服务
 * -搜索局域网可用 1（暂时舍弃，ip不好获取，功能上手动搜索可以弥补）
 * -搜索广播可用 1
 * -手动搜索可用 5
 */
public class NetworkBrowser {
    private static final Uri SMB_BROWSING_URI = Uri.parse("smb://");

    private static final String TAG = "NetworkBrowser";
    private final SmbClient mClient;

    public NetworkBrowser(SmbClient client) {
        mClient = client;
    }

    /**
     * 获取可用服务名
     *
     * @throws BrowsingException
     */
    public void getServers() {

    /*根据ip获取*/
        IpSearchBrowsingProvider ipSearch = new IpSearchBrowsingProvider();
        ipSearch.getServersAsy();

    //todo ip不好获取，暂时舍弃此获取方式
    /*局域网获取*//*
    MasterBrowsingProvider master = new MasterBrowsingProvider(mClient);
    master.getServersAsy();*/

    /*根据广播获取*/
        BroadcastBrowsingProvider broadcast = new BroadcastBrowsingProvider();
        broadcast.getServersAsy();
    }


    public List<DirectoryEntry> getRootDir(String serviceName){
        List<DirectoryEntry> servers = new ArrayList<>();
        try {
            servers = getDirectoryChildren
                    (mClient.openDir("smb://" + serviceName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return servers;    }

    private static List<DirectoryEntry> getDirectoryChildren(SmbDir dir) throws IOException {
        List<DirectoryEntry> children = new ArrayList<>();

        DirectoryEntry currentEntry;
        while ((currentEntry = dir.readDir()) != null) {
            children.add(currentEntry);
        }

        return children;
    }

    /**
     * 根据服务名获取子目录
     */
    public List<DirectoryEntry> getChildDirByUri(String serverUri) {
        Logs.dBrowsing(TAG, "getChildDirByUri: " + serverUri);
        if (mClient == null) {
            Logs.eBrowsing(TAG, "getChildDirByPWD mClient is null!");
            return null;
        }

        List<DirectoryEntry> de = new ArrayList<>();
        try {
            SmbDir serverDir = mClient.openDir(serverUri);

            DirectoryEntry shareEntry;
            while ((shareEntry = serverDir.readDir()) != null) {

                de.add(shareEntry);
                /*if (shareEntry.getType() == DirectoryEntry.FILE_SHARE) {

                    String path = shareEntry.getName().trim();
                    *//*过滤"$"结尾的文件夹*//*
                    if (TextUtils.isEmpty(path) || path.endsWith("$")) {
                        continue;
                    }

                    shares.add(serverUri + "/" + shareEntry.getName().trim());
                } else {
                    Logs.iBrowsing(TAG, "Unsupported entry type: " + String.valueOf(shareEntry.getType()));
                }*/
            }
        } catch (IOException e) {
            de = null;
            Logs.eBrowsing(TAG, "getChildDirByUri:" + e.toString());
        }
        return de;
    }


    /**
     * 获取子路径
     * https://jcifs.samba.org/src/docs/api/jcifs/smb/SmbFile.html
     */
    public static String creatUrlByPwd(Iconfig config) {

        if (config == null) {
            return SMB_BROWSING_URI.toString();
        }

        //无密码
        if (TextUtils.isEmpty(config.user) || TextUtils.isEmpty(config.pwd)
                && TextUtils.isEmpty(config.ip)) {

            //用服务名访问 smb://ZOUYINFJUN-PC
            return SMB_BROWSING_URI + config.serviceName;
        }

        //smb://;zyj:1234@192.168.31.86/
        StringBuilder wrappedHost = new StringBuilder(SMB_BROWSING_URI + ";")//
                .append(config.user)//
                .append(":")//
                .append(config.pwd)//
                .append("@")//
                .append(config.ip)//
                ;

        return wrappedHost.toString();

    }
}
