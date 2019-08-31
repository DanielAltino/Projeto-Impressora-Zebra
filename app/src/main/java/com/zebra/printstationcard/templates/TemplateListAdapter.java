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

package com.zebra.printstationcard.templates;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zebra.printstationcard.R;

import java.util.List;

public class TemplateListAdapter extends ArrayAdapter {

    @LayoutRes
    private int itemLayoutId;
    private List<String> templateNameList;

    public TemplateListAdapter(@NonNull Context context, @LayoutRes int resource, List<String> templateNameList) {
        super(context, resource);
        this.itemLayoutId = resource;
        this.templateNameList = templateNameList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(itemLayoutId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.templateName = (TextView) convertView.findViewById(R.id.templateName);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String templateName = templateNameList.get(position);
        if (templateName != null) {
            viewHolder.templateName.setText(templateName);
        }

        return convertView;
    }

    public int getCount() {
        return templateNameList.size();
    }

    public void addTemplateName(String templateName) {
        templateNameList.add(templateName);
        notifyDataSetChanged();
    }

    public String getTemplateNameAt(int index) {
        return templateNameList.get(index);
    }

    public void clearTemplateNames() {
        templateNameList.clear();
        notifyDataSetChanged();
    }

    private class ViewHolder {
        TextView templateName;
    }
}
