package org.foodscanner.foodscanner.helpers;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import org.foodscanner.foodscanner.Constants;

import java.io.File;

public class AssetHelper {

    private static final String TESSDATA_FOLDER = "tessdata";
    private static final String TESSDATA_FILE = "slv.traineddata";
    private static final String TEST_FOLDER = "test";
    private static final String TEST_IMAGE = "test.jpg";

    private Context mContext;
    private AssetManager mManager;


    public AssetHelper(Context context) {
        mContext = context;
        mManager = context.getAssets();
    }

    public boolean moveTesseractAssets() {
        File tessdataFolder = mContext.getExternalFilesDir(TESSDATA_FOLDER);
        if (tessdataFolder == null) {
            Log.e(Constants.TAG, "External storage is currently not available!");
            return false;
        }

        String tessdataFilePath = tessdataFolder.getPath() + "/" + TESSDATA_FILE;
        String tessDataAssetFile = TESSDATA_FOLDER + "/" + TESSDATA_FILE;

        return FileOperations.moveAssetFileToExternalStorage(mManager, tessdataFilePath, tessDataAssetFile);
    }

    public boolean moveTestAssets() {
        File picturesFolder = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesFolder == null) {
            Log.e(Constants.TAG, "External storage is currently not available!");
            return false;
        }

        String testPictureFilePath = picturesFolder.getPath() + "/" + FileOperations.TEST_IMAGE_NAME + FileOperations.IMAGE_SUFFIX;
        String testPictureAssetFile = TEST_FOLDER + "/" + TEST_IMAGE;

        return FileOperations.moveAssetFileToExternalStorage(mManager, testPictureFilePath, testPictureAssetFile);
    }

    public boolean wereAssetsMoved() {
        File tessdataFolder = mContext.getExternalFilesDir(TESSDATA_FOLDER);
        if (tessdataFolder == null) {
            Log.e(Constants.TAG, "External storage is currently not available!");
            return false;
        }

        File picturesFolder = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesFolder == null) {
            Log.e(Constants.TAG, "External storage is currently not available!");
            return false;
        }

        String tessdataFilePath = tessdataFolder.getPath() + "/" + TESSDATA_FILE;
        String testPictureFilePath = picturesFolder + "/" + FileOperations.TEST_IMAGE_NAME + FileOperations.IMAGE_SUFFIX;

        return FileOperations.doesFileExist(tessdataFilePath) && FileOperations.doesFileExist(testPictureFilePath);
    }
}
