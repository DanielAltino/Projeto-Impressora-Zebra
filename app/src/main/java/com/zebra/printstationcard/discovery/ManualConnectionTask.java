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

import android.content.Context;
import android.os.AsyncTask;

import com.zebra.printstationcard.R;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.common.card.printer.discovery.DiscoveredCardPrinterNetwork;
import com.zebra.sdk.common.card.printer.discovery.DiscoveryUtilCard;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.settings.SettingsException;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class ManualConnectionTask extends AsyncTask<Void, Void, Void> {

    private WeakReference<Context> weakContext;
    private DiscoveredPrinter printer;
    private String ipAddress;
    private OnManualConnectionListener onManualConnectionListener;
    private Exception exception;

    public interface OnManualConnectionListener {
        void onManualConnectionStarted();
        void onManualConnectionFinished(Exception exception, DiscoveredPrinter printer);
    }

    ManualConnectionTask(Context context, String ipAddress) {
        weakContext = new WeakReference<>(context);
        this.ipAddress = ipAddress;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onManualConnectionListener != null) {
            onManualConnectionListener.onManualConnectionStarted();
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        TcpConnection connection = null;

        try {
            connection = getTcpConnection(ipAddress);
            connection.open();

            Map<String, String> discoveryDataMap = DiscoveryUtilCard.getDiscoveryDataMap(connection);

            String model = discoveryDataMap.get("MODEL");
            if (model != null) {
                if(!model.toLowerCase().contains("zxp1") && !model.toLowerCase().contains("zxp3")) {
                    printer = new DiscoveredCardPrinterNetwork(discoveryDataMap);
                } else {
                    throw new ConnectionException(weakContext.get().getString(R.string.printer_model_not_supported));
                }
            } else {
                throw new SettingsException(weakContext.get().getString(R.string.no_printer_model_found));
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            ConnectionHelper.cleanUpQuietly(null, connection);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (onManualConnectionListener != null) {
            onManualConnectionListener.onManualConnectionFinished(exception, printer);
        }
    }

    private TcpConnection getTcpConnection(String connectionText) {
        int colonIndex = connectionText.indexOf(":");
        if (colonIndex != -1) {
            String ipAddress = connectionText.substring(0, colonIndex);
            int portNumber = Integer.parseInt(connectionText.substring(colonIndex + 1));
            return new TcpConnection(ipAddress, portNumber);
        } else {
            return new TcpConnection(connectionText, 9100);
        }
    }

    void setOnManualConnectionListener(OnManualConnectionListener onManualConnectionListener) {
        this.onManualConnectionListener = onManualConnectionListener;
    }
}