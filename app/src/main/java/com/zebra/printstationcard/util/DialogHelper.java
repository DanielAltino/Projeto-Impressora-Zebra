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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import com.zebra.printstationcard.MainActivity;
import com.zebra.printstationcard.R;

public class DialogHelper {
    public static void showAlarmEncounteredDialog(final Activity activity,
                                                  final DialogInterface.OnClickListener onPositiveButtonClickListener,
                                                  final DialogInterface.OnClickListener onNegativeButtonClickListener,
                                                  final int jobId,
                                                  final String alarmInfoDescription) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String positiveButtonText = activity.getString(android.R.string.ok);
                String negativeButtonText = activity.getString(android.R.string.cancel);

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                AlertDialog dialog = builder.setTitle(R.string.alarm_encountered)
                        .setMessage(activity.getString(R.string.alarm_encountered_message, jobId, alarmInfoDescription, positiveButtonText, negativeButtonText))
                        .setPositiveButton(positiveButtonText, onPositiveButtonClickListener)
                        .setNegativeButton(negativeButtonText, onNegativeButtonClickListener)
                        .setCancelable(false)
                        .create();
                dialog.show();
            }
        });
    }

    public static AlertDialog.Builder createManuallyConnectDialog(Context context, DialogInterface.OnClickListener onPositiveButtonClickListener) {
        return new AlertDialog.Builder(context).setTitle(R.string.dialog_title_manually_connect)
                .setView(R.layout.dialog_manually_connect)
                .setPositiveButton(R.string.connect, onPositiveButtonClickListener)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    public static AlertDialog.Builder createDisconnectDialog(Context context, DialogInterface.OnClickListener onPositiveButtonClickListener) {
        return new AlertDialog.Builder(context).setTitle(R.string.dialog_title_disconnect_printer)
                .setMessage(R.string.dialog_message_disconnect_printer)
                .setPositiveButton(R.string.disconnect, onPositiveButtonClickListener)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    public static AlertDialog.Builder createPrintingErrorDialog(Context context, String errorMessage, DialogInterface.OnClickListener onPositiveButtonClickListener) {
        return new AlertDialog.Builder(context).setTitle(R.string.printing_error)
                .setMessage(context.getString(R.string.dialog_message_printing_error) + "\n" + errorMessage)
                .setPositiveButton(R.string.retry, onPositiveButtonClickListener)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);
    }

    public static void showErrorDialog(final Activity activity, final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                AlertDialog dialog = builder.setTitle(R.string.error)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .create();
                dialog.show();
            }
        });
    }

    public static void showStoragePermissionDeniedDialog(final Activity activity, final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                AlertDialog dialog = builder.setTitle(R.string.error)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, MainActivity.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
                            }
                        })
                        .setCancelable(false)
                        .create();
                dialog.show();
            }
        });
    }
}
