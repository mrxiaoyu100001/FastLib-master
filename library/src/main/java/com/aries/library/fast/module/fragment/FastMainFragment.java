package com.aries.library.fast.module.fragment;

import android.support.v4.view.ViewPager;

import com.aries.library.fast.R;
import com.aries.library.fast.basis.BasisFragment;
import com.aries.library.fast.delegate.FastMainTabDelegate;
import com.aries.library.fast.i.IFastMainView;
import com.flyco.tablayout.listener.OnTabSelectListener;

/**
 * Created: AriesHoo on 2017/10/19 13:43
 * E-Mail: AriesHoo@126.com
 * Function: 快速创建主页布局
 * Description:
 */
public abstract class FastMainFragment extends BasisFragment implements IFastMainView, OnTabSelectListener {

    protected FastMainTabDelegate mFastMainTabDelegate;

    public void setViewPager(ViewPager mViewPager) {
    }

    @Override
    public boolean isSwipeEnable() {
        return false;
    }

    @Override
    public int getContentLayout() {
        return isSwipeEnable() ? R.layout.fast_activity_main_view_pager : R.layout.fast_activity_main;
    }

    @Override
    public void beforeInitView() {
        super.beforeInitView();
        mFastMainTabDelegate = new FastMainTabDelegate(mContentView, this, this);
    }

    @Override
    public void onTabReselect(int position) {

    }

    @Override
    public void onTabSelect(int position) {

    }
}
