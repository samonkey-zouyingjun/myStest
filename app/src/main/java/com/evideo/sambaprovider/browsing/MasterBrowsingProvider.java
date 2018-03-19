/*
 * zouyingjun 2018年3月16日15:15:58
 *
 * 局域网内获取可用服务分组
 *
 * -MASTER_BROWSING_DIR = "smb://:
 *
 *  Just as smb://server/ lists shares and smb://workgroup/
 *  lists servers, the smb:// URL lists all available workgroups on a netbios LAN. Again,
 *  in this context many methods are not valid and return default values
 *  (e.g. isHidden will always return false).
 *
 *  see:https://jcifs.samba.org/src/docs/api/jcifs/smb/SmbFile.html
 */

package com.evideo.sambaprovider.browsing;

import com.evideo.sambaprovider.base.DirectoryEntry;
import com.evideo.sambaprovider.nativefacade.SmbClient;
import com.evideo.sambaprovider.nativefacade.SmbDir;
import com.evideo.sambaprovider.thread.TerminableThreadPool;
import com.evideo.sambaprovider.util.Logs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class MasterBrowsingProvider implements NetworkBrowsingProvider {
  private static final String MASTER_BROWSING_DIR = "smb://";

  private final SmbClient mClient;
  private TerminableThreadPool mMasterThread;

  MasterBrowsingProvider(SmbClient client) {
    mClient = client;
  }

  @Override
  public void getServersAsy() {
    Runnable runTask = new Runnable() {
      @Override
      public void run() {
        try {
          SmbDir rootDir = mClient.openDir(MASTER_BROWSING_DIR);
          List<DirectoryEntry> workgroups = getDirectoryChildren(rootDir);
          for (DirectoryEntry workgroup : workgroups) {
            if (workgroup.getType() == DirectoryEntry.WORKGROUP) {
              List<DirectoryEntry> servers = getDirectoryChildren
                      (mClient.openDir(MASTER_BROWSING_DIR + workgroup.getName()));

              for (DirectoryEntry server : servers) {
                if (server.getType() == DirectoryEntry.SERVER) {
                  //todo 获取局域网内的源地址
                  ServiceHelper.getInstance().addServiceData(ServiceHelper.IP_LAN_DEFAULT,
                          server.getName());
                }
              }
            }
          }
        } catch (IOException e) {
          Logs.e(Logs.TAG_BROWSING," Master getServersAsy:"+e.toString());
        }
      }
    };

    if(mMasterThread == null){
      mMasterThread = new TerminableThreadPool(runTask);
      mMasterThread.start();
    }else{
      Logs.e(Logs.TAG_BROWSING,"mMasterThread is start!");
    }

  }

  private static List<DirectoryEntry> getDirectoryChildren(SmbDir dir) throws IOException {
    List<DirectoryEntry> children = new ArrayList<>();

    DirectoryEntry currentEntry;
    while ((currentEntry = dir.readDir()) != null) {
      children.add(currentEntry);
    }

    return children;
  }
}
