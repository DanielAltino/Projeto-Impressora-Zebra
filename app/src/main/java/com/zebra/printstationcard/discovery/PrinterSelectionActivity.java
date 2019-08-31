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

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zebra.printstationcard.R;
import com.zebra.printstationcard.util.DialogHelper;
import com.zebra.printstationcard.util.SelectedPrinterManager;
import com.zebra.printstationcard.util.UIHelper;
import com.zebra.sdk.common.card.printer.discovery.DiscoveredCardPrinterNetwork;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;
import com.zebra.sdk.printer.discovery.UsbDiscoverer;
import com.zebra.zebraui.ZebraEditText;

import java.util.HashMap;
import java.util.Map;

public class PrinterSelectionActivity extends AppCompatActivity implements ManualConnectionTask.OnManualConnectionListener,
    NetworkAndUsbDiscoveryTask.OnPrinterDiscoveryListener,
    FindNfcTask.OnFindNfcListener {

    private static final String ACTION_USB_PERMISSION_GRANTED = "com.zebra.developerdemocard.USB_PERMISSION_GRANTED";

    private boolean isApplicationBusy = false;
    private ManualConnectionTask manualConnectionTask;
    private NetworkAndUsbDiscoveryTask networkAndUsbDiscoveryTask;
    private FindNfcTask findNfcTask;

    private Map<DiscoveredPrinter, View> discoveredPrinterMap = new HashMap<>();
    private Animation rotation;
    private LinearLayout recentlySelectedPrintersList;
    private LinearLayout discoveredPrintersList;
    private LinearLayout noPrintersFoundContainer;
    private ImageView refreshPrintersButton;
    private LinearLayout progressOverlay;
    private TextView progressMessage;

    private BroadcastReceiver usbDisconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                DiscoveredPrinterUsb disconnectedPrinter = new DiscoveredPrinterUsb(device.getDeviceName(), getUsbManager(), device);
                String address = disconnectedPrinter.getDiscoveryDataMap().get(ConnectionHelper.KEY_PRINTER_ADDRESS);

                if (UsbDiscoverer.isZebraUsbDevice(device) && address != null) {
                    for (DiscoveredPrinter printer : discoveredPrinterMap.keySet()) {
                        if (address.equals(printer.getDiscoveryDataMap().get(ConnectionHelper.KEY_PRINTER_ADDRESS))) {
                            View view = discoveredPrinterMap.get(printer);
                            ((ViewGroup) view.getParent()).removeView(view);
                        }
                    }
                }
            }
        }
    };

    private BroadcastReceiver usbPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_USB_PERMISSION_GRANTED.equals(intent.getAction())) {
                synchronized (this) {
                    boolean permissionGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null && UsbDiscoverer.isZebraUsbDevice(device)) {
                        if (permissionGranted) {
                            SelectedPrinterManager.setSelectedPrinter(PrinterSelectionActivity.this, new DiscoveredPrinterUsb(device.getDeviceName(), getUsbManager(), device));
                            SelectedPrinterManager.storePrinterHistoryInPreferences(PrinterSelectionActivity.this);
                            finish();
                        } else {
                            UIHelper.showSnackbar(PrinterSelectionActivity.this, getString(R.string.usb_permission_denied));
                        }
                    }

                    isApplicationBusy = false;
                    hideProgressOverlay();
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_printer_selection);
        UIHelper.setLogoOnActionBar(this);
        setTitle(R.string.discover_printers);

        registerReceivers();

        ImageView addPrinterButton = (ImageView) findViewById(R.id.addPrinterButton);
        recentlySelectedPrintersList = (LinearLayout) findViewById(R.id.recentlySelectedPrintersList);
        discoveredPrintersList = (LinearLayout) findViewById(R.id.discoveredPrintersList);
        LinearLayout noRecentlySelectedPrintersContainer = (LinearLayout) findViewById(R.id.noRecentlySelectedPrintersContainer);
        noPrintersFoundContainer = (LinearLayout) findViewById(R.id.noPrintersFoundContainer);
        refreshPrintersButton = (ImageView) findViewById(R.id.refreshPrintersButton);
        progressOverlay = (LinearLayout) findViewById(R.id.progressOverlay);
        progressMessage = (TextView) findViewById(R.id.progressMessage);

        rotation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);

        addPrinterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isApplicationBusy) {
                    DialogHelper.createManuallyConnectDialog(PrinterSelectionActivity.this, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ZebraEditText printerDnsIpAddressInput = (ZebraEditText) ((AlertDialog) dialog).findViewById(R.id.printerDnsIpAddressInput);
                            if (printerDnsIpAddressInput != null) {
                                String ipAddress = printerDnsIpAddressInput.getText();

                                if (manualConnectionTask != null) {
                                    manualConnectionTask.cancel(true);
                                }

                                manualConnectionTask = new ManualConnectionTask(PrinterSelectionActivity.this, ipAddress);
                                manualConnectionTask.setOnManualConnectionListener(PrinterSelectionActivity.this);
                                manualConnectionTask.execute();
                            } else {
                                UIHelper.showSnackbar(PrinterSelectionActivity.this, getString(R.string.unable_to_find_ip_address_input));
                            }
                        }
                    }).show();
                }
            }
        });

        refreshPrintersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoveredPrinterMap.clear();
                discoveredPrintersList.removeAllViews();
                noPrintersFoundContainer.setVisibility(View.VISIBLE);

                startRefreshAnimation();

                if (networkAndUsbDiscoveryTask != null) {
                    networkAndUsbDiscoveryTask.cancel(true);
                }

                networkAndUsbDiscoveryTask = new NetworkAndUsbDiscoveryTask(getUsbManager());
                networkAndUsbDiscoveryTask.setOnPrinterDiscoveryListener(PrinterSelectionActivity.this);
                networkAndUsbDiscoveryTask.execute();
            }
        });

        DiscoveredPrinter[] recentlySelectedPrinters = SelectedPrinterManager.getPrinterHistory();
        if (recentlySelectedPrinters.length > 0) {
            if (noRecentlySelectedPrintersContainer.getVisibility() == View.VISIBLE) {
                noRecentlySelectedPrintersContainer.setVisibility(View.GONE);
            }

            for (DiscoveredPrinter printer : recentlySelectedPrinters) {
                View view = createDiscoveredPrinterItemView(printer);
                if (view != null) {
                    recentlySelectedPrintersList.addView(view);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceivers();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processNfcScan(getIntent());
        }
    }

    @Override
    protected void onDestroy() {
        if (manualConnectionTask != null) {
            manualConnectionTask.cancel(true);
        }

        if (networkAndUsbDiscoveryTask != null) {
            networkAndUsbDiscoveryTask.cancel(true);
        }

        if (findNfcTask != null) {
            findNfcTask.cancel(true);
        }

        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            processNfcScan(intent);
        }
    }

    @Override
    public void onFindNfcStarted() {
        isApplicationBusy = true;
        showProgressOverlay(getString(R.string.processing_nfc_scan));
    }

    private void processNfcScan(Intent intent) {
        Parcelable[] scannedTags = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (scannedTags != null && scannedTags.length > 0) {
            NdefMessage msg = (NdefMessage) scannedTags[0];
            String payload = new String(msg.getRecords()[0].getPayload());

            if (findNfcTask != null) {
                findNfcTask.cancel(true);
            }

            findNfcTask = new FindNfcTask(payload);
            findNfcTask.setOnFindNfcListener(this);
            findNfcTask.execute();

            intent.removeExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        } else {
            DialogHelper.showErrorDialog(this, getString(R.string.error_scanning_nfc_tag));
        }
    }

    @Override
    public void onFindNfcFinished(Exception exception, DiscoveredPrinter printer) {
        isApplicationBusy = false;
        hideProgressOverlay();

        if (exception != null || printer == null) {
            DialogHelper.showErrorDialog(this, getString(R.string.unable_to_find_nfc_printer));
        } else {
            SelectedPrinterManager.setSelectedPrinter(this, printer);
            SelectedPrinterManager.storePrinterHistoryInPreferences(this);
            navigateUp();
        }
    }

    private UsbManager getUsbManager() {
        return (UsbManager) PrinterSelectionActivity.this.getSystemService(Context.USB_SERVICE);
    }

    public void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbDisconnectReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION_GRANTED);
        registerReceiver(usbPermissionReceiver, filter);
    }

    public void unregisterReceivers() {
        unregisterReceiver(usbDisconnectReceiver);
        unregisterReceiver(usbPermissionReceiver);
    }

    public void requestUsbPermission(UsbManager manager, UsbDevice device) {
        showProgressOverlay(getString(R.string.requesting_usb_permission));

        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION_GRANTED), 0);
        manager.requestPermission(device, permissionIntent);
    }

    @Override
    public void onPrinterDiscoveryStarted() {

    }

    @Override
    public void onPrinterDiscovered(final DiscoveredPrinter printer) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (noPrintersFoundContainer.getVisibility() == View.VISIBLE) {
                    noPrintersFoundContainer.setVisibility(View.GONE);
                }

                View view = createDiscoveredPrinterItemView(printer);
                if (view != null) {
                    discoveredPrinterMap.put(printer, view);
                    discoveredPrintersList.addView(view);
                }
            }
        });
    }

    @Override
    public void onPrinterDiscoveryFinished(Exception exception) {
        finishRefreshAnimation();

        if (exception != null) {
            DialogHelper.showErrorDialog(this, getString(R.string.error_discovering_printers_message, exception.getMessage()));
        }
    }

    @Override
    public void onManualConnectionStarted() {
        isApplicationBusy = true;
        showProgressOverlay(getString(R.string.connecting_to_printer));
    }

    @Override
    public void onManualConnectionFinished(Exception exception, DiscoveredPrinter printer) {
        isApplicationBusy = false;
        hideProgressOverlay();

        if (exception != null) {
            DialogHelper.showErrorDialog(this, getString(R.string.error_manually_connecting_to_printer_message, exception.getMessage()));
        } else if (printer != null) {
            SelectedPrinterManager.setSelectedPrinter(this, printer);
            SelectedPrinterManager.storePrinterHistoryInPreferences(this);
            finish();
        }
    }

    private View createDiscoveredPrinterItemView(final DiscoveredPrinter printer) {
        View view = null;
        if (printer != null) {
            view = getLayoutInflater().inflate(R.layout.list_item_discovered_printer, recentlySelectedPrintersList, false);

            String address = printer.getDiscoveryDataMap().get(ConnectionHelper.KEY_PRINTER_ADDRESS);
            String model = printer.getDiscoveryDataMap().get(ConnectionHelper.KEY_PRINTER_MODEL);

            TextView printerModel = (TextView) view.findViewById(R.id.printerModel);
            printerModel.setVisibility(model != null && !model.isEmpty() ? View.VISIBLE : View.GONE);
            printerModel.setText(model);

            TextView printerAddress = (TextView) view.findViewById(R.id.printerAddress);
            printerAddress.setVisibility(address != null && !address.isEmpty() ? View.VISIBLE : View.GONE);
            printerAddress.setText(address);

            ImageView connectionTypeIcon = (ImageView) view.findViewById(R.id.connectionTypeIcon);
            if (printer instanceof DiscoveredPrinterUsb) {
                connectionTypeIcon.setImageResource(R.drawable.ic_usb);
            } else if (printer instanceof DiscoveredPrinterBluetooth) {
                connectionTypeIcon.setImageResource(R.drawable.ic_bluetooth);
            } else if (printer instanceof DiscoveredCardPrinterNetwork) {
                connectionTypeIcon.setImageResource(R.drawable.ic_wifi);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isApplicationBusy) {
                        isApplicationBusy = true;

                        showProgressOverlay(getString(R.string.connecting_to_printer));

                        if (printer instanceof DiscoveredPrinterUsb) {
                            UsbManager usbManager = getUsbManager();
                            UsbDevice device = ((DiscoveredPrinterUsb) printer).device;
                            if (!usbManager.hasPermission(device)) {
                                requestUsbPermission(usbManager, device);
                                return;
                            }
                        }

                        SelectedPrinterManager.setSelectedPrinter(PrinterSelectionActivity.this, printer);
                        SelectedPrinterManager.storePrinterHistoryInPreferences(PrinterSelectionActivity.this);
                        finish();
                    }
                }
            });
        }
        return view;
    }

    private void navigateUp() {
        final Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (NavUtils.shouldUpRecreateTask(this, upIntent) || this.isTaskRoot()) {
            TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
            finish();
        } else {
            NavUtils.navigateUpTo(this, upIntent);
        }
    }

    private void startRefreshAnimation() {
        refreshPrintersButton.setEnabled(false);
        refreshPrintersButton.setAlpha(0.5f);
        refreshPrintersButton.startAnimation(rotation);
    }

    private void finishRefreshAnimation() {
        refreshPrintersButton.setEnabled(true);
        refreshPrintersButton.setAlpha(1.0f);
        refreshPrintersButton.clearAnimation();
    }

    private void showProgressOverlay(String message) {
        progressMessage.setText(message);
        progressOverlay.setVisibility(View.VISIBLE);
    }

    private void hideProgressOverlay() {
        progressMessage.setText(null);
        progressOverlay.setVisibility(View.GONE);
    }
}
