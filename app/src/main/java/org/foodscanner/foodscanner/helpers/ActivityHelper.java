package org.foodscanner.foodscanner.helpers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import static com.google.common.base.Preconditions.checkNotNull;

public class ActivityHelper {

    public static void addFragmentToActivity (@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment, int containerId) {
        addFragmentToActivity(fragmentManager, fragment, containerId, null);
    }

    public static void addFragmentToActivity (@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment, int containerId, @Nullable String tag) {
        checkNotNull(fragmentManager);
        checkNotNull(fragment);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(containerId, fragment, tag);
        transaction.commit();
    }
}
