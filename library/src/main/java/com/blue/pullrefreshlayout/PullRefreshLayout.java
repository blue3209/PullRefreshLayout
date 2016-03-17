package com.blue.pullrefreshlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

/**
 * PullRefresh
 * Desc:下拉刷新控件,参考SwipeRefreshLayout.
 * Author:chengli3209@gmail.com Date:2016/3/11 14:20
 */
public class PullRefreshLayout extends ViewGroup {
    private static final String TAG = "PullRefreshLayout";
    private static final int[] LAYOUT_ATTRS = new int[]{
            android.R.attr.enabled
    };

    /**
     * 下拉刷新的临界值最低96dp
     */
    private static final float DRAG_DISTANCE = 64f;

    /**
     * Header头部的类型，正常/覆盖/悬浮
     */
    public enum HeaderType {
        NORMAL, OVERLAY, ABOVE, NONE
    }

    private float mTargetFinalOffset;
    private int mOriginalOffsetTop;
    /**
     * 移动距离临界值，移动距离小于此值认为没有滑动
     */
    private int mTouchSlop;
    /**
     * 刷新的总距离
     */
    private int mTotalDragDistance;

    /**
     * 动画时间
     */
    private long mAnimationDuration;
    /**
     * Header的类型
     */
    private HeaderType mHeaderType = HeaderType.NORMAL;

    /**
     * 目标View，一般指可滚动试图
     */
    private View mTargetView;
    /**
     * 头部View
     */
    private PullRefreshHeader mHeaderView;
    /**
     * 包含头部View的Container
     */
    private RelativeLayout mHeaderContainerView;
    private int mHeaderViewHeight;
    private int mHeaderViewContainerIndex;

    private boolean mRefreshing = false;

    public PullRefreshLayout(Context context) {
        this(context, null);
    }

    public PullRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PullRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        mDecelerateInterpolator = new DecelerateInterpolator(2f);

