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

import android.os.AsyncTask;

import com.zebra.printstationcard.discovery.ConnectionHelper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.common.card.template.ZebraCardTemplate;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.util.ArrayList;
import java.util.List;

public class GetTemplateVariablesTask extends AsyncTask<Void, Void, Void> {

    private ZebraCardTemplate zebraCardTemplate;
    private DiscoveredPrinter printer;
    private List<String> templateVariables = new ArrayList<>();
    private String templateName;
    private OnGetTemplateVariablesListener onGetTemplateVariablesListener;
    private Exception exception;

    public interface OnGetTemplateVariablesListener {
        void onGetTemplateVariablesStarted();
        void onGetTemplateVariablesFinished(Exception exception, List<String> templateVariables);
    }

    GetTemplateVariablesTask(ZebraCardTemplate zebraCardTemplate, DiscoveredPrinter printer, String templateName) {
        this.zebraCardTemplate = zebraCardTemplate;
        this.printer = printer;
        this.templateName = templateName;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onGetTemplateVariablesListener != null) {
            onGetTemplateVariablesListener.onGetTemplateVariablesStarted();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        Connection connection = null;
        ZebraCardPrinter zebraCardPrinter = null;

        try {
            connection = printer.getConnection();
            connection.open();

            zebraCardPrinter = ZebraCardPrinterFactory.getInstance(connection);

            templateVariables.addAll(zebraCardTemplate.getTemplateFields(templateName));
        } catch (Exception e) {
            exception = e;
        } finally {
            ConnectionHelper.cleanUpQuietly(zebraCardPrinter, connection);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (onGetTemplateVariablesListener != null) {
            onGetTemplateVariablesListener.onGetTemplateVariablesFinished(exception, templateVariables);
        }
    }

    void setOnGetTemplateVariablesListener(OnGetTemplateVariablesListener onGetTemplateVariablesListener) {
        this.onGetTemplateVariablesListener = onGetTemplateVariablesListener;
    }
}
