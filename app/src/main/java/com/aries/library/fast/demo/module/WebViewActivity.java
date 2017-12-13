package com.aries.library.fast.demo.module;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.aries.library.fast.manager.LoggerManager;
import com.aries.library.fast.module.activity.FastWebActivity;
import com.aries.ui.view.title.TitleBarView;
import com.just.library.AgentWeb;

/**
 * Created: AriesHoo on 2017/10/13 8:47
 * E-Mail: AriesHoo@126.com
 * Function: 应用内浏览器
 * Desc:
 */
public class WebViewActivity extends FastWebActivity {

    private static boolean mIsShowTitle = true;

    public static void start(Activity mActivity, String url) {
        start(mActivity, url, true);
    }

    public static void start(Activity mActivity, String url, boolean isShowTitle) {
        mIsShowTitle = isShowTitle;
        start(mActivity, WebViewActivity.class, url);
    }

    @Override
    protected int getProgressColor() {
        return super.getProgressColor();
    }

    @Override
    protected int getProgressHeight() {
        return super.getProgressHeight();
    }

    @Override
    public boolean isLightStatusBarEnable() {
        return mIsShowTitle;
    }

    @Override
    public int getContentBackground() {
        return -1;
    }

    @Override
    public void setTitleBar(TitleBarView titleBar) {
        if (!mIsShowTitle) {
            titleBar.setVisibility(View.GONE);
        }
        titleBar.setTitleMainTextMarquee(true)
                .setDividerVisible(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP);
    }

    @Override
    protected void setAgentWeb(AgentWeb mAgentWeb, AgentWeb.CommonAgentBuilder mAgentBuilder) {
        WebView mWebView = mAgentWeb.getWebCreator().get();
        mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                WebView.HitTestResult hitTestResult = mWebView.getHitTestResult();
                if (hitTestResult == null) {
                    return false;
                }
                if (!mIsShowTitle) {
                    showActionSheet();
                }
                LoggerManager.d("onLongClick:hitTestResult-Type:" + hitTestResult.getType() + ";Extra:" + hitTestResult.getExtra());
                return true;
            }
        });
    }

    @Override
    public void initView(Bundle savedInstanceState) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //横竖屏切换过后不能设置滑动返回--可以切换试一试效果
        if (mSwipeBackHelper != null) {
            mSwipeBackHelper.setSwipeBackEnable(false);
        }
    }
}
