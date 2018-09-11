package org.foodscanner.foodscanner.scan;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import org.foodscanner.foodscanner.BasePresenter;
import org.foodscanner.foodscanner.BaseView;

public interface ScanContract {

    interface View extends BaseView<Presenter> {

        void showSnackBar(String message, int duration);

        void showMain();

        void showOcrProgress();

        void updateOcrProgressPercentage(int percentage);

        void showResult();

        void onWordClick(int wordIndex);

        void changePage(int page);

        AppCompatActivity getParentActivity();
    }

    interface PagerView extends BaseView<Presenter> { }

    interface Presenter extends BasePresenter {

        void handleActivityResult(int requestCode, int resultCode, Intent data);

        void startCameraActivity();

        void startCropActivity();

        void startOcrTask();

        void cancelOcrTask();

        void startParseOcrResultTask(String hocrText);

        void startAdditiveRecognitionTask();

        void changePage(int page);

        FragmentManager getFragmentManager();

    }

}
