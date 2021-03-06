package org.eyeseetea.malariacare.strategies;

import android.content.Intent;
import android.view.MenuItem;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.SettingsActivity;
import org.eyeseetea.malariacare.domain.boundary.IAuthenticationManager;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.usecase.LoadUserAndCredentialsUseCase;
import org.eyeseetea.malariacare.domain.usecase.LoginUseCase;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;

public class LoginActivityStrategy extends ALoginActivityStrategy {
    public LoginActivityStrategy(LoginActivity loginActivity) {
        super(loginActivity);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(loginActivity, SettingsActivity.class);
        loginActivity.startActivity(intent);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    public void onCreate() {
        LoadUserAndCredentialsUseCase loadUserAndCredentialsUseCase =
                new LoadUserAndCredentialsUseCase(loginActivity);

        loadUserAndCredentialsUseCase.execute();
    }

    @Override
    public void finishAndGo() {
        loginActivity.onFinishLoading(null);
        if (loginActivity.getIntent().getBooleanExtra(LoginActivity.PULL_REQUIRED, false)) {
            loginActivity.startActivity(new Intent(loginActivity, ProgressActivity.class));
        } else {
            Intent intent = new Intent(loginActivity, SettingsActivity.class);

            intent.putExtra(SettingsActivity.IS_LOGIN_DONE, true);

            loginActivity.startActivity(intent);
        }

        loginActivity.finish();
    }

    @Override
    public void initViews() {

    }


    @Override
    public void onLoginSuccess(Credentials credentials) {
        loginActivity.checkAnnouncement();
    }

    @Override
    public void initLoginUseCase(IAuthenticationManager authenticationManager) {
        IAsyncExecutor asyncExecutor = new AsyncExecutor();
        IMainExecutor mainExecutor = new UIThreadExecutor();

        loginActivity.mLoginUseCase = new LoginUseCase(authenticationManager, asyncExecutor, mainExecutor);
    }


}
