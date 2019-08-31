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

package com.zebra.printstationcard;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zebra.printstationcard.discovery.ConnectionHelper;
import com.zebra.printstationcard.discovery.PrinterSelectionActivity;
import com.zebra.printstationcard.discovery.PrinterStatusUpdateTask;
import com.zebra.printstationcard.file.FileHelper;
import com.zebra.printstationcard.settings.AboutDialogActivity;
import com.zebra.printstationcard.settings.SettingsActivity;
import com.zebra.printstationcard.templates.SelectedTemplateJobActivity;
import com.zebra.printstationcard.templates.TemplateListAdapter;
import com.zebra.printstationcard.util.DialogHelper;
import com.zebra.printstationcard.util.PreferenceHelper;
import com.zebra.printstationcard.util.SelectedPrinterManager;
import com.zebra.printstationcard.util.UIHelper;
import com.zebra.sdk.common.card.template.ZebraCardTemplate;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;
import com.zebra.sdk.printer.discovery.UsbDiscoverer;
import com.zebra.zebraui.ZebraPrinterView;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.zebra.printstationcard.settings.SettingsActivity.KEY_TEMPLATE_DIRECTORY;
import static com.zebra.printstationcard.settings.SettingsActivity.KEY_TEMPLATE_IMAGE_DIRECTORY;
import static com.zebra.printstationcard.templates.SelectedTemplateJobActivity.KEY_SELECTED_TEMPLATE_NAME;

public class MainActivity extends AppCompatActivity implements PrinterStatusUpdateTask.OnUpdatePrinterStatusListener {

    public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1001;

    private ZebraCardTemplate zebraCardTemplate;
    private TemplateListAdapter templateListAdapter;
    private PrinterStatusUpdateTask printerStatusUpdateTask;

    private FrameLayout noPrinterSelectedContainer;
    private LinearLayout printerSelectedContainer;
    private ZebraPrinterView printerView;
    private TextView printerAddress;
    private TextView printerModel;

    private BroadcastReceiver usbDisconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                DiscoveredPrinter printer = SelectedPrinterManager.getSelectedPrinter();

