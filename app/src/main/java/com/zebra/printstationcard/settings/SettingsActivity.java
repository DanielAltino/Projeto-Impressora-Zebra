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

package com.zebra.printstationcard.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import com.zebra.printstationcard.MainApplication;
import com.zebra.printstationcard.R;
import com.zebra.printstationcard.file.DirectoryChooserActivity;
import com.zebra.printstationcard.util.DialogHelper;
import com.zebra.printstationcard.util.PathHelper;
import com.zebra.sdk.common.card.template.ZebraCardTemplate;

import java.io.IOException;

import static com.zebra.printstationcard.file.DirectoryChooserActivity.DirectoryChooserFragment.KEY_CURRENT_DIRECTORY;

public class SettingsActivity extends AppCompatActivity {

    public static final String KEY_TEMPLATE_DIRECTORY = "KEY_TEMPLATE_DIRECTORY";
    public static final String KEY_TEMPLATE_IMAGE_DIRECTORY = "KEY_TEMPLATE_IMAGE_DIRECTORY";
    public static final String TAG_SETTINGS_FRAGMENT = "TAG_SETTINGS_FRAGMENT";
    public static final int REQUEST_SELECT_TEMPLATE_DIRECTORY = 3001;
    public static final int REQUEST_SELECT_TEMPLATE_IMAGE_DIRECTORY = 3002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(TAG_SETTINGS_FRAGMENT);
        if (fragment == null) {
            fm.beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment(), TAG_SETTINGS_FRAGMENT)
                    .commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        private SharedPreferences prefs;
        private ZebraCardTemplate zebraCardTemplate;

        private PreferenceScreen templateDirectory;
        private PreferenceScreen templateImageDirectory;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getActivity().setTitle(getResources().getString(R.string.settings));

            zebraCardTemplate = ((MainApplication) getActivity().getApplication()).getZebraCardTemplate();

            templateDirectory = (PreferenceScreen) findPreference(getString(R.string.template_directory));
            templateImageDirectory = (PreferenceScreen) findPreference(getString(R.string.template_image_directory));

            prefs = getPreferenceManager().getSharedPreferences();
            prefs.registerOnSharedPreferenceChangeListener(this);

            setDirectorySummaryIfExists(templateDirectory, KEY_TEMPLATE_DIRECTORY);
            templateDirectory.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), DirectoryChooserActivity.class);
                    intent.putExtra(KEY_CURRENT_DIRECTORY, prefs.getString(KEY_TEMPLATE_DIRECTORY, null));
                    startActivityForResult(intent, REQUEST_SELECT_TEMPLATE_DIRECTORY);
                    return false;
                }
            });

            setDirectorySummaryIfExists(templateImageDirectory, KEY_TEMPLATE_IMAGE_DIRECTORY);
            templateImageDirectory.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), DirectoryChooserActivity.class);
                    intent.putExtra(KEY_CURRENT_DIRECTORY, prefs.getString(KEY_TEMPLATE_IMAGE_DIRECTORY, null));
                    startActivityForResult(intent, REQUEST_SELECT_TEMPLATE_IMAGE_DIRECTORY);
                    return false;
                }
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case REQUEST_SELECT_TEMPLATE_DIRECTORY:
                    if (resultCode == RESULT_OK) {
                        String directory = data.getStringExtra(DirectoryChooserActivity.KEY_SELECTED_DIRECTORY);
                        prefs.edit().putString(KEY_TEMPLATE_DIRECTORY, directory).apply();
                        try {
                            zebraCardTemplate.setTemplateFileDirectory(directory);
                        } catch (IOException e) {
                            DialogHelper.showErrorDialog(getActivity(), getString(R.string.unable_to_set_template_directory_message, e.getMessage()));
                        }
                    }
                    break;
                case REQUEST_SELECT_TEMPLATE_IMAGE_DIRECTORY:
                    if (resultCode == RESULT_OK) {
                        String directory = data.getStringExtra(DirectoryChooserActivity.KEY_SELECTED_DIRECTORY);
                        prefs.edit().putString(KEY_TEMPLATE_IMAGE_DIRECTORY, directory).apply();
                        try {
                            zebraCardTemplate.setTemplateImageFileDirectory(directory);
                        } catch (IOException e) {
                            DialogHelper.showErrorDialog(getActivity(), getString(R.string.unable_to_set_template_image_directory_message, e.getMessage()));
                        }
                    }
                    break;
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case KEY_TEMPLATE_DIRECTORY:
                    setDirectorySummaryIfExists(templateDirectory, key);
                    break;
                case KEY_TEMPLATE_IMAGE_DIRECTORY:
                    setDirectorySummaryIfExists(templateImageDirectory, key);
                    break;
            }
        }

        private void setDirectorySummaryIfExists(Preference preference, String prefKey) {
            String directory = prefs.getString(prefKey, null);
            if (directory != null && !directory.isEmpty()) {
                preference.setSummary(PathHelper.formatDisplayPath(directory));
            }
        }
    }
}