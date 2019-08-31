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

package com.zebra.printstationcard.file;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zebra.printstationcard.R;
import com.zebra.printstationcard.util.PathHelper;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.zebra.printstationcard.file.DirectoryChooserActivity.DirectoryChooserFragment.KEY_CURRENT_DIRECTORY;

public class DirectoryChooserActivity extends AppCompatActivity {

    public static final String KEY_SELECTED_DIRECTORY = "KEY_SELECTED_DIRECTORY";

    private static final String TAG_DIRECTORY_CHOOSER_FRAGMENT = "directoryChooser";

    private DirectoryChooserFragment directoryChooserFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.select_directory);

        setContentView(R.layout.activity_directory_chooser);

        if (savedInstanceState != null) {
            directoryChooserFragment = (DirectoryChooserFragment) getSupportFragmentManager().getFragment(savedInstanceState, TAG_DIRECTORY_CHOOSER_FRAGMENT);
        }

        if (directoryChooserFragment == null) {
            directoryChooserFragment = new DirectoryChooserFragment();
            Bundle bundle = new Bundle();
            bundle.putString(KEY_CURRENT_DIRECTORY, getIntent().getStringExtra(KEY_CURRENT_DIRECTORY));
            directoryChooserFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentFileChooser, directoryChooserFragment, TAG_DIRECTORY_CHOOSER_FRAGMENT)
                    .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, TAG_DIRECTORY_CHOOSER_FRAGMENT, directoryChooserFragment);
    }

    public static class DirectoryChooserFragment extends ListFragment {

        private static final String KEY_FILE_INFO_LIST = "KEY_FILE_INFO_LIST";

        public static final String KEY_CURRENT_DIRECTORY = "KEY_CURRENT_DIRECTORY";

        private ProgressBar fileChooserEmptyViewSpinner;
        private TextView fileChooserEmptyViewText;
        private TextView displayPath;

        private String currentDirectory;
        private FileAdapter fileAdapter;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (savedInstanceState != null) {
                currentDirectory = savedInstanceState.getString(KEY_CURRENT_DIRECTORY);
                List<FileInfo> fileList = (List<FileInfo>) savedInstanceState.getSerializable(KEY_FILE_INFO_LIST);
                fileAdapter = new FileAdapter(getActivity(), fileList);
            } else {
                String storedDirectory = getArguments().getString(KEY_CURRENT_DIRECTORY);
                currentDirectory = storedDirectory != null ? storedDirectory : PathHelper.PATH_EXTERNAL_STORAGE_DIRECTORY;
                List<FileInfo> fileList = populateFileList(currentDirectory);
                FileHelper.sortFileInfoList(fileList);
                fileAdapter = new FileAdapter(getActivity(), fileList);
                updateProgressView(fileList);
            }
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_file_chooser, container, false);

            fileChooserEmptyViewSpinner = (ProgressBar) rootView.findViewById(R.id.fileChooserEmptyViewSpinner);
            fileChooserEmptyViewText = (TextView) rootView.findViewById(R.id.fileChooserEmptyViewText);
            displayPath = (TextView) rootView.findViewById(R.id.displayPath);
            displayPath.setText(PathHelper.formatDisplayPath(currentDirectory));

            rootView.findViewById(R.id.selectButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra(KEY_SELECTED_DIRECTORY, currentDirectory);
                    getActivity().setResult(RESULT_OK, intent);
                    getActivity().finish();
                }
            });

            rootView.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });

            resetEmptyView();

            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setListAdapter(fileAdapter);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString(KEY_CURRENT_DIRECTORY, currentDirectory);
            outState.putSerializable(KEY_FILE_INFO_LIST, (Serializable) fileAdapter.getFileInfoList());
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            registerForContextMenu(getListView());
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);

            FileInfo clickedFileInfo = fileAdapter.getItem(position);
            if (clickedFileInfo.getType() == FileType.DIRECTORY || clickedFileInfo.getType() == FileType.DIRECTORY_UP) {
                resetEmptyView();

                String path = clickedFileInfo.getPath();
                List<FileInfo> fileList = populateFileList(path);
                displayPath.setText(PathHelper.formatDisplayPath(path));

                FileHelper.sortFileInfoList(fileList);
                fileAdapter.setFileInfoList(fileList);
                updateProgressView(fileList);

                currentDirectory = path;
            }
        }

        @NonNull
        public List<FileInfo> populateFileList(@NonNull String path) {
            File file = new File(path);
            File[] filesAndDirectories = file.listFiles();
            List<FileInfo> directories = new ArrayList<>();
            List<FileInfo> files = new ArrayList<>();

            try {
                if (filesAndDirectories != null) {
                    for (File f : filesAndDirectories) {
                        if (f.isDirectory()) {
                            directories.add(new FileInfo(f.getName(), f.getAbsolutePath(), 0, FileType.DIRECTORY));
                        } else {
                            files.add(new FileInfo(f.getName(), f.getAbsolutePath(), f.length(), FileType.FILE));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            directories.addAll(files);

            if (file.getParent() != null && !PathHelper.isExternalStorageDirectoryRoot(file.getAbsolutePath())) {
                directories.add(0, new FileInfo(getActivity().getString(R.string.parent_directory), file.getParent(), 0, FileType.DIRECTORY_UP));
            }

            return directories;
        }

        private void resetEmptyView() {
            fileChooserEmptyViewSpinner.setVisibility(View.VISIBLE);
            fileChooserEmptyViewText.setVisibility(View.GONE);
        }

        public void updateProgressView(List<FileInfo> fileInfoList) {
            if (fileInfoList == null || fileInfoList.size() == 0) {
                fileChooserEmptyViewSpinner.setVisibility(View.GONE);
                fileChooserEmptyViewText.setVisibility(View.VISIBLE);
            }
        }
    }
}
