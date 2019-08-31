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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.zebra.printstationcard.discovery.ConnectionHelper;
import com.zebra.sdk.common.card.printer.discovery.DiscoveredCardPrinterNetwork;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;

import java.util.ArrayList;
import java.util.List;

public class SelectedPrinterManager {
	private static final int MAX_HISTORY_SIZE = 5;
	private static final String KEY_IS_PRINTER_SELECTED = "isPrinterSelected";
	private static final String KEY_PRINTER_ADDRESS = "printerAddress";
	private static final String KEY_PRINTER_MODEL = "printerModel";
	private static final String KEY_PRINTER_TYPE = "printerType";
	private static final String PRINTER_TYPE_NETWORK = "Network";
	private static final String PRINTER_TYPE_USB = "Usb";

	private static boolean isPrinterSelected;
	private static List<DiscoveredPrinter> selectedPrinterHistory = new ArrayList<>();

	public static DiscoveredPrinter getSelectedPrinter() {
		if (isPrinterSelected && selectedPrinterHistory.size() > 0) {
			return selectedPrinterHistory.get(0);
		} else {
			return null;
		}
	}

	public static void setSelectedPrinter(Context context, DiscoveredPrinter printer) {
		isPrinterSelected = printer != null;

        Editor editor =  PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(KEY_IS_PRINTER_SELECTED, printer != null);
        editor.apply();

		if (printer != null) {
			for (int i = 0; i < selectedPrinterHistory.size(); i++) {
				if (selectedPrinterHistory.get(i).getDiscoveryDataMap().get(ConnectionHelper.KEY_PRINTER_ADDRESS).equals(printer.getDiscoveryDataMap().get(ConnectionHelper.KEY_PRINTER_ADDRESS))) {
					selectedPrinterHistory.remove(i);
					break;
				}
			}
			selectedPrinterHistory.add(0, printer);

			if (selectedPrinterHistory.size() > MAX_HISTORY_SIZE) {
				selectedPrinterHistory.remove(selectedPrinterHistory.size() - 1);
			}
		}
	}

	public static DiscoveredPrinter[] getPrinterHistory() {
		return selectedPrinterHistory.toArray(new DiscoveredPrinter[] {});
	}

	public static void storePrinterHistoryInPreferences(Context context) {
		Editor editor =  PreferenceManager.getDefaultSharedPreferences(context).edit();
		int storageIndex = 0;
		for (DiscoveredPrinter printer : selectedPrinterHistory) {
			if (printer instanceof DiscoveredCardPrinterNetwork) {
				DiscoveredCardPrinterNetwork networkPrinter = (DiscoveredCardPrinterNetwork) printer;
				String addressToStore = networkPrinter.getDiscoveryDataMap().get(ConnectionHelper.KEY_PRINTER_ADDRESS) + ":" + networkPrinter.getDiscoveryDataMap().get(ConnectionHelper.KEY_PRINTER_PORT_NUMBER);
				editor.putString(KEY_PRINTER_ADDRESS + storageIndex, addressToStore);
				editor.putString(KEY_PRINTER_MODEL + storageIndex, networkPrinter.getDiscoveryDataMap().get(ConnectionHelper.KEY_PRINTER_MODEL));
				editor.putString(KEY_PRINTER_TYPE + storageIndex, PRINTER_TYPE_NETWORK);
				storageIndex++;
			}
		}

		if (getSelectedPrinter() instanceof DiscoveredPrinterUsb) {
		    editor.putBoolean(KEY_IS_PRINTER_SELECTED, false);
        }

		editor.apply();
	}

	public static void populatePrinterHistoryFromPreferences(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		List<DiscoveredCardPrinterNetwork> printers = new ArrayList<>();

		for (int i = 0; preferences.contains(KEY_PRINTER_ADDRESS + i); i++) {
			if (preferences.getString(KEY_PRINTER_TYPE + i, PRINTER_TYPE_USB).equals(PRINTER_TYPE_NETWORK)) {
				String[] addressAndPort = preferences.getString(KEY_PRINTER_ADDRESS + i, "").split(":");
				DiscoveredCardPrinterNetwork printer = new DiscoveredCardPrinterNetwork(addressAndPort[0], Integer.parseInt(addressAndPort[1]));
				printer.getDiscoveryDataMap().put(ConnectionHelper.KEY_PRINTER_MODEL, preferences.getString(KEY_PRINTER_MODEL + i, ""));
				printers.add(printer);
			}
		}

		// Get isPrinterSelected before populating recently selected printer list because setSelectedPrinter() will set isPrinterSelected to true while populating printers
        boolean isPrinterSelected = preferences.getBoolean(KEY_IS_PRINTER_SELECTED, false);

        for (int i = printers.size(); i > 0; i--) {
            setSelectedPrinter(context, printers.get(i - 1));
        }

        if (!isPrinterSelected) {
            setSelectedPrinter(context, null);
        }
	}
}
