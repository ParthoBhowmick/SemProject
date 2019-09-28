/*
 Privacy Friendly Finance Manager is licensed under the GPLv3.
 Copyright (C) 2019 Leonard Otto, Felix Hofmann

 This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 General Public License as published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this program.
 If not, see http://www.gnu.org/licenses/.

 Additionally icons from Google Design Material Icons are used that are licensed under Apache
 License Version 2.0.
 */

package org.secuso.privacyfriendlyfinance.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.secuso.privacyfriendlyfinance.R;
import org.secuso.privacyfriendlyfinance.activities.helper.FullTaskListener;
import org.secuso.privacyfriendlyfinance.domain.FinanceDatabase;
import org.secuso.privacyfriendlyfinance.helpers.KeyStoreHelper;
import org.secuso.privacyfriendlyfinance.helpers.KeyStoreHelperException;
import org.secuso.privacyfriendlyfinance.helpers.SharedPreferencesManager;

/**
 * Startup activity.
 *
 * @author Felix Hofmann
 * @author Leonard Otto
 */
public class StartupActivity extends AppCompatActivity implements FullTaskListener {
    ProgressBar progressBar;
    TextView progressText;
    boolean keyGen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferencesManager.init(getApplicationContext());
        setContentView(R.layout.activity_startup);
        getSupportActionBar().hide();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        progressText = findViewById(R.id.progressText);
        progressBar = findViewById(R.id.progressBar);
        if (FinanceDatabase.getInstance() == null) {
            try {
                if (!KeyStoreHelper.getInstance(FinanceDatabase.KEY_ALIAS).keyExists()) {
                    keyGen = true;
                    progressText.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setMax(1000);
                }
            } catch (KeyStoreHelperException e) {
                e.printStackTrace();
            }

            FinanceDatabase.connect(getApplicationContext(), this);
        } else {
            nextActivity();
        }
    }

    @Override
    public void onDone(Object result, AsyncTask<?, ?, ?> task) {
        nextActivity();
    }

    private void nextActivity() {
        Intent mainIntent;
        if(SharedPreferencesManager.isFirstTimeLaunch()) {
            mainIntent = new Intent(this, TutorialActivity.class);
        } else {
            mainIntent = new Intent(this, TransactionsActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }

        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onProgress(final Double progress, AsyncTask<?, ?, ?> task) {
        if (keyGen) runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(Double.valueOf(progress * 1000).intValue());
            }
        });
    }

    @Override
    public void onOperation(final String operation, AsyncTask<?, ?, ?> task) {
        if (keyGen) runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressText.setText(operation);
            }
        });
    }
}
