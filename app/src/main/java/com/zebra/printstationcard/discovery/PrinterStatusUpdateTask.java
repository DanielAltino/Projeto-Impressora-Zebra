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

package com.zebra.printstationcard.discovery;

import android.os.AsyncTask;

import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.common.card.containers.PrinterStatusInfo;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.zebraui.ZebraPrinterView;

public class PrinterStatusUpdateTask extends AsyncTask<Void, Void, ZebraPrinterView.PrinterStatus> {

    private DiscoveredPrinter printer;

    private OnUpdatePrinterStatusListener onUpdatePrinterStatusListener;

    public interface OnUpdatePrinterStatusListener {
        void onUpdatePrinterStatus(ZebraPrinterView.PrinterStatus printerStatus);
    }

    public PrinterStatusUpdateTask(DiscoveredPrinter printer) {
        this.printer = printer;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onUpdatePrinterStatusListener != null) {
            onUpdatePrinterStatusListener.onUpdatePrinterStatus(ZebraPrinterView.PrinterStatus.REFRESHING);
        }
    }

    @Override
    protected ZebraPrinterView.PrinterStatus doInBackground(Void... params) {
        Connection connection = printer.getConnection();
        ZebraCardPrinter printer = null;

        try {
            connection.open();

            printer = ZebraCardPrinterFactory.getInstance(connection);
            PrinterStatusInfo printerStatus = printer.getPrinterStatus();
            if (printerStatus != null) {
                if (printerStatus.errorInfo.value != 0 || printerStatus.alarmInfo.value != 0) {
                    return ZebraPrinterView.PrinterStatus.ERROR;
                } else {
                    return ZebraPrinterView.PrinterStatus.ONLINE;
                }
            } else {
                return ZebraPrinterView.PrinterStatus.ERROR;
            }
        } catch (Exception e) {
            return ZebraPrinterView.PrinterStatus.ERROR;
        } finally {
            ConnectionHelper.cleanUpQuietly(printer, connection);
        }
    }

    @Override
    protected void onPostExecute(ZebraPrinterView.PrinterStatus printerStatus) {
        super.onPostExecute(printerStatus);

        if (onUpdatePrinterStatusListener != null) {
            onUpdatePrinterStatusListener.onUpdatePrinterStatus(printerStatus);
        }
    }

    public void setOnUpdatePrinterStatusListener(OnUpdatePrinterStatusListener onUpdatePrinterStatusListener) {
        this.onUpdatePrinterStatusListener = onUpdatePrinterStatusListener;
    }
}