package org.foodscanner.foodscanner.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.foodscanner.foodscanner.Constants;
import org.foodscanner.foodscanner.helpers.AssetHelper;
import org.foodscanner.foodscanner.helpers.FileOperations;
import org.foodscanner.foodscanner.interfaces.CancelOcrCallback;
import org.foodscanner.foodscanner.interfaces.OcrCallbacksManager;

import java.io.File;

/**
 * Asynchronous task to perform the OCR process.
 */
public class OcrTask extends AsyncTask<Void, Integer, String> implements TessBaseAPI.ProgressNotifier, CancelOcrCallback {

    private OcrCallbacksManager mOcrCallbacksManager;
    private AssetHelper mAssetHelper;
    private Context mContext;
    private String mFilesDir;
    private String mCroppedImageFilePath;
    private TessBaseAPI mOcr;
    private boolean mIsOcrStopped;

    public OcrTask(AppCompatActivity activity, OcrCallbacksManager ocrCallbacksManager) {
        mAssetHelper = new AssetHelper(activity);
        mContext = activity.getApplicationContext();
        mOcrCallbacksManager = ocrCallbacksManager;
    }

    /**
     *  Copies Tesseract assets to the device's External Storage.
     */
    @Override
    protected void onPreExecute() {
        // Check if tessdata was set-up before (first time running OCR)
        if (!mAssetHelper.wereAssetsMoved()) {
            Log.d(Constants.TAG, "Assets moving started");

            if (!mAssetHelper.moveTesseractAssets() || !mAssetHelper.moveTestAssets()) {
                Log.e(Constants.TAG, "Copying of tessdata failed!");
                // TODO: Handle this!
                return;
            }
            Log.d(Constants.TAG, "Assets were moved successfully!");
        }

        mCroppedImageFilePath = FileOperations.getCroppedImageFile(mContext).getPath();

        File filesDir = mContext.getExternalFilesDir("");
        if (filesDir == null) {
            //TODO: Handle this
            return;
        }

        mFilesDir = filesDir.getPath();
    }

    /**
     * Initializes the OCR engine by providing it the path to the language and image files. Performs
     * the OCR process on the given image and returns the result in the hOCR format.
     *
     * @return hOCR text recognized with OCR
     */
    @Override
    protected String doInBackground(Void... voids) {
        mOcr = new TessBaseAPI(this);
        // TODO: Remove hardcoded language!
        mOcr.init(mFilesDir, "slv");
        mOcr.setImage(
                FileOperations.openExternalImageFile(mCroppedImageFilePath)
        );
        String hocrResult = mOcr.getHOCRText(0);
        mOcr.end();
        return hocrResult;
    }

    /**
     * Ocr progress listener - receives ocr process progress updates. Publishes progress to async
     * task method.
     *
     * @param progressValues OCR Api progress object containing among other things the progress percentage
     */
    @Override
    public void onProgressValues(TessBaseAPI.ProgressValues progressValues) {
        publishProgress(progressValues.getPercent());
    }

    /**
     * Listens for progress reports and displays them through the UI thread.
     *
     * @param values progress percentage to display
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        mOcrCallbacksManager.setOcrProgressPercentage(values[0]);
        Log.d(Constants.TAG, "OCR Progress: " + values[0]);
    }

    /**
     * Performs callback to listening class (MainActivity) and provides said class with the OCR
     * result.
     *
     * @param hocrText OCR result in hocr ("xml") form
     */
    @Override
    protected void onPostExecute(String hocrText) {
        if (mIsOcrStopped) {
            mOcrCallbacksManager.onOcrCanceled();
            return;
        }
        mOcrCallbacksManager.onOcrCompleted(hocrText);
    }


    @Override
    public void onCancelOcr() {
        if (mOcr != null) {
            try {
                mOcr.stop();
                mIsOcrStopped = true;
                Log.d(Constants.TAG, "OCR stopped successfully!");
            } catch (Exception ex) {
                Log.e(Constants.TAG, "Unable to stop ocr process:\n\n" + ex.toString());
            }
        }
    }
}
