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


import com.evideo.sambaprovider.BuildConfig;
import com.evideo.sambaprovider.browsing.BrowsingException;
import com.evideo.sambaprovider.util.Logs;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class BroadcastUtils {

    public static final int NBT_PORT = 137;
    private static final String TAG = "BroadcastUtils";

    private static final int FILE_SERVER_NODE_TYPE = 0x20;
    private static final int SERVER_NAME_LENGTH = 15;
    private static final String SERVER_NAME_CHARSET = "US-ASCII";

    /**
     * 生成一个NetBIOS名称查询请求.
     * NetBIOS服务的协议标准:https://tools.ietf.org/html/rfc1002
     * Section 4.2.12
     */
    static byte[] createPacket(int transId) {
        ByteBuffer os = ByteBuffer.allocate(50);

        char broadcastFlag = 0x0010;
        char questionCount = 1;
        char answerResourceCount = 0;
        char authorityResourceCount = 0;
        char additionalResourceCount = 0;

        os.putChar((char) transId);
        os.putChar(broadcastFlag);
        os.putChar(questionCount);
        os.putChar(answerResourceCount);
        os.putChar(authorityResourceCount);
        os.putChar(additionalResourceCount);

        // Length of name. 16 bytes of name encoded to 32 bytes.
        os.put((byte) 0x20);

        // '*' character encodes to 2 bytes.
        os.put((byte) 0x43);
        os.put((byte) 0x4b);

        // Write the remaining 15 nulls which encode to 30* 0x41
        for (int i = 0; i < 30; i++) {
            os.put((byte) 0x41);
        }

        // Length of next segment.
        os.put((byte) 0);

        // Question type: Node status
        os.putChar((char) 0x21);

        // Question class: Internet
        os.putChar((char) 0x01);

        return os.array();
    }

    /**
     * 解析对NetBIOS名称请求查询的正面响应
     * Parses a positive response to NetBIOS name request query.
     * https://tools.ietf.org/html/rfc1002
     * Section 4.2.13
     */
    static List<String> extractServers(byte[] data, int expectedTransId) throws BrowsingException {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

            int transId = buffer.getChar();
            if (transId != expectedTransId) {
                // This response is not to our broadcast.

                if (BuildConfig.DEBUG) Logs.dBrowsing(TAG, "Irrelevant broadcast response");

                return Collections.emptyList();
            }

            skipBytes(buffer, 2); // Skip flags.

            skipBytes(buffer, 2); // No questions.
            skipBytes(buffer, 2); // Skip answers count.
            skipBytes(buffer, 2); // No authority resources.
            skipBytes(buffer, 2); // No additional resources.

            int nameLength = buffer.get();
            skipBytes(buffer, nameLength);

            skipBytes(buffer, 1);

            int nodeStatus = buffer.getChar();
            if (nodeStatus != 0x20 && nodeStatus != 0x21) {
                throw new BrowsingException("Received negative response for the broadcast");
            }

            skipBytes(buffer, 2);
            skipBytes(buffer, 4);
            skipBytes(buffer, 2);

            int addressListEntryCount = buffer.get();

            List<String> servers = new ArrayList<>();
            for (int i = 0; i < addressListEntryCount; i++) {
                byte[] nameArray = new byte[SERVER_NAME_LENGTH];
                buffer.get(nameArray, 0, SERVER_NAME_LENGTH);

                final String serverName = new String(nameArray, Charset.forName(SERVER_NAME_CHARSET));
                final int type = buffer.get();

                if (type == FILE_SERVER_NODE_TYPE) {
                    servers.add(serverName.trim());
                }

                skipBytes(buffer, 2);
            }

            for (String service :
                    servers) {
                Logs.dBrowsing(TAG,"解析后：" + service);
            }

            return servers;
        } catch (BufferUnderflowException e) {
            Logs.eBrowsing(TAG, "Malformed incoming packet");

            return Collections.emptyList();
        }
    }

    /**
     * 获取ipv4地址
     * @return
     */
    public static String getIpV4Address() {

        try {
            // 获取本地设备的所有网络接口
            Enumeration<NetworkInterface> enumerationNi = NetworkInterface
                    .getNetworkInterfaces();
            while (enumerationNi.hasMoreElements()) {
                NetworkInterface networkInterface = enumerationNi.nextElement();
                String interfaceName = networkInterface.getDisplayName();
                Logs.iBrowsing(TAG,"网络名字" + interfaceName);

                // 如果是有限网卡
                if (interfaceName.equals("eth0")) {
                    Enumeration<InetAddress> enumIpAddr = networkInterface
                            .getInetAddresses();

                    while (enumIpAddr.hasMoreElements()) {
                        // 返回枚举集合中的下一个IP地址信息
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        // 不是回环地址，并且是ipv4的地址
                        if (!inetAddress.isLoopbackAddress()
                                && inetAddress instanceof Inet4Address) {
                            Logs.iBrowsing(TAG,inetAddress.getHostAddress() + "   ");

                            return inetAddress.getHostAddress();
                        }
                    }
                    //  如果是无限网卡
                } else if (interfaceName.equals("wlan0")) {
                    Enumeration<InetAddress> enumIpAddr = networkInterface
                            .getInetAddresses();

                    while (enumIpAddr.hasMoreElements()) {
                        // 返回枚举集合中的下一个IP地址信息
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        // 不是回环地址，并且是ipv4的地址
                        if (!inetAddress.isLoopbackAddress()
                                && inetAddress instanceof Inet4Address) {
                            Logs.iBrowsing(TAG,inetAddress.getHostAddress() + "   ");

                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }

        } catch (Exception e) {
            Logs.eBrowsing(TAG,"cant getIpV4Address :"+e.toString());
        }

        return "";
    }

    /**
     * 获取广播地址
     *
     * @return
     * @throws BrowsingException
     * @throws SocketException
     */
    static List<String> getBroadcastAddress() throws BrowsingException, SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        List<String> broadcastAddresses = new ArrayList<>();

        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback()) {
                continue;
            }

            for (InterfaceAddress interfaceAddress :
                    networkInterface.getInterfaceAddresses()) {
                InetAddress broadcast = interfaceAddress.getBroadcast();

                if (broadcast != null) {
                    broadcastAddresses.add(broadcast.toString().substring(1));
                }
            }
        }

        return broadcastAddresses;
    }

    private static void skipBytes(ByteBuffer buffer, int bytes) {
        for (int i = 0; i < bytes; i++) {
            buffer.get();
        }
    }


    /**
     * 发送名称查询广播 UDP
     *
     * @param socket
     * @param address
     * @throws IOException
     */
    public static void sendNameQueryBroadcast(
            DatagramSocket socket,
            InetAddress address, int transId) throws IOException {
        byte[] data = createPacket(transId);
        int dataLength = data.length;

        DatagramPacket packet = new DatagramPacket(data, 0, dataLength, address, NBT_PORT);
        socket.send(packet);

        if (BuildConfig.DEBUG) Logs.dBrowsing(TAG, "Broadcast package sent");
    }
}
