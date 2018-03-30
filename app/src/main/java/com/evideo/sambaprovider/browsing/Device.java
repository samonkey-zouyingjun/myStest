package com.evideo.sambaprovider.browsing;

import java.util.List;

/**
 * Created by zouyingjun on 2018/3/29.
 */

public class Device {
    private String ip;
    private List<String> services;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return "Device{" +
                "ip='" + ip + '\'' +
                ", services=" + services +
                '}';
    }
}
