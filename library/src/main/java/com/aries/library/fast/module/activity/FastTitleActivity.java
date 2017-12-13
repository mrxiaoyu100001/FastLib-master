package com.aries.library.fast.module.activity;

import android.view.View;

import com.aries.library.fast.FastConfig;
import com.aries.library.fast.basis.BasisActivity;
import com.aries.library.fast.delegate.FastTitleDelegate;
import com.aries.library.fast.i.IFastTitleView;
import com.aries.ui.view.title.TitleBarView;

/**
 * Created: AriesHoo on 2017/7/3 16:04
 * Function: title 基类
 * Desc:
 */

public abstract class FastTitleActivity extends BasisActivity implements IFastTitleView {

    protected FastTitleDelegate mFastTitleDelegate;
    protected TitleBarView mTitleBar;

    @Override
    public void beforeSetTitleBar(TitleBarView titleBar) {

    }

    @Override
    public int getLeftIcon() {
        return FastConfig.getInstance(this).getTitleConfig().getLeftTextDrawable();
    }

    @Override
    public View.OnClickListener getLeftClickListener() {
        if (FastConfig.getInstance(this).getTitleConfig().isLeftTextFinishEnable()) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            };
        } else {
            return null;
        }
    }

    @Override
    public boolean isLightStatusBarEnable() {
        return FastConfig.getInstance(this).getTitleConfig().isLightStatusBarEnable();
    }

    @Override
    public void beforeInitView() {
        super.beforeInitView();
        mFastTitleDelegate = new FastTitleDelegate(mContentView, mContext, this);
        mTitleBar = mFastTitleDelegate.mTitleBar;
    }
}
