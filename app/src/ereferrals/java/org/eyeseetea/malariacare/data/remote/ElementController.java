package org.eyeseetea.malariacare.data.remote;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.element.utils.ElementSDKManager;

import org.eyeseetea.malariacare.domain.boundary.IExternalVoucherRegistry;

public class ExternalVoucherRegistry implements IExternalVoucherRegistry, ElementSDKManager.ElementActivityListener, ElementSDKManager.EnrollListener {

    final private String TAG = "ExternalVoucherRegistry";

    public interface Callback {
        void onSuccess(String uid);

        void onError();
    }

    private Activity mActivity;
    private Callback mCallback;

    public ExternalVoucherRegistry(Callback callback, Activity activity){
        mCallback = callback;
        mActivity = activity;
    }

    @Override
    public void sendVoucherUId(String voucherUId) {
        ElementSDKManager.enrollNewUser(mActivity, voucherUId, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Object data) {
        ElementSDKManager.onActivityResult(requestCode, resultCode, (Intent) data, this);
    }

    @Override
    public void onUserEnrolled(String id, boolean isNewUser) {
        Log.d(TAG,  "onUserEnrolled id:"+id +" new:"+isNewUser);
        mCallback.onSuccess(id);
    }

    @Override
    public void onUserFailedToEnroll() {
        Log.d(TAG, "onUserFailedToEnroll");
        mCallback.onError();
    }
}
