package org.foodscanner.foodscanner.adapters;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import org.foodscanner.foodscanner.scan.ScanDetailsFragment;
import org.foodscanner.foodscanner.scan.ScanWordPager;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Fragment Adapter for holding all the details fragments.
 */
public class FragmentAdapter extends FragmentStatePagerAdapter {

    public static final String TAG = "FoodScanner-PagerAdaptr";
    private List<ScanDetailsFragment> mFragments;
    private ScanWordPager mPager;

    public FragmentAdapter(FragmentManager fragmentManager, int size) {
        super(fragmentManager);
        // Initialize Fragment list
        mFragments = new ArrayList<>();
        // Fill Fragment list with pre-created Fragments
        fillFragmentArrayList(size);
    }

    @Override
    public Fragment getItem(int position) {
        if (position < mFragments.size()){
            if (mFragments.get(position) == null) {
                mFragments.set(position, ScanDetailsFragment.newInstance(this, position));
            }
            return mFragments.get(position);
        }

        Log.e(TAG, "Position is larger than number of fragments - (" + position + " > " + mFragments.size() + ").");
        return null;
    }

    @Override
    public int getItemPosition(Object object) {
        ScanDetailsFragment fragment = (ScanDetailsFragment) object;
        if (mFragments.contains(fragment)) {
            return mFragments.indexOf(fragment);
        }
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public Parcelable saveState() {
        return super.saveState();
    }

    public void onPageChanged(int page) {
        mFragments.get(page).onPageChanged();
    }

    public void setPager(ScanWordPager pager) {
        mPager = checkNotNull(pager);
    }

    public ScanWordPager getPager() {
        return mPager;
    }

    /**
     * Adds specified amount of {@code null} values to the {@code Fragment ArrayList}.
     *
     * @param amount specifies amount of {@code null} values to add to the list
     */
    private void fillFragmentArrayList(int amount) {
        for (int i = 0; i < amount; i++) {
            mFragments.add(ScanDetailsFragment.newInstance(this, i));
        }
        notifyDataSetChanged();
    }
}
