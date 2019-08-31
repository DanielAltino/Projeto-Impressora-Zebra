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

import com.zebra.sdk.common.card.printer.discovery.UrlCardPrinterDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;

import java.util.concurrent.CountDownLatch;

public class FindNfcTask extends AsyncTask<Void, Void, Void> {
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private String payload;
    private OnFindNfcListener onFindNfcListener;
    private Exception exception;
    private DiscoveredPrinter printer;

    public interface OnFindNfcListener {
        void onFindNfcStarted();
        void onFindNfcFinished(Exception exception, DiscoveredPrinter printer);
    }

    FindNfcTask(String payload) {
        this.payload = payload;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onFindNfcListener != null) {
            onFindNfcListener.onFindNfcStarted();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            UrlCardPrinterDiscoverer.findPrinters(payload, new DiscoveryHandler() {
                @Override
                public void foundPrinter(DiscoveredPrinter discoveredPrinter) {
                    if (printer == null) {
                        printer = discoveredPrinter;
                    }
                }

                @Override
                public void discoveryFinished() {
                    countDownLatch.countDown();
                }

                @Override
                public void discoveryError(String message) {
                    countDownLatch.countDown();
                }
            });

            countDownLatch.await();
        } catch (Exception e) {
            exception = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        if (onFindNfcListener != null) {
            onFindNfcListener.onFindNfcFinished(exception, printer);
        }
    }

    void setOnFindNfcListener(OnFindNfcListener onFindNfcListener) {
        this.onFindNfcListener = onFindNfcListener;
    }
}
