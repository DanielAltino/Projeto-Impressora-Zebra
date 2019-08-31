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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class FileInfo implements Parcelable {
    private String fileName;
    private String path;
    private long fileSize;
    private FileType fileType;

    FileInfo(String fileName, String path, long fileSize, FileType fileType) {
        this.fileName = fileName;
        this.path = path;
        this.fileSize = fileSize;
        this.fileType = fileType;
    }

    public String getName() { return fileName; }
    String getPath() { return path; }
    FileType getType() { return fileType; }
    long getSize() { return fileSize; }

    public static final Parcelable.Creator<FileInfo> CREATOR = new Parcelable.Creator<FileInfo>() {
        @NonNull
        public FileInfo createFromParcel(@NonNull Parcel in) {
            return new FileInfo(in);
        }

        @NonNull
        public FileInfo[] newArray(int size) {
            return new FileInfo[size];
        }
    };

    private FileInfo(@NonNull Parcel in) {
        fileName = in.readString();
        path = in.readString();
        fileSize = in.readLong();
        fileType = (FileType) in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(fileName);
        dest.writeString(path);
        dest.writeLong(fileSize);
        dest.writeSerializable(fileType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfo that = (FileInfo) o;

        if (fileSize != that.fileSize) return false;
        if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (fileType != that.fileType) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = fileName != null ? fileName.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (int) (fileSize ^ (fileSize >>> 32));
        result = 31 * result + (fileType != null ? fileType.hashCode() : 0);
        return result;
    }
}
