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

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by zouyingjun on 2018/3/15.
 * 统一管理服务名数据 ip和对应的服务器列表
 */

public class ServiceHelper{

    public static final String IP_LAN_DEFAULT = "255.255.255.255";
    private static final ServiceHelper ourInstance = new ServiceHelper();

    public static ServiceHelper getInstance() {
        return ourInstance;
    }

    private ServiceHelper() {
    }

    /*ip-servieName*/
    private Map<String,List<String>> mMap = new HashMap<>();
    private ServiesListListener mListener;

    public void setListener(ServiesListListener listener){
        this.mListener = listener;
    }

    /**
     *
     * 增加数据并通知接收者
     * @param ip
     * @param servieName
     */
    public synchronized void addServiceData(String ip, String servieName){
        if(TextUtils.isEmpty(ip) || TextUtils.isEmpty(servieName)){
            return;
        }

        if(mMap.containsKey(ip)){
            List<String> strings = mMap.get(ip);
            mMap.remove(ip);
            if(!strings.contains(servieName)){
                strings.add(servieName);
                mMap.put(ip,strings);
            }
        }else {
            ArrayList<String> list = new ArrayList<>();
            list.add(servieName);
            mMap.put(ip,list);
        }

        if(mListener != null){
            mListener.dataChange();
        }

        Log.d("addServiceData", "addServiceData: ip"+ip+" servieName"+servieName);
    }

    public interface ServiesListListener{
        void dataChange();
    }

    public Map<String, List<String>> getMap() {
        return mMap;
    }

    /**
     * 获取可用设备
     * @return
     */
    public List<Device> getDevice(){
        if (mMap == null){
            return null;
        }
        List<Device> devices = new ArrayList<>();
        Iterator it = mMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String ip = (String) entry.getKey();
            List<String> services = (List<String>) entry.getValue();
            Device device = new Device();
            device.setIp(ip);
            device.setServices(services);
            devices.add(device);
        }
        return devices;
    }
}
