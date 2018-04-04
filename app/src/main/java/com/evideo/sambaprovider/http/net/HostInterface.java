package com.evideo.sambaprovider.http.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

public class HostInterface {
    public static final int IPV4_BITMASK = 1;
    public static final int IPV6_BITMASK = 16;
    public static final int LOCAL_BITMASK = 256;
    private static final String TAG = "org.cybergarage.net.HostInterface";
    public static boolean USE_LOOPBACK_ADDR = false;
    public static boolean USE_ONLY_IPV4_ADDR = false;
    public static boolean USE_ONLY_IPV6_ADDR = false;
    private static String ifAddress = "";

    public static final void setInterface(String ifaddr) {
        System.out.println("����=======================================");
        ifAddress = ifaddr;
    }

    public static final String getInterface() {
        return ifAddress;
    }

    private static final boolean hasAssignedInterface() {
        return ifAddress.length() > 0;
    }

    private static final boolean isUsableAddress(InetAddress addr) {
        if (!USE_LOOPBACK_ADDR && (addr.isLoopbackAddress() || addr.isLinkLocalAddress())) {
            return false;
        }
        if (USE_ONLY_IPV4_ADDR && (addr instanceof Inet6Address)) {
            return false;
        }
        if (USE_ONLY_IPV6_ADDR && (addr instanceof Inet4Address)) {
            return false;
        }
        return true;
    }

    public static final int getNHostAddresses() {
        int nHostAddrs = 1;
        if (hasAssignedInterface()) {
            System.out.println("hasAssignedInterface");
        } else {
            nHostAddrs = 0;

            Enumeration nis = null;
            try {
                nis = NetworkInterface.getNetworkInterfaces();
                while (nis.hasMoreElements()) {
                    Enumeration<InetAddress> addrs = ((NetworkInterface) nis.nextElement()).getInetAddresses();
                    while (addrs.hasMoreElements()) {
                        if (isUsableAddress((InetAddress) addrs.nextElement())) {
                            nHostAddrs++;
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        return nHostAddrs;
    }

    public static final InetAddress[] getInetAddress(int ipfilter, String[] interfaces) {
        Enumeration nis;
        if (interfaces != null) {
            Vector iflist = new Vector();
            for (String byName : interfaces) {
                try {
                    NetworkInterface ni = NetworkInterface.getByName(byName);
                    if (ni != null) {
                        iflist.add(ni);
                    }
                } catch (SocketException e) {
                }
            }
            nis = iflist.elements();
        } else {
            try {
                nis = NetworkInterface.getNetworkInterfaces();
            } catch (SocketException e2) {
                return null;
            }
        }
        ArrayList addresses = new ArrayList();
        while (nis.hasMoreElements()) {
            Enumeration addrs = ((NetworkInterface) nis.nextElement()).getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress addr = (InetAddress) addrs.nextElement();
                if ((ipfilter & 256) != 0 || !addr.isLoopbackAddress()) {
                    if ((ipfilter & 1) != 0 && (addr instanceof Inet4Address)) {
                        addresses.add(addr);
                    } else if ((ipfilter & 16) != 0 && (addr instanceof InetAddress)) {
                        addresses.add(addr);
                    }
                }
            }
        }
        return (InetAddress[]) addresses.toArray(new InetAddress[0]);
    }

    public static final String getHostAddress(int n) {
        if (hasAssignedInterface()) {
            return getInterface();
        }
        int hostAddrCnt = 0;
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                Enumeration<InetAddress> addrs = ((NetworkInterface) nis.nextElement()).getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = (InetAddress) addrs.nextElement();
                    if (isUsableAddress(addr)) {
                        if (hostAddrCnt >= n) {
                            return addr.getHostAddress();
                        }
                        hostAddrCnt++;
                    }
                }
            }
        } catch (Exception e) {
        }
        return "";
    }

    public static final boolean isIPv6Address(String host) {
        try {
            if (InetAddress.getByName(host) instanceof Inet6Address) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static final boolean isIPv4Address(String host) {
        try {
            if (InetAddress.getByName(host) instanceof Inet4Address) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static final boolean hasIPv4Addresses() {
        int addrCnt = getNHostAddresses();
        for (int n = 0; n < addrCnt; n++) {
            if (isIPv4Address(getHostAddress(n))) {
                return true;
            }
        }
        return false;
    }

    public static final boolean hasIPv6Addresses() {
        int addrCnt = getNHostAddresses();
        for (int n = 0; n < addrCnt; n++) {
            if (isIPv6Address(getHostAddress(n))) {
                return true;
            }
        }
        return false;
    }

    public static final String getIPv4Address() {
        int addrCnt = getNHostAddresses();
        for (int n = 0; n < addrCnt; n++) {
            String addr = getHostAddress(n);
            if (isIPv4Address(addr)) {
                return addr;
            }
        }
        return "";
    }

    public static final String getIPv6Address() {
        int addrCnt = getNHostAddresses();
        for (int n = 0; n < addrCnt; n++) {
            String addr = getHostAddress(n);
            if (isIPv6Address(addr)) {
                return addr;
            }
        }
        return "";
    }

    public static final String getHostURL(String host, int port, String uri) {
        String hostAddr = host;
        if (isIPv6Address(host)) {
            hostAddr = "[" + host + "]";
        }
        return "http://" + hostAddr + ":" + Integer.toString(port) + uri;
    }
}
