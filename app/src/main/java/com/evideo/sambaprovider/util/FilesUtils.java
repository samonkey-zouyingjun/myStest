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

package com.evideo.sambaprovider.util;

import android.system.ErrnoException;

/**
 * Created by zouyingjun on 2018/3/13.
 * 文件操作的工具类
 * -获取文件信息（大小）
 * -文件操作（打开，复制，移动，删除）
 */

public final class FilesUtils {

    /**
     * 打开文件
     *
     * @param handler
     * @param uri
     * @return
     * @throws ErrnoException
     */
    private static int openDir(long handler, String uri) throws ErrnoException {
        return 0;
    }

    ;

//    private native StructStat stat(long handler, String uri) throws ErrnoException;
//
//    private native void createFile(long handler, String uri) throws ErrnoException;
//
//    private native void mkdir(long handler, String uri) throws ErrnoException;
//
//    private native void rmdir(long handler, String uri) throws ErrnoException;
//
//    private native void rename(long handler, String uri, String newUri) throws ErrnoException;
//
//    private native void unlink(long handler, String uri) throws ErrnoException;
//
//    private native int openFile(long handler, String uri, String mode) throws ErrnoException;

}
