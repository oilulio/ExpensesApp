package com.github.oilulio.expensesapp;

import androidx.core.content.FileProvider;
// https://developer.android.com/reference/androidx/core/content/FileProvider
// "It is possible to use FileProvider directly instead of extending it. However, this is not reliable and will causes crashes on some devices."

public class MyFileProvider extends FileProvider {
    public MyFileProvider() {
        super(R.xml.file_paths);
    }
}