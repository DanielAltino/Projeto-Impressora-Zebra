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

package com.zebra.printstationcard.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class FileHelper {

    public static String getHumanReadableByteCount(long bytes) {
        int unit = 1024;

        if (bytes < unit) {
            return bytes + " B";
        }

        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "kMGTPE".charAt(exp - 1);

        return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static void sortFileInfoList(List<FileInfo> fileInfoList) {
        Collections.sort(fileInfoList, new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo lhs, FileInfo rhs) {
                if (lhs.getType() == FileType.DIRECTORY_UP && rhs.getType() != FileType.DIRECTORY_UP) {
                    return -1;
                } else if (lhs.getType() != FileType.DIRECTORY_UP && rhs.getType() == FileType.DIRECTORY_UP) {
                    return 1;
                } else if (lhs.getType() == FileType.DIRECTORY && rhs.getType() == FileType.DIRECTORY) {
                    return lhs.getName().compareTo(rhs.getName());
                } else if (lhs.getType() == FileType.DIRECTORY && rhs.getType() != FileType.DIRECTORY) {
                    return -1;
                } else if (lhs.getType() != FileType.DIRECTORY && rhs.getType() == FileType.DIRECTORY) {
                    return 1;
                } else {
                    return lhs.getName().compareTo(rhs.getName());
                }
            }
        });
    }

    public static String readFileToString(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append('\n');
        }

        br.close();

        return sb.toString();
    }
}
