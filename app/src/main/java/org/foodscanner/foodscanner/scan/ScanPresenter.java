package org.foodscanner.foodscanner.scan;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.foodscanner.foodscanner.Constants;
import org.foodscanner.foodscanner.data.Result;
import org.foodscanner.foodscanner.data.Word;
import org.foodscanner.foodscanner.helpers.FileOperations;
import org.foodscanner.foodscanner.interfaces.CancelOcrCallback;
import org.foodscanner.foodscanner.interfaces.OcrCallbacksManager;
import org.foodscanner.foodscanner.tasks.AdditiveRecognitionTask;
import org.foodscanner.foodscanner.tasks.OcrTask;
import org.foodscanner.foodscanner.tasks.ParseHocrTask;

import static com.google.common.base.Preconditions.checkNotNull;

public class ScanPresenter implements ScanContract.Presenter,
        OcrCallbacksManager {

    private static final int REQUEST_CODE_IMAGE_CAPTURE = 1;
    private static final int RESULT_OK = -1;

    private static Uri sTakenImageUri;
    private CancelOcrCallback mCancelOcrCallback;

    @NonNull
    private final ScanContract.View mScanView;

    ScanPresenter(@NonNull ScanContract.View scanView) {
        mScanView = scanView;

        mScanView.setPresenter(this);
    };

    @Override
    public void start() {
    }

    @Override
    public void startCameraActivity() {
        AppCompatActivity activity = mScanView.getParentActivity();
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(cameraIntent, REQUEST_CODE_IMAGE_CAPTURE);
            return;
        }

        Log.e(Constants.TAG, "Camera intent does not exist");
        mScanView.showSnackBar("Camera could not be started!", Snackbar.LENGTH_LONG);
    }

    @Override
    public void startCropActivity() {
        AppCompatActivity activity = mScanView.getParentActivity();

        Uri croppedImageUri = FileOperations.createExternalImageFile(
                activity,
                FileOperations.CROPPED_IMAGE_NAME,
                FileOperations.IMAGE_SUFFIX
        );

        CropImage.activity(sTakenImageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setFixAspectRatio(false)
                .setMaxZoom(50)
                .setOutputUri(croppedImageUri)
                .start(activity);
    }

    @Override
    public void startOcrTask() {
        mCancelOcrCallback = (CancelOcrCallback) new OcrTask(mScanView.getParentActivity(), this).execute();
    }

    @Override
    public void cancelOcrTask() {
        if (mCancelOcrCallback == null) {
            Log.e(Constants.TAG, "Cancel OCR callback is null!");
            return;
        }

        mCancelOcrCallback.onCancelOcr();
    }

    @Override
    public void startParseOcrResultTask(String hocrText) {
        new ParseHocrTask(this).execute(hocrText);
    }

    @Override
    public void startAdditiveRecognitionTask() {
        new AdditiveRecognitionTask(mScanView.getParentActivity(), this, null).execute();
    }

    @Override
    public void changePage(int page) {
        mScanView.changePage(page);
    }

    @Override
    public FragmentManager getFragmentManager() {
        return mScanView.getParentActivity().getSupportFragmentManager();
    }

    @Override
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_IMAGE_CAPTURE:
                if (resultCode != RESULT_OK) {
                    Log.e(Constants.TAG, "Camera result returned error");
                    return;
                }

                sTakenImageUri = data.getData();
                checkNotNull(sTakenImageUri);
                Log.d(Constants.TAG, "mTakenImageUri set to " + sTakenImageUri.toString());

                startCropActivity();

                return;

            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                // Store cropping result
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == RESULT_OK) {
                    // Start OCR process
                    mScanView.showOcrProgress();
                    startOcrTask();
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    Log.e(Constants.TAG, error.toString());
                }

                Log.i(Constants.TAG, "Deleting original image");
                // The original image is not needed
                deleteTakenImage();

                break;
        }
    }

    @Override
    public void setOcrProgressPercentage(int percentage) {
        mScanView.updateOcrProgressPercentage(percentage);
    }

    @Override
    public void onOcrCompleted(String hocrText) {
        startParseOcrResultTask(hocrText);
    }

    @Override
    public void onOcrCanceled() {
        mScanView.showMain();   // Display main fragment
    }

    @Override
    public void onHocrParsingComplete() {
        printHocrParsingResultDEBUG();   //TODO: DEBUG only
        startAdditiveRecognitionTask();
    }

    @Override
    public void onAdditiveRecognitionCompleted(boolean wereAdditivesRecognized) {
        mScanView.showResult();
    }

    private void deleteTakenImage() {
        checkNotNull(sTakenImageUri);
        FileOperations.deleteContentFile(mScanView.getParentActivity(), sTakenImageUri);
    }

    private void printHocrParsingResultDEBUG() {
        Log.d(Constants.TAG, "Hocr completed, result: ");

        for (Word w : checkNotNull(Result.getInstance().getWordList())) {
            Log.d(Constants.TAG, w.getName());
        }
    }
}
