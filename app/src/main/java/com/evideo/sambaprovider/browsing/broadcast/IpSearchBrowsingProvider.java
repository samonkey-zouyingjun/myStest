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

package com.evideo.sambaprovider.browsing.broadcast;

import android.text.TextUtils;
import com.evideo.sambaprovider.browsing.NetworkBrowsingProvider;
import com.evideo.sambaprovider.browsing.ServiceHelper;
import com.evideo.sambaprovider.thread.TerminableThreadPool;
import com.evideo.sambaprovider.util.Logs;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zouyingjun on 2018/3/15.
 * 每个网段搜索开5个搜索任务，放入大小为20的线程池中
 *
 */

public class IpSearchBrowsingProvider implements NetworkBrowsingProvider {
    private TerminableThreadPool mSearchThread1;
    private TerminableThreadPool mSearchThread2;
    private TerminableThreadPool mSearchThread3;
    private TerminableThreadPool mSearchThread4;
    private TerminableThreadPool mSearchThread5;
    private static final String TAG = "IpSearchBrowsingProvider";

    public IpSearchBrowsingProvider() {
    }

    /**
     * 方案二：扫描搜索：遍历同网段下1-254地址,发送查询请求
     * 效果好，速度慢
     * @return
     */
    @Override
    public void getServersAsy() {
        String ipV4Address = BroadcastUtils.getIpV4Address();

        if (TextUtils.isEmpty(ipV4Address)) {
            Logs.eBrowsing(TAG, "ipV4Address is null!");
            return;
        }

        /*192.168.199.*/
        String subHead = ipV4Address.substring(0, ipV4Address.lastIndexOf(".")+1);
        Logs.dBrowsing(TAG, "start:" +
                System.currentTimeMillis()+"ipV4Address substring: " + subHead);

        //获取广播地址
        final List<String> ad1 = new ArrayList<>();
        final List<String> ad2 = new ArrayList<>();
        final List<String> ad3 = new ArrayList<>();
        final List<String> ad4 = new ArrayList<>();
        final List<String> ad5 = new ArrayList<>();

        for (int i = 1; i < 51; i++) {
            ad1.add(subHead+i);
            ad2.add(subHead+(i+50));
            ad3.add(subHead+(i+100));
            ad4.add(subHead+(i+150));
            ad5.add(subHead+(i+200));
        }

        ad5.add(subHead+251);
        ad5.add(subHead+252);
        ad5.add(subHead+253);
        ad5.add(subHead+254);

        if (mSearchThread1 == null) {
            mSearchThread1 = new TerminableThreadPool(getRunnable(ad1));
            mSearchThread1.start();
        }else{
            Logs.eBrowsing(TAG,"getServersAsy has used!");
        }
        if (mSearchThread2 == null) {
            mSearchThread2 = new TerminableThreadPool(getRunnable(ad2));
            mSearchThread2.start();
        }
        if (mSearchThread3 == null) {
            mSearchThread3 = new TerminableThreadPool(getRunnable(ad3));
            mSearchThread3.start();
        }
        if (mSearchThread4 == null) {
            mSearchThread4 = new TerminableThreadPool(getRunnable(ad4));
            mSearchThread4.start();
        }
        if (mSearchThread5 == null) {
            mSearchThread5 = new TerminableThreadPool(getRunnable(ad5));
            mSearchThread5.start();
        }
    }

    private Runnable getRunnable(final List<String> broadcastAddresses) {
        return new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < broadcastAddresses.size(); i++) {
                    String broadcastAddress = broadcastAddresses.get(i);
                    try (DatagramSocket socket = new DatagramSocket()) {
                        InetAddress address = InetAddress.getByName(broadcastAddress);

                        BroadcastUtils.sendNameQueryBroadcast(socket, address, i);

                        socket.setSoTimeout(1000);
                        while (true) {
                            try {
                                byte[] buf = new byte[1024];
                                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                                socket.receive(packet);
                                List<String> strings = BroadcastUtils.extractServers(packet.getData(), i);

                                for (String name : strings) {
                                    //更新道ServiceProvider 注意线程安全
                                    ServiceHelper.getInstance().addServiceData(broadcastAddress,name);
                                }
                                break;
                            } catch (Exception e) {
//                                Logs.eBrowsing(TAG, "ipSearch getRunnable:"+e.toString());
                                break;
                            }
                        }

                    } catch (Exception e) {
                        Logs.eBrowsing(TAG,"getRunnable:" + e.toString());
                    }
                }
                Logs.wBrowsing(TAG, Thread.currentThread().getName()+" end:" + System.currentTimeMillis());
            }
        };
    }

    public void cancelLoadTask() {

        if (mSearchThread1 != null) {
            mSearchThread1.cancel();
        }
        if (mSearchThread2 != null) {
            mSearchThread2.cancel();
        }
        if (mSearchThread3 != null) {
            mSearchThread3.cancel();
        }
        if (mSearchThread4 != null) {
            mSearchThread4.cancel();
        }
        if (mSearchThread5 != null) {
            mSearchThread5.cancel();
        }
    }
}