        setWillNotDraw(false);
        final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));
        a.recycle();
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);

        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mHeaderViewHeight = mTotalDragDistance = (int) (DRAG_DISTANCE * metrics.density);
        createHeaderContainer();

        mTargetFinalOffset = mHeaderViewHeight * 1.0f;
        mOriginalOffsetTop = -mHeaderViewHeight;
    }

    /**
     * 创建Header容器布局
     */
    private void createHeaderContainer() {
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, mHeaderViewHeight * 3);
        mHeaderContainerView = new RelativeLayout(getContext());

        mHeaderContainerView.setVisibility(View.GONE);
        this.addView(mHeaderContainerView, lp);

        mHeaderView = new PullRefreshHeaderView(getContext());
        setHeaderView(mHeaderView);
    }

    /**
     * 设置Header的类型
     *
     * @param headerType
     */
    public void setHeaderType(HeaderType headerType) {
        if (this.getChildCount() == 0) {
            return;
        }

        if (mHeaderType == headerType) {
            return;
        }

        if (headerType == HeaderType.ABOVE || (mHeaderType == HeaderType.ABOVE && headerType != mHeaderType.ABOVE)) {
            setRefreshing(false);
            View target = mTargetView;
            View header = mHeaderContainerView;
            this.removeAllViews();

            if (headerType == HeaderType.ABOVE) {
                this.addView(target);
                this.addView(header);
            } else {
                this.addView(header);
                this.addView(target);
            }
            this.mHeaderType = headerType;
            requestLayout();
        }
        this.mHeaderType = headerType;
        setHeaderView(mHeaderView);
    }

    /**
     * 设置自定义HeaderView必须实现PullRefreshHeader接口
     *
     * @param headerView
     */
    public void setHeaderView(PullRefreshHeader headerView) {
        if (null == headerView || null == headerView.getRefreshHeader()) {
            return;
        }

        if (null == mHeaderContainerView) {
            return;
        }

        this.mHeaderView = headerView;
        mHeaderContainerView.removeAllViews();
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, mHeaderViewHeight);
        if (mHeaderType != HeaderType.OVERLAY) {
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        } else {
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }
        mHeaderContainerView.addView(headerView.getRefreshHeader(), lp);
    }

    /**
     * 设置Header的背景
     *
     * @param resource
     */
    public void setHeaderBackground(int resource) {
        if (null == mHeaderContainerView || resource <= 0) {
            return;
        }

        mHeaderContainerView.setBackgroundResource(resource);
    }

    /**
     * 设置下拉刷新的距离
     *
     * @param totalDragDistance
     */
    public void setTotalDragDistance(int totalDragDistance) {
        if (totalDragDistance > 0) {
            this.mTotalDragDistance = totalDragDistance;
        }
    }

    /**
     * 查找TargetView
     */
    private void ensureTarget() {
        if (null == mTargetView) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(mHeaderContainerView)) {
                    mTargetView = child;
                    break;
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ensureTarget();
        if (null == mTargetView) {
            return;
        }

        mTargetView.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() -
                        getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight() -
                        getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));

        if (null == mHeaderContainerView) {
            return;
        }

        mHeaderContainerView.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() -
                        getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mHeaderViewHeight * 3, MeasureSpec.EXACTLY));

        mHeaderViewContainerIndex = -1;
        // 获取HeaderContainer的位置
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) == mHeaderContainerView) {
                mHeaderViewContainerIndex = index;
                break;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() == 0) {
            return;
        }

        int offset = mCurrentTargetOffsetTop;
        if (null != mListener || null != mHeaderView) {
            float rate = Math.abs(offset) / (mTotalDragDistance * 2f);

            if (null != mListener) {
                mListener.onRefreshDistance(Math.abs(offset), rate);
            }

            if (null != mHeaderView) {
                mHeaderView.onRefreshDistance(Math.abs(offset), rate);
            }
        }

        switch (mHeaderType) {
            case NORMAL:
                layoutHeaderContainerView(offset);
                layoutTargetView(offset);
                break;
            case OVERLAY:
                layoutHeaderContainerView(0);
                layoutTargetView(offset);
                break;
            case ABOVE:
                layoutTargetView(0);
                layoutHeaderContainerView(offset);
                break;
            case NONE:
                layoutTargetView(0);
                break;
        }
    }

    /**
     * 布局HeaderContainerView
     *
     * @param offset
     */
    private void layoutHeaderContainerView(int offset) {
        if (null == mHeaderContainerView) {
            return;
        }

        int headerWidth = mHeaderContainerView.getMeasuredWidth();
        int headerHeight = mHeaderContainerView.getMeasuredHeight();

        int top = offset;
        if (mHeaderType == HeaderType.NORMAL || mHeaderType == HeaderType.ABOVE) {
            top = -headerHeight + offset;
        }
        mHeaderContainerView.layout(0, top, headerWidth, headerHeight + top);

    }

    /**
     * 布局TargetView
     *
     * @param offset
     */
    private void layoutTargetView(int offset) {
        ensureTarget();
        if (null == mTargetView) {
            return;
        }

        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();

        final View child = mTargetView;
        int childLeft = getPaddingLeft();
        int childTop = getPaddingTop() + offset;
        int childWidth = width - getPaddingLeft() - getPaddingRight();
        int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
    }

    /**
     * 判断TargetView是否可以向上滚动
     *
     * @return
     */
    public boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTargetView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTargetView;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mTargetView, -1) || mTargetView.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTargetView, -1);
        }
    }

    private static final int INVALID_POINTER = -1;
    private float mInitialMotionY;
    private float mInitialDownY;
    private boolean mIsBeingDragged;
    private int mActivePointerId = INVALID_POINTER;
    private static final float DRAG_RATE = 0.5f;

    private int mCurrentTargetOffsetTop = 0;

    /**
     * 主要判断是否应该拦截子View的事件<br>
     * 如果拦截，则交给自己的OnTouchEvent处理<br>
     * 否者，交给子View处理<br>
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();
        final int action = MotionEventCompat.getActionMasked(ev);

        if (!isEnabled() || null == mTargetView || canChildScrollUp() || mRefreshing) {
            // 如果子View可以滑动，不拦截事件，交给子View处理-下拉刷新
            // 或者子View没有滑动到底部不拦截事件-上拉加载更多
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setTargetOffsetTopAndBottom(0, true);
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                final float initialDownY = getMotionEventY(ev, mActivePointerId);
                if (initialDownY == -1) {
                    return false;
                }
                //记录首次按下去的位置
                mInitialDownY = initialDownY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }

                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }

                //TODO 可在此处做上拉处理判断，ps:mTarget滚动到底部
                final float yDiff = y - mInitialDownY;
                if (!mIsBeingDragged && yDiff > mTouchSlop) {
                    //有效滚动
                    if (yDiff < 0) {
                        return false;
                    }
                    mInitialMotionY = mInitialDownY + mTouchSlop;
                    //正在下拉
                    mIsBeingDragged = true;
                }
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled() || null == mTargetView || canChildScrollUp() || mRefreshing) {
            // 如果子View可以滑动，不拦截事件，交给子View处理-下拉刷新
            // 或者子View没有滑动到底部不拦截事件-上拉加载更多
            return false;
        }
        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex = -1;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                break;
            case MotionEvent.ACTION_MOVE: {
                pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }

                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float distance = (y - mInitialMotionY) * DRAG_RATE;
                if (mIsBeingDragged) {
                    if (distance < 0) {
                        return false;
                    }
                    moveTarget(distance);
                }
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                pointerIndex = MotionEventCompat.getActionIndex(ev);
                if (pointerIndex < 0) {
                    return false;
                }
                mActivePointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_UP:
                pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }

                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float distance = (y - mInitialMotionY) * DRAG_RATE;
                mIsBeingDragged = false;
                //刷新完毕
                if (!mRefreshing) {
                    finishTarget(distance);
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            case MotionEvent.ACTION_CANCEL:
                return false;
        }
        return true;
    }

    /**
     * 移动Target
     *
     * @param distance
     */
    private void moveTarget(float distance) {
        float originalDragPercent = distance / mTotalDragDistance;
        float dragPercent = Math.min(1f, Math.abs(originalDragPercent));

        float extraOS = distance > 0 ? Math.abs(distance) - mTotalDragDistance : mTotalDragDistance - Math.abs(distance);

        float slingshotDist = mTargetFinalOffset;
        float tensionSlingshotPercent = Math.max(0, Math.min(extraOS, slingshotDist * 2) / slingshotDist);
        float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow((tensionSlingshotPercent / 4), 2)) * 2f;
        float extraMove = (slingshotDist) * tensionPercent * 2;

        int targetY = (int) ((slingshotDist * dragPercent) + extraMove);
        if (mHeaderContainerView.getVisibility() != View.VISIBLE) {
            mHeaderContainerView.setVisibility(View.VISIBLE);
        }

        if (null != mHeaderView) {
            mHeaderView.onRefreshStart();
        }

        int offset = targetY - mCurrentTargetOffsetTop;
        setTargetOffsetTopAndBottom(offset, true);
    }

    /**
     * 停止Target
     *
     * @param distance
     */
    private void finishTarget(float distance) {
        if (distance > mTotalDragDistance) {
            //滑动距离大于，处于刷新状态
            setRefreshing(true, true);
        } else {
            mRefreshing = false;
            animateOffsetToStartPosition(mCurrentTargetOffsetTop, null);
        }
    }

    private static final int ANIMATE_TO_TRIGGER_DURATION = 500;
    private static final int ANIMATE_TO_START_DURATION = 500;

    private static final int MSG_AUTO = 0x01;
    private static final int MSG_RESET = 0x02;

    private int mFrom = 0;
    private boolean mNotify;
    private OnRefreshListener mListener;
    private DecelerateInterpolator mDecelerateInterpolator = null;
    private Animation.AnimationListener mRefreshListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            if (mRefreshing) {
                if (null != mHeaderView) {
                    mHeaderView.onRefresh();
                }
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mRefreshing) {
                //开始刷新动画
                if (mNotify) {
                    if (mListener != null) {
                        mListener.onRefresh();
                    }
                }
            } else {
                //停止刷新
                mHeaderContainerView.setVisibility(View.GONE);
                setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCurrentTargetOffsetTop, true);
            }
            mNotify = false;
            mCurrentTargetOffsetTop = getTargetOffsetTop();
        }
    };

    public int getTargetOffsetTop() {
        int offsetTop = 0;
        switch (mHeaderType) {
            case NORMAL:
            case OVERLAY:
                offsetTop = mTargetView.getTop();
                break;
            case ABOVE:
                offsetTop = mHeaderContainerView.getHeight() + mHeaderContainerView.getTop();
                break;
            case NONE:
                offsetTop = 0;
                break;
        }
        return offsetTop;
    }

    /**
     * onRefreshListener
     *
     * @param onRefreshListener
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.mListener = onRefreshListener;
    }

    /**
     * 自动刷新
     */
    public void setAutoRefresh() {
        if (null != mHandler) {
            if (null != mHeaderView) {
                mHeaderContainerView.setVisibility(View.VISIBLE);
                mHeaderView.onRefreshStart();
            }
            if (mHandler.hasMessages(MSG_AUTO)) {
                mHandler.removeMessages(MSG_AUTO);
            }
            mHandler.sendEmptyMessageDelayed(MSG_AUTO, 500);
        }
    }

    /**
     * 结束刷新
     */
    public void completeRefresh() {
        if (null != mHeaderView) {
            mHeaderView.onRefreshComplete();
        }
        setRefreshing(false, false);
    }

    /**
     * 设置刷新状态
     *
     * @param refreshing
     */
    public void setRefreshing(boolean refreshing) {
        if (refreshing && mRefreshing != refreshing) {
            setAutoRefresh();
        } else {
            completeRefresh();
        }
    }

    /**
     * 设置刷新状态
     *
     * @param refreshing
     * @param notify     是否通知
     */
    private void setRefreshing(boolean refreshing, final boolean notify) {
        if (mRefreshing != refreshing) {
            mNotify = notify;
            ensureTarget();
            mRefreshing = refreshing;
            if (mRefreshing) {
                animateOffsetToCorrectPosition(mCurrentTargetOffsetTop, mRefreshListener);
            } else {
                animateOffsetToStartPosition(mCurrentTargetOffsetTop, mRefreshListener);
            }

            setOnTargetScrollListener();
        }
    }

    /**
     * 设置Target的TopAndBottom
     *
     * @param offset
     * @param requiresUpdate
     */
    private void setTargetOffsetTopAndBottom(int offset, boolean requiresUpdate) {
        if (mHeaderType == HeaderType.ABOVE || mHeaderType == HeaderType.NORMAL) {
            mHeaderContainerView.bringToFront();
            mHeaderContainerView.offsetTopAndBottom(offset);
        }

        if (mHeaderType == HeaderType.NORMAL || mHeaderType == HeaderType.OVERLAY) {
            mTargetView.bringToFront();
            mTargetView.offsetTopAndBottom(offset);
        }

        mCurrentTargetOffsetTop = getTargetOffsetTop();
        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
            //API11以下invalidate不回调onLayout
            invalidate();
            requestLayout();
        }
    }

    /**
     * 从当前mCurrentTargetOffsetTop移动Target到起始位置的动画
     *
     * @param from
     * @param listener
     */
    private void animateOffsetToStartPosition(int from, Animation.AnimationListener listener) {
        mFrom = from;
        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(ANIMATE_TO_START_DURATION);
        mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);

        if (null == listener) {
            mAnimateToStartPosition.setAnimationListener(listener);
        }

        clearAnimation();
        startAnimation(mAnimateToStartPosition);
        resetTargetLayoutDelay(mAnimationDuration);
    }

    /**
     * 从当前mCurrentTargetOffsetTop移动Target到刷新位置的动画
     *
     * @param from
     * @param listener
     */
    private void animateOffsetToCorrectPosition(int from, Animation.AnimationListener listener) {
        mFrom = from;
        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        if (listener != null) {
            mAnimateToCorrectPosition.setAnimationListener(listener);
        }

        clearAnimation();
        startAnimation(mAnimateToCorrectPosition);
    }

    /**
     * 移动到刷新位置
     *
     * @param from
     * @param listener
     */
    private void animateOffsetToRefreshPosition(int from, Animation.AnimationListener listener) {
        mFrom = from;
        mAnimateToRefreshPosition.reset();
        mAnimateToRefreshPosition.setDuration(ANIMATE_TO_START_DURATION);
        mAnimateToRefreshPosition.setInterpolator(mDecelerateInterpolator);
        if (null != listener) {
            mAnimateToRefreshPosition.setAnimationListener(listener);
        }

        clearAnimation();
        startAnimation(mAnimateToRefreshPosition);
    }

    /**
     * 移动Target到起始位置的动画
     */
    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = (int) (mFrom * (1 - interpolatedTime));
            int offset = targetTop - getTargetOffsetTop();
            setTargetOffsetTopAndBottom(offset, false);
        }
    };

    /**
     * 移动Target到刷新位置的动画
     */
    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = (int) ((mFrom + (mTargetFinalOffset - mFrom) * interpolatedTime));
            int offset = targetTop - getTargetOffsetTop();
            setTargetOffsetTopAndBottom(offset, false);
        }
    };

    /**
     * 移动到刷新位置
     */
    private final Animation mAnimateToRefreshPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = (int) ((mFrom + (mTargetFinalOffset - mFrom) * interpolatedTime));
            int offset = targetTop - getTargetOffsetTop();
            setTargetOffsetTopAndBottom(offset, false);
        }
    };

    /**
     * 重置Target的位置
     *
     * @param delay
     */
    private void resetTargetLayoutDelay(long delay) {
        if (null != mHandler) {
            if (mHandler.hasMessages(MSG_RESET)) {
                mHandler.removeMessages(MSG_RESET);
            }
            mHandler.sendEmptyMessageDelayed(MSG_RESET, delay);
        }
    }

    /**
     * 打印日志
     *
     * @param message
     */
    private static void LOG(String message) {
        if (null == message) {
            return;
        }
        Log.e(TAG, message);
    }

    /**
     * 设置TargetView的滚动事件
     */
    private void setOnTargetScrollListener() {
        if (null == mTargetView || !mRefreshing) {
            return;
        }
    }

    /**
     * 刷新回调接口
     */
    public interface OnRefreshListener {
        void onRefresh();

        void onRefreshDistance(int distance, float rate);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_AUTO:
                    ensureTarget();
                    if (null == mTargetView) {
                        return;
                    }
                    mRefreshing = true;
                    mNotify = true;
                    animateOffsetToRefreshPosition(0, mRefreshListener);
                    break;
                case MSG_RESET:
                    layoutTargetView(0);
                    layoutHeaderContainerView(0);
                    break;
            }
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages("");
        }
        mHandler = null;
    }
}
