package org.eyeseetea.malariacare.data.io;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

import android.content.pm.PackageManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesEReferral;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.boundary.io.IFileDownloader;
import org.eyeseetea.malariacare.domain.entity.Media;
import org.eyeseetea.malariacare.domain.exception.FileDownloadException;
import org.eyeseetea.sdk.data.DownloadMediaTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FileDownloader implements IFileDownloader {


    private GoogleCredential serviceCredential;
    private static final String[] SCOPES = {DriveScopes.DRIVE};
    private final File mFileDir;
    private final InputStream mPrivateJsonStream;
    private Drive drive;

    public FileDownloader(File fileDir, InputStream privateJsonStream) {
        mFileDir = required(fileDir, "File dir is required");
        mPrivateJsonStream = required(privateJsonStream,
                "Private Json stream with google play key is required");
    }

    @Override
    public void download(String rootUid, String program, final Callback mCallback) {
        initServiceAccountCredential();

        if (!isGooglePlayServicesAvailable()) {
            if (!isGooglePlayAppAvailable()) {
                return;
            }
            mCallback.onError(new FileDownloadException(new GooglePlayAppNotAvailableException()));
            return;
        }

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        drive = new Drive.Builder(
                transport, jsonFactory, null)
                .setHttpRequestInitializer(serviceCredential)
                .build();
        if (drive == null) {
            System.out.println("Drive is null, init() method must be called first");
        }
        List<String> uids = new ArrayList<>();

        final List<Media> mediaFiles = new ArrayList<>();
        try {
            getFilesFromDriveFolder(rootUid, program, mediaFiles);
        } catch (IOException e) {
            e.printStackTrace();
            mCallback.onError(new FileDownloadException(e));
            return;
        }

        for(Media media:mediaFiles){
            uids.add(media.getResourceUrl());
        }
        new DownloadMediaTask(mFileDir, drive, uids,
                new DownloadMediaTask.Callback() {

                    @Override
                    public void onError(Exception error) {
                        mCallback.onError(new FileDownloadException(error));
                    }

                    @Override
                    public void onCancelled(Exception mLastError) {
                        mCallback.onError(new FileDownloadException(mLastError));
                    }

                    @Override
                    public void onSuccess(HashMap<String, String> syncedFiles) {
                        for(Media media : mediaFiles){
                            media.setResourcePath(syncedFiles.get(media.getResourceUrl()));
                        }
                        mCallback.onSuccess(mediaFiles);
                    }
                }).execute();
    }

    private void getFilesFromDriveFolder(String rootUid, String program, List<Media> mediaFiles)
            throws IOException {
        FileList folders = drive.files().list().setQ(
                "\'" + rootUid + "\' in parents").execute();
        for (com.google.api.services.drive.model.File file : folders.getFiles()) {
            if (file.getMimeType().equals("application/vnd.google-apps.folder")) {
                if (file.getName().equals(ProgramDB.getProgram(
                        PreferencesEReferral.getUserProgramId()).getName())) {
                    FileList fileList = drive.files().list().setQ(
                            "\'" + file.getId() + "\' in parents").execute();
                    for (com.google.api.services.drive.model.File fileMedia : fileList
                            .getFiles()) {
                        Media media = convertInMedia(fileMedia, program);
                        if (media != null) {
                            mediaFiles.add(media);
                        }
                    }
                }
            }
        }
    }

    private static Media convertInMedia(com.google.api.services.drive.model.File file, String program) {
        if (!file.getMimeType().equals("application/vnd.google-apps.folder")) {
            Media.MediaType mediaType = Media.MediaType.VIDEO;
            if (file.getMimeType().contains("image")) {
                mediaType = Media.MediaType.PICTURE;
            } else if (file.getMimeType().contains("video")) {
                mediaType = Media.MediaType.VIDEO;
            }
            return new Media(file.getId(), mediaType, program);
        }
        return null;
    }

    private void initServiceAccountCredential() {
        try {
            if(serviceCredential==null) {
                serviceCredential = GoogleCredential.fromStream(mPrivateJsonStream).createScoped(
                        Arrays.asList(SCOPES));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isGooglePlayAppAvailable() {
        try {
            PreferencesState.getInstance().getContext().getPackageManager()
                    .getPackageInfo(GooglePlayServicesUtil.GOOGLE_PLAY_STORE_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            System.out.println("No GooglePlay Available . acquire play services will not possible");
            return false;
        }
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(
                PreferencesState.getInstance().getContext());
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }
}
