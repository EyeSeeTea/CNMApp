package org.eyeseetea.malariacare.strategies;

import static org.eyeseetea.malariacare.services.strategies.APushServiceStrategy.TAG;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import org.eyeseetea.malariacare.BuildConfig;
import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.SplashScreenActivity;
import org.eyeseetea.malariacare.data.authentication.CredentialsReader;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.intent.ConnectVoucher;
import org.eyeseetea.malariacare.data.mappers.ConnectVoucherMapper;
import org.eyeseetea.malariacare.domain.boundary.IConnectivityManager;
import org.eyeseetea.malariacare.domain.usecase.DownloadLanguageTranslationUseCase;
import org.eyeseetea.malariacare.domain.utils.IntentSurveyCreator;
import org.eyeseetea.malariacare.network.factory.NetworkManagerFactory;

import java.io.IOException;

public class SplashActivityStrategy extends ASplashActivityStrategy {
    public static final String INTENT_JSON_EXTRA_KEY = "ConnectVoucher";
    private Activity activity;

    public interface Callback {
        void onSuccess();
    }

    public SplashActivityStrategy(Activity mActivity) {
        super(mActivity);
        this.activity = mActivity;
        if(BuildConfig.translations) {
            PreferencesState.getInstance().loadsLanguageInActivity();
        }
    }

    public void init(final SplashScreenActivity.Callback callback) {
        String connectVoucherJson = activity.getIntent().getStringExtra(
                INTENT_JSON_EXTRA_KEY);
        clearIntentExtras();
        if(connectVoucherJson!=null) {
            ConnectVoucher connectVoucher = null;
            try {
                connectVoucher = ConnectVoucherMapper.parseJson(connectVoucherJson);
                if(connectVoucher.getValues()!=null && connectVoucher.getValues().size()>0) {
                    //a existing auth with empty user and password identifies a connect voucher from other app without credentials.
                    saveAuthFromIntent(connectVoucher);
                    IntentSurveyCreator intentSurveyCreator = new IntentSurveyCreator();
                    intentSurveyCreator.createFromConnectVoucher(connectVoucher.getValues(), new Callback() {
                        @Override
                        public void onSuccess() {
                            callback.onSuccess(canEnterApp());
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(activity, R.string.format_error, Toast.LENGTH_LONG).show();
            }
        }else{
            //when the connect voucher from other app not exists, the auth is null
            PreferencesState.getInstance().clearIntentCredentials();
            callback.onSuccess(canEnterApp());
        }
    }

    private void saveAuthFromIntent(ConnectVoucher connectVoucher) {
        PreferencesState.getInstance().setIntentCredentials(connectVoucher.getAuth());
    }

    @Override
    public void finishAndGo() {
        super.finishAndGo(LoginActivity.class);
    }

    @Override
    public boolean canEnterApp() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return true;
        }else {
            showDialogNotSupportedAndroid();
            return false;
        }
    }

    private void showDialogNotSupportedAndroid() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_title_error)
                .setMessage(R.string.error_android_version_not_supported)
                .setPositiveButton(R.string.provider_redeemEntry_msg_matchingOk,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        activity.finish();
                    }
                })
                .setCancelable(false);
        builder.show();
    }

    @Override
    public void downloadLanguagesFromServer() {
        try {
            Log.i(TAG, "Starting to download Languages From Server");
            CredentialsReader credentialsReader = CredentialsReader.getInstance();
            IConnectivityManager connectivity = NetworkManagerFactory.getConnectivityManager(
                    activity);

            DownloadLanguageTranslationUseCase downloader =
                    new DownloadLanguageTranslationUseCase(credentialsReader, connectivity);

            downloader.download();
        } catch (Exception e) {
            Log.e(TAG, "Unable to download Languages From Server" + e.getMessage());
            e.printStackTrace();
            showToast(R.string.error_downloading_languages, e);
        }
    }

    private void showToast(int titleResource, final Exception e) {
        final String title = activity.getResources().getString(titleResource);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, title + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    private void clearIntentExtras() {
        if(activity.getIntent().getExtras()!=null) {
            //remove intent extras.
            activity.setIntent(activity.getIntent().putExtra(INTENT_JSON_EXTRA_KEY, ""));
        }
    }
}
