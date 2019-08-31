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

import android.content.Context;
import android.os.AsyncTask;

import com.zebra.printstationcard.discovery.ConnectionHelper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.common.card.containers.JobStatusInfo;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class PollJobStatusTask extends AsyncTask<Void, Void, Void> {

    private static final int CARD_FEED_TIMEOUT = 30000;

    private WeakReference<Context> weakContext;
    private DiscoveredPrinter printer;
    private int jobId;
    private String templateName;
    private Map<String, String> variablesData;
    private int quantity;
    private JobStatusInfo jobStatusInfo;
    private OnJobStatusPollListener onJobStatusPollListener;
    private Exception exception;
    private boolean cancelledByUser = false;
    private String doneMessage;

    public interface OnJobStatusPollListener {
        void onJobStatusPollStarted();
        void onJobStatusUserInputRequired(int jobId, String alarmInfoDescription, PollJobStatusTask.OnUserInputListener onUserInputListener);
        void onJobStatusPollFinished(Exception exception, String templateName, Map<String, String> variablesData, int quantity, JobStatusInfo jobStatusInfo, String doneMessage);
    }

    public interface OnUserInputListener {
        void onPositiveButtonClicked();
        void onNegativeButtonClicked();
    }

    public PollJobStatusTask(Context context, DiscoveredPrinter printer, int jobId, String templateName, Map<String, String> variablesData, int quantity) {
        weakContext = new WeakReference<>(context);
        this.printer = printer;
        this.jobId = jobId;
        this.templateName = templateName;
        this.variablesData = variablesData;
        this.quantity = quantity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onJobStatusPollListener != null) {
            onJobStatusPollListener.onJobStatusPollStarted();
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Connection connection = null;
        ZebraCardPrinter zebraCardPrinter = null;
        boolean isFeeding = false;

        try {
            connection = printer.getConnection();
            connection.open();
            zebraCardPrinter = ZebraCardPrinterFactory.getInstance(connection);
            long startTime = System.currentTimeMillis();

            while (true) {
                if (isCancelled()) {
                    break;
                }

                jobStatusInfo = zebraCardPrinter.getJobStatus(jobId);

                if (!isFeeding) {
                    startTime = System.currentTimeMillis();
                }

                boolean isAlarmInfoPresent = jobStatusInfo.alarmInfo.value > 0;
                boolean isErrorInfoPresent = jobStatusInfo.errorInfo.value > 0;
                isFeeding = jobStatusInfo.cardPosition.contains("feeding");

                if (jobStatusInfo.printStatus.contains("done_ok")) {
                    doneMessage = weakContext.get().getString(R.string.print_job_finished_successfully);
                    break;
                } else if (jobStatusInfo.printStatus.equals("done_error")) {
                    if (onJobStatusPollListener != null) {
                        doneMessage = weakContext.get().getString(R.string.error_printer_template_message, jobId, jobStatusInfo.errorInfo.description);
                    }
                    break;
                } else if (jobStatusInfo.printStatus.contains("cancelled")) {
                    if (isErrorInfoPresent) {
                        doneMessage = weakContext.get().getString(R.string.print_job_cancelled_with_error_message, jobStatusInfo.errorInfo.description);
                    } else {
                        doneMessage = weakContext.get().getString(R.string.print_job_cancelled);
                    }
                    break;
                } else if (isAlarmInfoPresent) {
                    if (onJobStatusPollListener != null) {
                        final CountDownLatch alarmEncounteredLatch = new CountDownLatch(1);
                        onJobStatusPollListener.onJobStatusUserInputRequired(jobId, jobStatusInfo.alarmInfo.description, new OnUserInputListener() {
                            @Override
                            public void onPositiveButtonClicked() {
                                cancelledByUser = false;
                                alarmEncounteredLatch.countDown();
                            }

                            @Override
                            public void onNegativeButtonClicked() {
                                cancelledByUser = true;
                                alarmEncounteredLatch.countDown();
                            }
                        });
                        alarmEncounteredLatch.await();
                    }

                    if (cancelledByUser) {
                        cancelledByUser = false;
                        zebraCardPrinter.cancel(jobId);
                    }
                } else if (isErrorInfoPresent) {
                    zebraCardPrinter.cancel(jobId);
                } else if (isFeeding) {
                    if (System.currentTimeMillis() > startTime + CARD_FEED_TIMEOUT) {
                        zebraCardPrinter.cancel(jobId);
                    }
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // Do nothing
                }
            }
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

        if (onJobStatusPollListener != null) {
            onJobStatusPollListener.onJobStatusPollFinished(exception, templateName, variablesData, quantity, jobStatusInfo, doneMessage);
        }
    }

    public void setOnJobStatusPollListener(OnJobStatusPollListener onJobStatusPollListener) {
        this.onJobStatusPollListener = onJobStatusPollListener;
    }
}