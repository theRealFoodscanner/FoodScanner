package org.foodscanner.foodscanner.helpers;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import org.foodscanner.foodscanner.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class containing some basic file operations which are used throughout the application. The
 * operations include creation, copying and deletion of files.
 */
public final class FileOperations {
    public static final String CROPPED_IMAGE_NAME = "cropped_image";
    public static final String TEST_IMAGE_NAME = "test";
    public static final String IMAGE_SUFFIX = ".jpg";
    private static final String TAG = "FoodScanner-FO";
    private static final String OCR_FOLDER = "tessdata";

    private FileOperations() {}

    public static File getPicturesDir(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    public static File getCroppedImageFile(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/" + getCroppedImageName());
    }

    /**
     * Moves an apk assets file to the device's external storage.
     *
     * @return true if file was successfully moved, false otherwise.
     */
    public static boolean moveAssetFileToExternalStorage(AssetManager assetManager, String destinationFilePath, String assetFile) {
        InputStream input = null;
        OutputStream output = null;

        String pathWithoutFile = destinationFilePath.substring(0, destinationFilePath.lastIndexOf('/'));
        File folderStructure = new File(pathWithoutFile);
        if (!folderStructure.mkdirs()) {
            Log.e(TAG, String.format("Could not make folder structure (%1$s).", pathWithoutFile));
        }

        File destinationFile = new File(destinationFilePath);

        // Initialize streams and try to copy file
        try {
            input = assetManager.open(assetFile);
            output = new FileOutputStream(destinationFile);
            copyFile(
                    input,  // source file
                    output, // destination file
                    1024    // buffer size
            );
        } catch (IOException e) {
            // TODO: Handle failure to copy files!
            e.printStackTrace();
            return false;
        } finally {
            // Close streams
            try {
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to close i/o stream!");
            }
        }

        return true;//destinationFile.getParent();
    }

    /**
     * Deletes the file provided within the uri using the Android ContentResolver class.
     *
     * @param context application context
     * @param uri content uri of file to be deleted
     */
    public static void deleteContentFile(Context context, Uri uri) {
        context.getContentResolver().delete(uri, null, null);
        Log.i(TAG, "Deleted: " + uri);
    }

    /**
     * Creates an image file in the private application storage named with the params data and
     * returns its Uri. The Uri is returned by using the FileProvider class.
     *
     * @param context application context
     * @param name name of the file to create
     * @param suffix suffix of the file to create
     * @return Uri of the temporary image file.
     */
    public static Uri createExternalImageFile(Context context, String name, String suffix) {
        File imageFile = new File(getPicturesDir(context), name + suffix);

        // TODO: Remove hardcoded authority?
        return FileProvider.getUriForFile (
                context,                                // context
                "org.foodscanner.foodscanner.fileprovider",   // authority
                imageFile                               // file
        );
    }

    /**
     * Opens image specified with imageName and returns its Bitmap.
     *
     * @param context context
     * @param imageName name of image file (with suffix)
     * @return instance of provided image file
     */
    public static Bitmap openExternalImageFile(Context context, String imageName) {
        return BitmapFactory.decodeFile(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + imageName
        );
    }

    /**
     * Opens image specified with imageName and returns its Bitmap.
     *
     * @param imagePath image file path
     * @return instance of provided image file
     */
    public static Bitmap openExternalImageFile(String imagePath) {
        return BitmapFactory.decodeFile(imagePath);
    }

    public static String getCroppedImageName() {
        return (Constants.DEBUG_MODE ? TEST_IMAGE_NAME : CROPPED_IMAGE_NAME) + IMAGE_SUFFIX;
    }

    public static boolean doesFileExist(String filepath) {
        File file = new File(filepath);
        return file.exists();
    }

    /**
     * Reads the file loaded into the InputStream and writes it to the OutputStream. A byte array is
     * used for reading into and writing from.
     *
     * @param input source file
     * @param output destination file
     * @param bufferSize size of the byte array buffer
     * @throws IOException
     */
    private static void copyFile(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(
                    buffer, // byte array buffer
                    0,      // offset
                    read    // length
            );
        }
    }
}