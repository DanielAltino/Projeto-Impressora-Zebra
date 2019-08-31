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

public class PreferenceHelper {

    private static final String DEFAULT_TEMPLATE_DIRECTORY = "ZebraTechnologies" + File.separator + "TemplateData" + File.separator + "TemplateFiles" + File.separator;
    private static final String DEFAULT_TEMPLATE_IMAGE_DIRECTORY = "ZebraTechnologies" + File.separator + "TemplateData" + File.separator + "TemplateImages" + File.separator;

    public static String getDefaultTemplateDirectoryPath() {
        return new File(Environment.getExternalStorageDirectory().getPath(), DEFAULT_TEMPLATE_DIRECTORY).getPath();
    }

    public static String getDefaultTemplateImageDirectoryPath() {
        return new File(Environment.getExternalStorageDirectory().getPath(), DEFAULT_TEMPLATE_IMAGE_DIRECTORY).getPath();
    }
}
