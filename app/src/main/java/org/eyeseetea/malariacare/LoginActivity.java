/*
 * Copyright (c) 2015.
 *
 * This file is part of QIS Surveillance App.
 *
 *  QIS Surveillance App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QIS Surveillance App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QIS Surveillance App.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.repositories.UserAccountRepository;
import org.eyeseetea.malariacare.domain.boundary.IUserAccountRepository;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.usecase.ALoginUseCase;
import org.eyeseetea.malariacare.domain.usecase.LoginUseCase;
import org.eyeseetea.malariacare.network.ServerAPIController;
import org.eyeseetea.malariacare.strategies.LoginActivityStrategy;
import org.eyeseetea.malariacare.utils.Utils;
import org.hisp.dhis.client.sdk.ui.activities.AbsLoginActivity;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Login Screen.
 * It shows only when the user has an open session.
 */
public class LoginActivity extends AbsLoginActivity {

    public static final String PULL_REQUIRED = "PULL_REQUIRED";
    public static final String DEFAULT_USER = "";
    public static final String DEFAULT_PASSWORD = "";
    private static final String TAG = ".LoginActivity";
    public IUserAccountRepository mUserAccountRepository = new UserAccountRepository(this);
    public LoginUseCase mLoginUseCase = new LoginUseCase(mUserAccountRepository);
    public LoginActivityStrategy mLoginActivityStrategy = new LoginActivityStrategy(this);
    EditText serverText;
    EditText usernameEditText;
    EditText passwordEditText;


    private ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        AsyncInit asyncPopulateDB = new AsyncInit(this);
        asyncPopulateDB.execute((Void) null);
    }

    private void initDataDownloadPeriodDropdown() {
        if (!BuildConfig.loginDataDownloadPeriod) {
            return;
        }
        //Add left text for the spinner "title"
        findViewById(R.id.date_spinner_container).setVisibility(View.VISIBLE);
        TextView textView = (TextView) findViewById(R.id.data_text_view);
        textView.setText(R.string.download);

        //add options
        ArrayList<String> dataLimitOptions = new ArrayList<>();
        dataLimitOptions.add(getString(R.string.no_data));
        dataLimitOptions.add(getString(R.string.last_6_days));
        dataLimitOptions.add(getString(R.string.last_6_weeks));
        dataLimitOptions.add(getString(R.string.last_6_months));

        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dataLimitOptions);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //add spinner
        Spinner spinner = (Spinner) findViewById(R.id.data_spinner);
        spinner.setVisibility(View.VISIBLE);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                PreferencesState.getInstance().setDataLimitedByDate(
                        spinnerArrayAdapter.getItem(pos).toString());
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //select the selected option or default no data option
        String dateLimit = PreferencesState.getInstance().getDataLimitedByDate();
        if (dateLimit.equals("")) {
            spinner.setSelection(spinnerArrayAdapter.getPosition(getString(R.string.no_data)));
        } else {
            spinner.setSelection(spinnerArrayAdapter.getPosition(dateLimit));
        }
    }

    @Override
    protected void onLoginButtonClicked(Editable server, Editable username, Editable password) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.getBoolean(getString(R.string.eula_accepted), false)) {
            askEula(R.string.app_EULA, R.raw.eula, LoginActivity.this);
        } else {
            login(server.toString(), username.toString(), password.toString());
        }
    }


    /**
     * Shows an alert dialog asking for acceptance of the EULA terms. If ok calls login function,
     * do
     * nothing otherwise
     */
    public void askEula(int titleId, int rawId, final Context context) {
        InputStream message = context.getResources().openRawResource(rawId);
        String stringMessage = Utils.convertFromInputStreamToString(message).toString();
        final SpannableString linkedMessage = new SpannableString(Html.fromHtml(stringMessage));
        Linkify.addLinks(linkedMessage, Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(titleId))
                .setMessage(linkedMessage)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rememberEulaAccepted(context);
                        login(serverText.toString(), usernameEditText.toString(),
                                passwordEditText.toString());
                    }
                })
                .setNegativeButton(android.R.string.no, null).create();

        dialog.show();

        ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(
                LinkMovementMethod.getInstance());
    }

    /**
     * Save a preference to remember that EULA was already accepted
     */
    public void rememberEulaAccepted(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.eula_accepted), true);
        editor.commit();
    }

    public void login(String serverUrl, String username, String password) {
        Credentials credentials = new Credentials(serverUrl, username, password);

        mLoginUseCase.execute(credentials, new ALoginUseCase.Callback() {
            @Override
            public void onLoginSuccess() {
                mLoginActivityStrategy.finishAndGo();
            }

            @Override
            public void onServerURLNotValid() {
                serverText.setError(getString(R.string.login_invalid_server_url));
                showError(getString(R.string.login_invalid_server_url));
            }

            @Override
            public void onInvalidCredentials() {
                showError(getString(R.string.login_invalid_credentials));
            }

            @Override
            public void onNetworkError() {
                showError(getString(R.string.network_error));
            }
        });
    }

    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    //// FIXME: 28/12/16
    //@Subscribe
    public void onLoginFinished() {
/*        if (result != null && result.getResourceType().equals(ResourceType.USERS)) {
            if (result.getResponseHolder().getApiException() == null) {

                Credentials credentials = new Credentials(serverUrl, username, password);
                mLoginUseCase.execute(credentials);
                //The first login is only to authenticate the user, and is need logout from the
                // sdk and login with the correct user/password.
                if (mLoginUseCase.isLogoutNeeded(credentials)) {
                    username = DEFAULT_USER;
                    password = DEFAULT_PASSWORD;
                    //The first login (user authentication) calls this
                    DhisService.logOutUser(this);
                } else {
                    mLoginActivityStrategy.finishAndGo();
                }
            } else {
                onLoginFail(result.getResponseHolder().getApiException());
            }
        }*/
    }


    @Override
    public void onBackPressed() {
        mLoginActivityStrategy.onBackPressed();
    }

    private void init() {
        initDataDownloadPeriodDropdown();
        //Populate server with the current value
        serverText = (EditText) findViewById(R.id.edittext_server_url);
        serverText.setText(ServerAPIController.getServerUrl());

        //Username, Password blanks to force real login
        usernameEditText = (EditText) findViewById(R.id.edittext_username);
        usernameEditText.setText(DEFAULT_USER);
        passwordEditText = (EditText) findViewById(R.id.edittext_password);
        passwordEditText.setText(DEFAULT_PASSWORD);
    }

    public class AsyncInit extends AsyncTask<Void, Void, Exception> {
        Activity activity;

        AsyncInit(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            //// FIXME: 30/12/16  Fix mising progressbar
            //bar = (ProgressBar) activity.findViewById(R.id.progress_bar_circular);
            bar = (ProgressBar) activity.findViewById(R.id.progress_bar_circular);
            bar.setVisibility(View.VISIBLE);
            activity.findViewById(R.id.layout_login_views).setVisibility(View.GONE);
        }

        @Override
        protected Exception doInBackground(Void... params) {
            mLoginActivityStrategy.onCreate();
            return null;
        }

        @Override
        protected void onPostExecute(final Exception exception) {
            //Error
            bar.setVisibility(View.GONE);
            activity.findViewById(R.id.layout_login_views).setVisibility(View.VISIBLE);

            init();
        }
    }
}



