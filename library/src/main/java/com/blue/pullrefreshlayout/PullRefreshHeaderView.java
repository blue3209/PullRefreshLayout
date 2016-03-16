package com.blue.pullrefreshlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * PullRefreshHeader
 * Desc:自定义默认HeaderView的接口.
 * Author:chengli3209@gmail.com Date:2016/3/14 13:28
 */
public class PullRefreshHeaderView extends RelativeLayout implements PullRefreshHeader {
    static final int DEFAULT_ROTATION_ANIMATION_DURATION = 150;

    private TextView tvTextView;
    private ProgressBar mProgressBar;
    private ImageView mIndicatorImgeView;

    private Animation mRotateAnimation, mResetRotateAnimation;

    public PullRefreshHeaderView(Context context) {
        this(context, null);
    }

    public PullRefreshHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PullRefreshHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.layout_pullrefresh_header_view, this);

        tvTextView = (TextView) findViewById(R.id.textView);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_pullrefresh);
        mIndicatorImgeView = (ImageView) findViewById(R.id.iv_pullrefresh_indicator);

        final Interpolator interpolator = new LinearInterpolator();
        mRotateAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        mRotateAnimation.setInterpolator(interpolator);
        mRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
        mRotateAnimation.setFillAfter(true);

        mResetRotateAnimation = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mResetRotateAnimation.setInterpolator(interpolator);
        mResetRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
        mResetRotateAnimation.setFillAfter(true);
    }

    @Override
    public View getRefreshHeader() {
        return this;
    }

    private boolean mRefreshStart;
    private boolean mRefreshComplete;

    @Override
    public void onRefreshStart() {
        this.mRefreshStart = false;
        this.mRefreshComplete = false;
        mIndicatorImgeView.setVisibility(View.VISIBLE);
        ((View) mProgressBar.getParent()).setVisibility(View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        mIndicatorImgeView.clearAnimation();
        this.mRefreshStart = true;
        this.mRefreshComplete = false;
        tvTextView.setText(R.string.pullrefresh_refreshing);
        mIndicatorImgeView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        ((View) mProgressBar.getParent()).setVisibility(View.VISIBLE);
    }

    @Override
    public void onRefreshComplete() {
        mIndicatorImgeView.clearAnimation();
        this.mRefreshStart = false;
        this.mRefreshComplete = true;
        tvTextView.setText(R.string.pullrefresh_complete);
        mProgressBar.setVisibility(View.GONE);
        ((View) mProgressBar.getParent()).setVisibility(View.GONE);
    }

    private boolean isReset = false;

    @Override
    public void onRefreshDistance(int distance, float rate) {
        if (mRefreshStart || mRefreshComplete) {
            return;
        }
        if (rate < 0.5) {
            tvTextView.setText(R.string.pullrefresh_pull_refresh);
            if (isReset) {
                mIndicatorImgeView.startAnimation(mResetRotateAnimation);
                isReset = false;
            }
        } else {
            tvTextView.setText(R.string.pullrefresh_release_refresh);
            if (!isReset) {
                mIndicatorImgeView.startAnimation(mRotateAnimation);
                isReset = true;
            }
        }
    }
}
