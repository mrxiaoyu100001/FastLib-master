package com.aries.library.fast.demo.module.main.sample.ali;

import android.graphics.Color;
import android.util.TypedValue;

import com.aries.library.fast.demo.R;
import com.aries.library.fast.module.fragment.FastTitleFragment;
import com.aries.ui.view.title.TitleBarView;


/**
 * Created: AriesHoo on 2017/8/5 23:06
 * Function: 支付宝-BaseFragment
 * Desc:
 */
public abstract class ALiPayBaseFragment extends FastTitleFragment {
    String[] titles;

    @Override
    public boolean isLightStatusBarEnable() {
        return false;
    }

    @Override
    public void beforeSetTitleBar(TitleBarView titleBar) {
        super.beforeSetTitleBar(titleBar);
        titles = getResources().getStringArray(R.array.arrays_tab_ali);
        titleBar.setStatusAlpha(75)
                .setLeftTextColor(Color.WHITE)
                .setRightTextColor(Color.WHITE)
                .setLeftTextSize(TypedValue.COMPLEX_UNIT_DIP, 16)
                .setRightTextSize(TypedValue.COMPLEX_UNIT_DIP, 16)
                .setBackgroundResource(R.color.colorMainAli);
    }

    @Override
    public int getContentBackground() {
        return -1;
    }
}
