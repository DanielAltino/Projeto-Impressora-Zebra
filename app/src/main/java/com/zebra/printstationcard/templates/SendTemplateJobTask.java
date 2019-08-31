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
import com.zebra.sdk.common.card.containers.TemplateJob;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.common.card.template.ZebraCardTemplate;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.util.Map;

public class SendTemplateJobTask extends AsyncTask<Void, Void, Void> {

    private ZebraCardTemplate zebraCardTemplate;
    private DiscoveredPrinter printer;
    private int jobId;
    private String templateName;
    private Map<String, String> variablesData;
    private int quantity;
    private OnSendTemplateJobListener onSendTemplateJobListener;
    private Exception exception;

    public interface OnSendTemplateJobListener {
        void onSendTemplateJobStarted();
        void onSendTemplateJobFinished(Exception exception, int jobId, String templateName, Map<String, String> variablesData, int quantity);
    }

    SendTemplateJobTask(ZebraCardTemplate zebraCardTemplate, DiscoveredPrinter printer, String templateName, Map<String, String> variablesData, int quantity) {
        this.zebraCardTemplate = zebraCardTemplate;
        this.printer = printer;
        this.templateName = templateName;
        this.variablesData = variablesData;
        this.quantity = quantity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onSendTemplateJobListener != null) {
            onSendTemplateJobListener.onSendTemplateJobStarted();
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

            TemplateJob templateJob = zebraCardTemplate.generateTemplateJob(templateName, variablesData);
            jobId = zebraCardPrinter.printTemplate(quantity, templateJob);
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

        if (onSendTemplateJobListener != null) {
            onSendTemplateJobListener.onSendTemplateJobFinished(exception, jobId, templateName, variablesData, quantity);
        }
    }

    void setOnSendTemplateJobListener(OnSendTemplateJobListener onSendTemplateJobListener) {
        this.onSendTemplateJobListener = onSendTemplateJobListener;
    }
}
