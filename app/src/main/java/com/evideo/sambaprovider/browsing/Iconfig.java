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

package com.evideo.sambaprovider.browsing;

/**
 * Created by zouyingjun on 2018/3/16.
 * 存储单个服务器和ip关联信息
 */

public class Iconfig {
    String ip;
    String domain;
    String user;
    String pwd;
    String serviceName;

    /**
     * ipSearch获取的服务列表和ip绑定
     * @param ip
     * @param domain
     * @param user
     * @param pwd
     * @param serviceName
     */
    public Iconfig(String ip, String domain, String user, String pwd, String serviceName) {
        this.ip = ip;
        this.domain = domain;
        this.user = user;
        this.pwd = pwd;
        this.serviceName = serviceName;
    }

    /**
     * broadcast和mast获取的只有serviceName
     * @param serviceName
     */
    public Iconfig(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return "Iconfig{" +
                "ip='" + ip + '\'' +
                ", domain='" + domain + '\'' +
                ", user='" + user + '\'' +
                ", pwd='" + pwd + '\'' +
                ", serviceName='" + serviceName + '\'' +
                '}';
    }
}
