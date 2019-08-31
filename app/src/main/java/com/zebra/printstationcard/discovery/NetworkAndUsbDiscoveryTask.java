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

import android.hardware.usb.UsbManager;
import android.os.AsyncTask;

import com.zebra.sdk.common.card.printer.discovery.NetworkCardDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;
import com.zebra.sdk.printer.discovery.DiscoveryException;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;
import com.zebra.sdk.printer.discovery.UsbDiscoverer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class NetworkAndUsbDiscoveryTask extends AsyncTask<Void, Void, Void> {
    private List<Integer> invalidProductIDs = Arrays.asList(0050, 80);
    private UsbManager usbManager;
    private OnPrinterDiscoveryListener onPrinterDiscoveryListener;
    private Exception exception;
    private boolean isUsbDiscoveryComplete;
    private boolean isNetworkDiscoveryComplete;
    private CountDownLatch discoveryCompleteLatch = new CountDownLatch(2);

    public interface OnPrinterDiscoveryListener {
        void onPrinterDiscoveryStarted();
        void onPrinterDiscovered(DiscoveredPrinter printer);
        void onPrinterDiscoveryFinished(Exception exception);
    }

    NetworkAndUsbDiscoveryTask(UsbManager usbManager) {
        this.usbManager = usbManager;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onPrinterDiscoveryListener != null) {
            onPrinterDiscoveryListener.onPrinterDiscoveryStarted();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            UsbDiscoverer.findPrinters(usbManager, new DiscoveryHandler() {
                @Override
                public void foundPrinter(DiscoveredPrinter discoveredPrinter) {
                    if(!invalidProductIDs.contains( ((DiscoveredPrinterUsb) discoveredPrinter).device.getProductId()) ) {
                        if (onPrinterDiscoveryListener != null) {
                            onPrinterDiscoveryListener.onPrinterDiscovered(discoveredPrinter);
                        }
                    }
                }

                @Override
                public void discoveryFinished() {
                    onUsbDiscoveryComplete();
                }

                @Override
                public void discoveryError(String s) {
                    onUsbDiscoveryComplete();
                }
            });

            try {
                NetworkCardDiscoverer.findPrinters(new DiscoveryHandler() {
                    @Override
                    public void foundPrinter(DiscoveredPrinter discoveredPrinter) {
                        if(!discoveredPrinter.getDiscoveryDataMap().get("MODEL").toLowerCase().contains(("zxp1")) && !discoveredPrinter.getDiscoveryDataMap().get("MODEL").toLowerCase().contains("zxp3")) {
                            if (onPrinterDiscoveryListener != null) {
                                onPrinterDiscoveryListener.onPrinterDiscovered(discoveredPrinter);
                            }
                        }
                    }

                    @Override
                    public void discoveryFinished() {
                        onNetworkDiscoveryComplete();
                    }

                    @Override
                    public void discoveryError(String s) {
                        onNetworkDiscoveryComplete();
                    }
                });
            } catch (DiscoveryException e) {
                onNetworkDiscoveryComplete();
            }

            discoveryCompleteLatch.await();
        } catch (Exception e) {
            exception = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        if (onPrinterDiscoveryListener != null) {
            onPrinterDiscoveryListener.onPrinterDiscoveryFinished(exception);
        }
    }

    private void onUsbDiscoveryComplete() {
        if (!isUsbDiscoveryComplete) {
            isUsbDiscoveryComplete = true;
            discoveryCompleteLatch.countDown();
        }
    }

    private void onNetworkDiscoveryComplete() {
        if (!isNetworkDiscoveryComplete) {
            isNetworkDiscoveryComplete = true;
            discoveryCompleteLatch.countDown();
        }
    }

    void setOnPrinterDiscoveryListener(OnPrinterDiscoveryListener onPrinterDiscoveryListener) {
        this.onPrinterDiscoveryListener = onPrinterDiscoveryListener;
    }
}