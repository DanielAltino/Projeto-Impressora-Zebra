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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zebra.printstationcard.R;

import java.io.Serializable;
import java.util.List;

public class FileAdapter extends BaseAdapter {

    private Context context;
    private List<FileInfo> fileInfoList;

    FileAdapter(Context context, List<FileInfo> fileInfoList) {
        this.context = context;
        this.fileInfoList = fileInfoList;
    }

    @Override
    public int getCount() {
        return fileInfoList.size();
    }

    @Override
    public FileInfo getItem(int position) {
        return fileInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_file, parent, false);
            viewHolder.fileName = (TextView) convertView.findViewById(R.id.fileName);
            viewHolder.fileSize = (TextView) convertView.findViewById(R.id.fileSize);
            viewHolder.fileTypeIcon = (ImageView) convertView.findViewById(R.id.fileTypeIcon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        FileInfo file = fileInfoList.get(position);

        if (file != null) {
            viewHolder.fileName.setText(file.getName());

            switch (file.getType()) {
                case DIRECTORY:
                    viewHolder.fileTypeIcon.setImageResource(R.drawable.ic_directory);
                    break;
                case DIRECTORY_UP:
                    viewHolder.fileTypeIcon.setImageResource(R.drawable.ic_directory_up);
                    break;
                case FILE:
                default:
                    viewHolder.fileTypeIcon.setImageResource(R.drawable.ic_file);
                    break;
            }

            viewHolder.fileSize.setVisibility(file.getType() == FileType.FILE ? View.VISIBLE : View.GONE);
            viewHolder.fileSize.setText(FileHelper.getHumanReadableByteCount(file.getSize()));
        }

        return convertView;
    }

    List<FileInfo> getFileInfoList() {
        return fileInfoList;
    }

    void setFileInfoList(List<FileInfo> fileInfoList) {
        this.fileInfoList = fileInfoList;
        notifyDataSetChanged();
    }

    private class ViewHolder implements Serializable {
        TextView fileName;
        TextView fileSize;
        ImageView fileTypeIcon;
    }
}
