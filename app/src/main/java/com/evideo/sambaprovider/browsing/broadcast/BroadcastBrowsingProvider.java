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

package com.evideo.sambaprovider.browsing.broadcast;


import com.evideo.sambaprovider.browsing.NetworkBrowsingProvider;
import com.evideo.sambaprovider.browsing.ServiceHelper;
import com.evideo.sambaprovider.thread.TerminableThreadPool;
import com.evideo.sambaprovider.util.Logs;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.List;

public class BroadcastBrowsingProvider implements NetworkBrowsingProvider {

    private static final int LISTENING_TIMEOUT = 3000; // 3 seconds.
    private int mTransId = 0;
    private TerminableThreadPool mBroadcastThread;
    private static final String TAG = "BroadcastBrowsingProvid";

    /**
     * 方案一：自动搜索，向广播地址发送请求广播，由此向下分发请求广播
     * 效果差，速度快
     * 由255自动分发，如果环境好的话能较快获取搜索结果，但是可靠性差，常常搜索不到
     * @return
     * @throws BrowsingException
     */
    @Override
    public void getServersAsy() {
        if (mBroadcastThread == null) {
            mBroadcastThread = new TerminableThreadPool(getBroadcastTask());
            mBroadcastThread.start();
        } else {
            Logs.eBrowsing(TAG, "mBroadcastThread has start!");
        }
    }


    private Runnable getBroadcastTask() {
        return new Runnable() {
            @Override
            public void run() {
                //获取广播地址
                try {
                    List<String> broadcastAddresses = BroadcastUtils.getBroadcastAddress();
                    for (String ad : broadcastAddresses) {
                        Logs.dBrowsing(TAG,"getServers: broadcast address:" + ad);
                    }
                    //发送广播
                    for (String add : broadcastAddresses) {
                        mTransId++;
                        DatagramSocket socket = new DatagramSocket();
                        InetAddress address = InetAddress.getByName(add);
                        BroadcastUtils.sendNameQueryBroadcast(socket, address, mTransId);
                        listenForServers(socket, mTransId);
                    }
                } catch (Exception e) {
                    Logs.eBrowsing(TAG, "broadcast getBroadcastTast:" + e.toString());
                }

            }
        };

    }

    /**
     * 通过套接字监听发送的广播
     *
     * @param socket
     * @return
     * @throws IOException
     */
    public void listenForServers(DatagramSocket socket, int transId) {
        try {
            socket.setSoTimeout(LISTENING_TIMEOUT);
            while (true) {
                try {
                    byte[] buf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);

                    List<String> servers = BroadcastUtils.extractServers(packet.getData(), transId);
                    for (String name : servers) {
                        ServiceHelper.getInstance().addServiceData(
                                packet.getAddress().getHostAddress(), name);
                    }
                } catch (SocketTimeoutException e) {
                    break;
                }
            }
        } catch (Exception e) {
            Logs.eBrowsing(TAG, "broadcast listenForServers:" + e.toString());
        }
    }
}
