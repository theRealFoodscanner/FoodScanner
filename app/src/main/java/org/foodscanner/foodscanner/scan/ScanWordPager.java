package org.foodscanner.foodscanner.scan;

import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;

import org.foodscanner.foodscanner.adapters.FragmentAdapter;
import org.foodscanner.foodscanner.data.Result;

import static com.google.common.base.Preconditions.checkNotNull;

public class ScanWordPager implements ScanContract.PagerView,
        ViewPager.OnPageChangeListener {
    private FragmentAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private ScanContract.Presenter mPresenter;


    ScanWordPager(FragmentAdapter pagerAdapter, ViewPager viewPager) {
        mViewPager = viewPager;
        mViewPager.addOnPageChangeListener(this);
        createNewPager(pagerAdapter);
    }

    @Override
    public void setPresenter(ScanContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onPageSelected(int position) {
        mPresenter.changePage(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

    @Override
    public void onPageScrollStateChanged(int state) { }

    public void increaseAmountOfDetailsFragments(int position) {
        // Create and set new adapter due to Android bug
        createNewPager(null);
        mViewPager.setCurrentItem(position);
    }

    public void mergeTwoDetailsFragments(int position, int adjacentPosition) {
        // Create and set new adapter due to Android bug
        createNewPager(null);
        int newPosition = Math.min(position, adjacentPosition);
        mViewPager.setCurrentItem(newPosition);
    }

    public void setCurrentPage(int page, boolean smoothScroll) {
        mViewPager.setCurrentItem(page, smoothScroll);
        mPagerAdapter.onPageChanged(page);
    }

    private void createNewPager(@Nullable FragmentAdapter fragmentAdapter) {
        mPagerAdapter = fragmentAdapter != null ? fragmentAdapter :
                                                  new FragmentAdapter(
                                                          mPresenter.getFragmentManager(),
                                                          Result.getInstance().getResultSize()
                                                  );

        mPagerAdapter.setPager(this);
        mViewPager.setAdapter(mPagerAdapter);
    }
}