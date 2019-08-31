/***********************************************
 * CONFIDENTIAL AND PROPRIETARY
 *
 * The source code and other information contained herein is the confidential and exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published,
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 *
 * Copyright ZIH Corp. 2018
 *
 * ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.printstationcard.util;

import android.os.Environment;

import java.io.File;

public class PathHelper {
    public static final String PATH_EXTERNAL_STORAGE_DIRECTORY = Environment.getExternalStorageDirectory().getPath();

    public static String formatDisplayPath(String path) {
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }

        if (path.startsWith(PATH_EXTERNAL_STORAGE_DIRECTORY)) {
            path = path.substring(PATH_EXTERNAL_STORAGE_DIRECTORY.length());
        }

        return path;
    }

    public static boolean isExternalStorageDirectoryRoot(String path) {
        return "/".equals(PathHelper.formatDisplayPath(path));
    }
}
