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

import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

/**
 * Created by zouyingjun on 2018/3/15.
 * 和url相关的工具
 * -路径解析
 * -服务列表获取
 */

public final class UrlUtils {

    private static final String TAG = "UrlUtils";

    /**
     * 解析路径是否正确，否则返回空
     *
     * @param path
     * @return
     */
    public static String[] parseSharePath(String path) {
        if (path.startsWith("\\")) {
            // Possibly Windows share path
            if (path.length() == 1) {
                return null;
            }
            final int endCharacter = path.endsWith("\\") ? path.length() - 1 : path.length();
            final String[] components = path.substring(2, endCharacter).split("\\\\");
            return components.length == 2 ? components : null;
        } else {
            // Try SMB URI
            final Uri smbUri = Uri.parse(path);

            final String host = smbUri.getAuthority();
            if (TextUtils.isEmpty(host)) {
                return null;
            }

            final List<String> pathSegments = smbUri.getPathSegments();
            if (pathSegments.size() != 1) {
                return null;
            }
            final String share = pathSegments.get(0);
            return new String[]{host, share};
        }
    }
}
