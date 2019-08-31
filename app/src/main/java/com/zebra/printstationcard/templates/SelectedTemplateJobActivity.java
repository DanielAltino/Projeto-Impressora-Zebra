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

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zebra.printstationcard.MainApplication;
import com.zebra.printstationcard.PollJobStatusTask;
import com.zebra.printstationcard.R;
import com.zebra.printstationcard.util.DialogHelper;
import com.zebra.printstationcard.util.SelectedPrinterManager;
import com.zebra.printstationcard.util.UIHelper;
import com.zebra.sdk.common.card.containers.JobStatusInfo;
import com.zebra.sdk.common.card.template.ZebraCardTemplate;
import com.zebra.zebraui.ZebraButton;
import com.zebra.zebraui.ZebraEditText;
import com.zebra.zebraui.ZebraSpinnerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectedTemplateJobActivity extends AppCompatActivity implements GetTemplateVariablesTask.OnGetTemplateVariablesListener,
        SendTemplateJobTask.OnSendTemplateJobListener,
        PollJobStatusTask.OnJobStatusPollListener {

    public static final String KEY_SELECTED_TEMPLATE_NAME = "KEY_SELECTED_TEMPLATE_NAME";

    private ZebraCardTemplate zebraCardTemplate;
    private boolean isApplicationBusy = false;
    private GetTemplateVariablesTask getTemplateVariablesTask;
    private SendTemplateJobTask sendTemplateJobTask;
    private PollJobStatusTask pollJobStatusTask;

    private String templateName;
    private Map<String, ZebraEditText> variablesData = new HashMap<>();

    private LinearLayout templateVariableList;
    private ZebraSpinnerView quantitySpinner;
    private ZebraButton printButton;
    private LinearLayout progressOverlay;
    private TextView progressMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_selected_template);

        zebraCardTemplate = ((MainApplication) getApplication()).getZebraCardTemplate();

        templateName = getIntent().getStringExtra(KEY_SELECTED_TEMPLATE_NAME);

        TextView selectedTemplateName = (TextView) findViewById(R.id.selectedTemplateName);
        templateVariableList = (LinearLayout) findViewById(R.id.templateVariableList);
        quantitySpinner = (ZebraSpinnerView) findViewById(R.id.quantitySpinner);
        printButton = (ZebraButton) findViewById(R.id.printButton);
        ZebraButton cancelButton = (ZebraButton) findViewById(R.id.cancelButton);
        progressOverlay = (LinearLayout) findViewById(R.id.progressOverlay);
        progressMessage = (TextView) findViewById(R.id.progressMessage);

        selectedTemplateName.setText(templateName);

        getTemplateVariablesTask = new GetTemplateVariablesTask(zebraCardTemplate, SelectedPrinterManager.getSelectedPrinter(), templateName);
        getTemplateVariablesTask.setOnGetTemplateVariablesListener(this);
        getTemplateVariablesTask.execute();

        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isApplicationBusy) {
                    isApplicationBusy = true;

                    printButton.setEnabled(false);
                    UIHelper.hideSoftKeyboard(SelectedTemplateJobActivity.this);

                    Map<String, String> varsData = new HashMap<>();
                    for (String variable : variablesData.keySet()) {
                        varsData.put(variable, variablesData.get(variable).getText());
                    }

                    int quantity = Integer.parseInt(quantitySpinner.getSelectedItem().toString());

                    if (sendTemplateJobTask != null) {
                        sendTemplateJobTask.cancel(true);
                    }

                    sendTemplateJobTask = new SendTemplateJobTask(zebraCardTemplate, SelectedPrinterManager.getSelectedPrinter(), templateName, varsData, quantity);
                    sendTemplateJobTask.setOnSendTemplateJobListener(SelectedTemplateJobActivity.this);
                    sendTemplateJobTask.execute();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (getTemplateVariablesTask != null) {
            getTemplateVariablesTask.cancel(true);
        }

        if (sendTemplateJobTask != null) {
            sendTemplateJobTask.cancel(true);
        }

        if (pollJobStatusTask != null) {
            pollJobStatusTask.cancel(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (!isApplicationBusy) {
            super.onBackPressed();
        }
    }

    @Override
    public void onGetTemplateVariablesStarted() {
        isApplicationBusy = true;
        showProgressOverlay(getString(R.string.retrieving_template_variables));
        templateVariableList.removeAllViews();
    }

    @Override
    public void onGetTemplateVariablesFinished(Exception exception, List<String> templateVariables) {
        isApplicationBusy = false;
        hideProgressOverlay();

        if (exception != null) {
            printButton.setEnabled(false);
            DialogHelper.showErrorDialog(this, getString(R.string.unable_to_retrieve_template_variables_message, exception.getMessage()));
        } else if (templateVariables == null) {
            DialogHelper.showErrorDialog(this, getString(R.string.no_template_variables_found));
        } else {
            for (String variableName : templateVariables) {
                ZebraEditText zebraEditText = createTemplateVariableView(variableName);
                templateVariableList.addView(zebraEditText);
                variablesData.put(variableName, zebraEditText);
            }
        }
    }

    @Override
    public void onSendTemplateJobStarted() {
        showProgressOverlay(getString(R.string.sending_template_job_to_printer));
    }

    @Override
    public void onSendTemplateJobFinished(Exception exception, int jobId, final String templateName, final Map<String, String> variablesData, final int quantity) {
        if (exception == null) {
            if (pollJobStatusTask != null) {
                pollJobStatusTask.cancel(true);
            }

            pollJobStatusTask = new PollJobStatusTask(this, SelectedPrinterManager.getSelectedPrinter(), jobId, templateName, variablesData, quantity);
            pollJobStatusTask.setOnJobStatusPollListener(this);
            pollJobStatusTask.execute();
        } else {
            isApplicationBusy = false;
            printButton.setEnabled(true);
            hideProgressOverlay();

            showRetryTemplateJobDialog(exception.getMessage(), templateName, variablesData, quantity);
        }
    }

    @Override
    public void onJobStatusPollStarted() {
        showProgressOverlay(getString(R.string.printing_template));
    }

    @Override
    public void onJobStatusUserInputRequired(int jobId, String alarmInfoDescription, final PollJobStatusTask.OnUserInputListener onUserInputListener) {
        DialogHelper.showAlarmEncounteredDialog(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onUserInputListener != null) {
                    onUserInputListener.onPositiveButtonClicked();
                }
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onUserInputListener != null) {
                    onUserInputListener.onNegativeButtonClicked();
                }
            }
        }, jobId, alarmInfoDescription);
    }

    @Override
    public void onJobStatusPollFinished(Exception exception, final String templateName, final Map<String, String> variablesData, final int quantity, JobStatusInfo jobStatusInfo, String doneMessage) {
        isApplicationBusy = false;
        printButton.setEnabled(true);
        hideProgressOverlay();

        if (exception == null) {
            if (jobStatusInfo != null && jobStatusInfo.errorInfo.value > 0) {
                showRetryTemplateJobDialog(doneMessage, templateName, variablesData, quantity);
            } else {
                UIHelper.showSnackbar(this, doneMessage);
            }
        } else {
            showRetryTemplateJobDialog(exception.getMessage(), templateName, variablesData, quantity);
        }
    }

    private void showRetryTemplateJobDialog(String message, final String templateName, final Map<String, String> variablesData, final int quantity) {
        DialogHelper.createPrintingErrorDialog(this, message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (sendTemplateJobTask != null) {
                    sendTemplateJobTask.cancel(true);
                }

                sendTemplateJobTask = new SendTemplateJobTask(zebraCardTemplate, SelectedPrinterManager.getSelectedPrinter(), templateName, variablesData, quantity);
                sendTemplateJobTask.setOnSendTemplateJobListener(SelectedTemplateJobActivity.this);
                sendTemplateJobTask.execute();

                dialog.dismiss();
            }
        }).show();
    }

    private ZebraEditText createTemplateVariableView(String headerText) {
        ZebraEditText zebraEditText = (ZebraEditText) LayoutInflater.from(this).inflate(R.layout.item_template_variable, templateVariableList, false);
        zebraEditText.setHeaderText(headerText);
        return zebraEditText;
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