                if (printer instanceof DiscoveredPrinterUsb
                        && UsbDiscoverer.isZebraUsbDevice(device)
                        && ((DiscoveredPrinterUsb) printer).device.equals(device)) {
                    SelectedPrinterManager.setSelectedPrinter(MainActivity.this, null);
                    refreshSelectedPrinterView();
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        UIHelper.setLogoOnActionBar(this);

        registerReceivers();

        noPrinterSelectedContainer = (FrameLayout) findViewById(R.id.noPrinterSelectedContainer);
        printerSelectedContainer = (LinearLayout) findViewById(R.id.printerSelectedContainer);
        printerView = (ZebraPrinterView) findViewById(R.id.printerView);
        printerAddress = (TextView) findViewById(R.id.printerAddress);
        printerModel = (TextView) findViewById(R.id.printerModel);
        templateListAdapter = new TemplateListAdapter(this, R.layout.list_item_template, new ArrayList<String>());

        ListView templateList = (ListView) findViewById(R.id.templateList);
        templateList.setAdapter(templateListAdapter);
        templateList.setEmptyView(findViewById(R.id.noTemplatesFoundContainer));
        templateList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String templateName = templateListAdapter.getTemplateNameAt(position);
                Intent intent = new Intent(MainActivity.this, SelectedTemplateJobActivity.class);
                intent.putExtra(KEY_SELECTED_TEMPLATE_NAME, templateName);
                startActivity(intent);
            }
        });

        printerSelectedContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptDisconnectPrinter();
            }
        });

        noPrinterSelectedContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PrinterSelectionActivity.class));
            }
        });

        setUpTemplateDirectories();

        refreshSelectedPrinterView();
    }

    @Override
    protected void onDestroy() {
        if (printerStatusUpdateTask != null) {
            printerStatusUpdateTask.cancel(true);
        }

        super.onDestroy();
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
        refreshSelectedPrinterView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem disconnectPrinterMenuItem = menu.findItem(R.id.disconnectPrinter);
        disconnectPrinterMenuItem.setVisible(SelectedPrinterManager.getSelectedPrinter() != null);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.disconnectPrinter:
                promptDisconnectPrinter();
                return true;
            case R.id.discoverPrinters:
                startActivity(new Intent(this, PrinterSelectionActivity.class));
                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.about:
                startActivity(new Intent(this, AboutDialogActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                refreshTemplates();
                UIHelper.showSnackbar(this, getString(R.string.storage_permissions_granted));
            } else if (Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(permissions[0])) {
                DialogHelper.showStoragePermissionDeniedDialog(this, getString(R.string.storage_permissions_request_enable_message));
            } else {
                DialogHelper.showStoragePermissionDeniedDialog(this, getString(R.string.storage_permissions_denied));
            }
        }
    }

    @Override
    public void onUpdatePrinterStatus(ZebraPrinterView.PrinterStatus printerStatus) {
        printerView.setPrinterStatus(printerStatus);
    }

    private void refreshTemplates() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        File storageDirectory = new File(prefs.getString(KEY_TEMPLATE_DIRECTORY, PreferenceHelper.getDefaultTemplateDirectoryPath()));

        String state = Environment.getExternalStorageState();
        if (!(state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) || !storageDirectory.exists()) {
            return;
        }

        File[] savedTemplateFiles = storageDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.toLowerCase().endsWith(".xml");
            }
        });

        if (savedTemplateFiles != null) {
            for (File templateFile : savedTemplateFiles) {
                String templateName = FilenameUtils.removeExtension(templateFile.getName());

                try {
                    String templateContents = FileHelper.readFileToString(templateFile);

                    List<String> storedTemplateNames = zebraCardTemplate.getAllTemplateNames();
                    if (storedTemplateNames.contains(templateName)) {
                        zebraCardTemplate.deleteTemplate(templateName);
                    }

                    zebraCardTemplate.saveTemplate(templateName, templateContents);

                    templateListAdapter.addTemplateName(templateName);
                } catch (Exception e) {
                    DialogHelper.showErrorDialog(this, getString(R.string.error_saving_template_message, templateName, e.getMessage()));
                }
            }
        }
    }

    private void setUpTemplateDirectories() {
        zebraCardTemplate = ((MainApplication) getApplication()).getZebraCardTemplate();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String templateDirectory = prefs.getString(KEY_TEMPLATE_DIRECTORY, null);
        if (templateDirectory == null) {
            templateDirectory = PreferenceHelper.getDefaultTemplateDirectoryPath();
            prefs.edit().putString(KEY_TEMPLATE_DIRECTORY, templateDirectory).apply();
        }

        String templateImageDirectory = prefs.getString(KEY_TEMPLATE_IMAGE_DIRECTORY, null);
        if (templateImageDirectory == null) {
            templateImageDirectory = PreferenceHelper.getDefaultTemplateImageDirectoryPath();
            prefs.edit().putString(KEY_TEMPLATE_IMAGE_DIRECTORY, templateImageDirectory).apply();
        }

        SelectedPrinterManager.populatePrinterHistoryFromPreferences(this);

        try {
            zebraCardTemplate.setTemplateFileDirectory(templateDirectory);
        } catch (IOException e) {
            DialogHelper.showErrorDialog(this, getString(R.string.unable_to_set_template_directory_message, e.getMessage()));
        }

        try {
            zebraCardTemplate.setTemplateImageFileDirectory(templateImageDirectory);
        } catch (IOException e) {
            DialogHelper.showErrorDialog(this, getString(R.string.unable_to_set_template_image_directory_message, e.getMessage()));
        }
    }

    private void promptDisconnectPrinter() {
        DialogHelper.createDisconnectDialog(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SelectedPrinterManager.setSelectedPrinter(MainActivity.this, null);
                refreshSelectedPrinterView();
            }
        }).show();
    }

    private void refreshSelectedPrinterView() {
        templateListAdapter.clearTemplateNames();

        DiscoveredPrinter printer = SelectedPrinterManager.getSelectedPrinter();
        boolean isPrinterSelected = printer != null;
        if (isPrinterSelected) {
            String address = printer.getDiscoveryDataMap().get(ConnectionHelper.KEY_PRINTER_ADDRESS);
            String model = printer.getDiscoveryDataMap().get(ConnectionHelper.KEY_PRINTER_MODEL);
            printerAddress.setVisibility(address != null && !address.isEmpty() ? View.VISIBLE : View.GONE);
            printerAddress.setText(address);
            printerModel.setVisibility(model != null && !model.isEmpty() ? View.VISIBLE : View.GONE);
            printerModel.setText(model);
            printerView.setPrinterModel(model);

            refreshTemplates();

            if (printerStatusUpdateTask != null) {
                printerStatusUpdateTask.cancel(true);
            }

            printerStatusUpdateTask = new PrinterStatusUpdateTask(printer);
            printerStatusUpdateTask.setOnUpdatePrinterStatusListener(this);
            printerStatusUpdateTask.execute();
        }

        printerSelectedContainer.setVisibility(isPrinterSelected ? View.VISIBLE : View.GONE);
        noPrinterSelectedContainer.setVisibility(isPrinterSelected ? View.GONE : View.VISIBLE);
        invalidateOptionsMenu();
    }

    public void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbDisconnectReceiver, filter);
    }

    public void unregisterReceivers() {
        unregisterReceiver(usbDisconnectReceiver);
    }
}
