package org.foodscanner.foodscanner.scan;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.foodscanner.foodscanner.Constants;
import org.foodscanner.foodscanner.R;
import org.foodscanner.foodscanner.adapters.FragmentAdapter;
import org.foodscanner.foodscanner.data.MultilineWord;
import org.foodscanner.foodscanner.data.Result;
import org.foodscanner.foodscanner.data.Word;
import org.foodscanner.foodscanner.helpers.AdditiveMarker;
import org.foodscanner.foodscanner.helpers.FileOperations;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ScanFragment extends Fragment implements ScanContract.View {

    private ScanContract.Presenter mPresenter;
    private TextView mOcrProgress;
    private Button mCancelOcrProcess;
    private TextView mNoDatabaseWarning;
    private FloatingActionButton mCameraButton;
    private List<Word> mMarkedWords;
    private ScanWordPager mScanWordPager;
    private boolean mIsWordView;

    public static ScanFragment newInstance() {
        return new ScanFragment();
    }

    public ScanFragment() {
        mIsWordView = false;
    }

    public boolean getIsWordView() {
        return mIsWordView;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
     }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeMain();
    }

    @Override
    public void setPresenter(ScanContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    /**
     * Shows a snackbar with the specified message and duration
     *
     * @param message Message to display.
     * @param duration Display duration in milliseconds or default values ({@link
     * Snackbar#LENGTH_LONG}, {@link Snackbar#LENGTH_SHORT}, {@link Snackbar#LENGTH_INDEFINITE}).
     */
    @Override
    public void showSnackBar(String message, int duration) {
        Snackbar.make(checkNotNull(getView()), message, duration).show();
    }

    @Override
    public void showMain() {
        setViewVisible(R.id.scan_main_view);
    }

    @Override
    public void showOcrProgress() {
        setViewVisible(R.id.scan_ocrprogress_view);
        updateOcrProgressPercentage(0);
    }

    @Override
    public void updateOcrProgressPercentage(int percentage) {
        if (percentage < 0 || percentage > 100) {
            Log.e(Constants.TAG, String.format("updateOcrProgressPercentage received an invalid value - %1$d", percentage));
            throw new IllegalArgumentException();
        }

        mOcrProgress.setText(getResources().getString(R.string.ocr_progress_percentage, percentage));
    }

    @Override
    public void showResult() {
        setViewVisible(R.id.scan_ocrresult_view);
        initializeResultView();
    }

    @Override
    public void onWordClick(int wordIndex) {
        setViewVisible(R.id.scan_word_view);
        initializeWordView();
        mScanWordPager.setCurrentPage(wordIndex, false);
    }

    @Override
    public void changePage(int page) {
        mScanWordPager.setCurrentPage(page, true);
    }

    @Override
    public AppCompatActivity getParentActivity() {
        return (AppCompatActivity) getActivity();
    }

    private void setViewVisible(int viewId) {
        ConstraintLayout root = checkNotNull(getView()).findViewById(R.id.scan_root);
        View targetView = null;

        for (int i = 0; i < root.getChildCount(); i ++) {
            View child = root.getChildAt(i);
            if (child instanceof ConstraintLayout) {
                if (child.getId() == viewId) {
                    targetView = child;
                    continue;
                }
                child.setVisibility(View.INVISIBLE);
            }
        }

        checkNotNull(targetView).setVisibility(View.VISIBLE);
        mCameraButton.setVisibility(targetView.getId() == R.id.scan_main_view ? View.VISIBLE : View.INVISIBLE);

        mIsWordView = targetView.getId() == R.id.scan_word_view;
    }

    private void initializeMain() {
        Log.d(Constants.TAG, "Initializing ScanFragment");
        View view = checkNotNull(getView());
        mOcrProgress = view.findViewById(R.id.tesseract_progress_percentage);

        mCameraButton = view.findViewById(R.id.start_camera_float_button);
        mCameraButton.setOnClickListener((View v) -> {
            if (Constants.DEBUG_MODE) {
                showOcrProgress();
                Result.getInstance().reset();
                mPresenter.startOcrTask();
                return;
            }
            mPresenter.startCameraActivity();
        });

        mNoDatabaseWarning = view.findViewById(R.id.main_no_database_warn);
        if (Constants.IS_ADDITIVE_DATABASE_ENABLED != 1) {
            mNoDatabaseWarning.setVisibility(View.VISIBLE);
        } else {
            mNoDatabaseWarning.setVisibility(View.INVISIBLE);
        }

        mCancelOcrProcess = view.findViewById(R.id.tesseract_progress_too_long_cancel_btn);
        mCancelOcrProcess.setOnClickListener((View v) -> {
            mPresenter.cancelOcrTask();
        });
    }

    private void initializeResultView() {
        initializeFinishButton();
        mMarkedWords = Result.getInstance().getWordList();
        ImageView resultImageView = checkNotNull(getView()).findViewById(R.id.result_ingredients_image);
        setResultImage(resultImageView);
        setTouchListener(resultImageView);
        ImageView overlayImageView = checkNotNull(getView()).findViewById(R.id.result_ingredients_image_overlay);
        markAdditives(overlayImageView, resultImageView);
    }

    /**
     * Initializes the finish button and sets an on click listener to it which resets the {@code
     * Result} data and shows the main (initial) fragment view.
     */
    private void initializeFinishButton() {
        Button finishButton = checkNotNull(getView()).findViewById(R.id.result_finish_button);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Result.getInstance().reset();
                mScanWordPager = null;
                showMain();
            }
        });
    }

    /**
     * Sets the cropped (result) image as the background of the result {@code ImageView}.
     *
     * @param resultImageView target {@code ImageView}
     */
    private void setResultImage(ImageView resultImageView) {
        Bitmap croppedImage = FileOperations.openExternalImageFile(
                getContext(),
                FileOperations.getCroppedImageName()
        );
        resultImageView.setImageBitmap(croppedImage);
    }

    private void setTouchListener(ImageView resultImageView) {
        resultImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    float[] xy = new float[2];
                    xy[0] = motionEvent.getX();
                    xy[1] = motionEvent.getY();

                    // Transform coordinates of scaled image to coordinates of the original image
                    Matrix invertMatrix = new Matrix();
                    ((ImageView)view).getImageMatrix().invert(invertMatrix);
                    invertMatrix.mapPoints(xy);

                    int clickedWordIndex = getTouchedWord(xy[0], xy[1]);
                    if (clickedWordIndex != -1) {
                        onWordClick(clickedWordIndex);
                    }
                }
                return true;
            }
        });
    }

    private int getTouchedWord(float x, float y) {
        for (int i = 0; i < mMarkedWords.size(); i++) {
            Word w = mMarkedWords.get(i);
            if (w instanceof MultilineWord) {
                for (Word line : ((MultilineWord) w).getAllLines()) {
                    if (line.getBoundingBox().areCoordinatesInsideBox(x, y)) {
                        return i;
                    }
                }
            } else {
                if (w.getBoundingBox().areCoordinatesInsideBox(x, y)) {
                    return i;
                }
            }
        }

        return -1;
    }

    private void markAdditives(ImageView overlayImageView, ImageView resultImageView) {
        AdditiveMarker additiveMarker = new AdditiveMarker(getContext(), overlayImageView, resultImageView);
        additiveMarker.markAdditives();
    }

    private void initializeWordView() {
        if (mScanWordPager != null) {
            return;
        }

        ViewPager viewPager = checkNotNull(getView()).findViewById(R.id.result_view_pager);
        mScanWordPager = new ScanWordPager(new FragmentAdapter(getFragmentManager(), Result.getInstance().getResultSize()), viewPager);
        mScanWordPager.setPresenter(checkNotNull(mPresenter));
    }
}
