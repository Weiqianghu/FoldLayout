package com.huweiqiang.customviewstudy.class8;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.huweiqiang.customviewstudy.R;
import com.nineoldandroids.view.ViewHelper;

/**
 * Description ${Desc}
 * Author huweiqiang
 * Date 2017/3/6.
 */

public class LeftDrawerLayout extends FrameLayout {

    private GestureDetectorCompat mGestureDetector;
    private ViewDragHelper mViewDragHelper;

    //水平拖拽的距离
    private int mRange;
    private int mMainLeft;
    private int mMenuWidth;
    private int mMenuHeight;

    //是否带有阴影效果
    private boolean isShowShadow = false;
    //手势处理类
    //滑动监听器
    private DragListener mDragListener;
    private ImageView mIvShadow;
    //左侧布局
    private RelativeLayout mMenuView;
    //右侧(主界面布局)
    private RelativeLayout mContentView;
    private Status mStatus;

    public LeftDrawerLayout(Context context) {
        this(context, null);
    }

    public LeftDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LeftDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        mGestureDetector = new GestureDetectorCompat(getContext(), new YScrollDetector());
        mViewDragHelper = ViewDragHelper.create(this, 1, new ViewDragCallback());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (isShowShadow) {
            mIvShadow = new ImageView(getContext());
            mIvShadow.setImageResource(R.drawable.girl);
            LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            addView(mIvShadow, 1, layoutParams);
        }
        mMenuView = (RelativeLayout) getChildAt(0);
        mContentView = (RelativeLayout) getChildAt(isShowShadow ? 2 : 1);
        mContentView.setClickable(true);
        mMenuView.setClickable(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMenuWidth = mMenuView.getMeasuredWidth();
        mMenuHeight = mMenuView.getMeasuredHeight();

        mRange = (int) (mMenuWidth * 0.6f);
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mMenuView.layout(0, 0, mMenuWidth, mMenuHeight);
        mContentView.layout(0, 0, mMainLeft + mMenuWidth, mMenuHeight);
    }

    public interface DragListener {
        //界面打开
        void onOpen();

        //界面关闭
        void onClose();

        //界面滑动过程中
        void onDrag(float percent);
    }

    private class YScrollDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
            return Math.abs(dy) <= Math.abs(dx);
        }
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (mMainLeft + dx < 0) {
                return 0;
            } else if (mMainLeft + dx > mRange) {
                return mRange;
            } else {
                return left;
            }
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mMenuWidth;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (xvel > 0) {
                open();
            } else if (xvel < 0) {
                close();
            } else if (releasedChild == mContentView && mMainLeft > mRange * 0.3) {
                open();
            } else if (releasedChild == mMenuView && mMainLeft > mRange * 0.7) {
                open();
            } else {
                close();
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView == mContentView) {
                mMainLeft = left;
            } else {
                mMainLeft += left;
            }

            if (mMainLeft < 0) {
                mMainLeft = 0;
            } else if (mMainLeft > mRange) {
                mMainLeft = mRange;
            }

            if (isShowShadow) {
                mIvShadow.layout(mMainLeft, 0, mMainLeft + mMenuWidth, mMenuHeight);
            }

            if (changedView == mMenuView) {
                mMenuView.layout(0, 0, mMenuWidth, mMenuHeight);
                mContentView.layout(mMainLeft, 0, mMainLeft + mMenuWidth, mMenuHeight);
            }
            dispatchDragEvent(mMainLeft);
        }
    }


    private void dispatchDragEvent(int mainLeft) {
        if (mDragListener == null) {
            return;
        }
        float percent = mainLeft / (float) mRange;
        //根据滑动的距离的比例,进行带有动画的缩小和放大View
        animateView(percent);
        //进行回调滑动的百分比
        mDragListener.onDrag(percent);
        Status lastStatus = mStatus;
        if (lastStatus != getStatus() && mStatus == Status.Close) {
            mDragListener.onClose();
        } else if (lastStatus != getStatus() && mStatus == Status.Open) {
            mDragListener.onOpen();
        }
    }

    /**
     * 根据滑动的距离的比例,进行带有动画的缩小和放大View
     *
     * @param percent
     */
    private void animateView(float percent) {
        float f1 = 1 - percent * 0.3f;
        //vg_main水平方向 根据百分比缩放
        ViewHelper.setScaleX(mContentView, f1);
        //vg_main垂直方向，根据百分比缩放
        ViewHelper.setScaleY(mContentView, f1);
        //沿着水平X轴平移
        ViewHelper.setTranslationX(mMenuView, -mMenuView.getWidth() / 2.3f + mMenuView.getWidth() / 2.3f * percent);
        //vg_left水平方向 根据百分比缩放
//        ViewHelper.setScaleX(mMenuView, 0.5f + 0.5f * percent);
//        //vg_left垂直方向 根据百分比缩放
//        ViewHelper.setScaleY(mMenuView, 0.5f + 0.5f * percent);
        //vg_left根据百分比进行设置透明度
//        ViewHelper.setAlpha(mMenuView, percent);
        if (isShowShadow) {
            //阴影效果视图大小进行缩放
            ViewHelper.setScaleX(mIvShadow, f1 * 1.4f * (1 - percent * 0.12f));
            ViewHelper.setScaleY(mIvShadow, f1 * 1.85f * (1 - percent * 0.12f));
        }
//        getBackground().setColorFilter(evaluate(percent, Color.BLACK, Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
    }

    private Integer evaluate(float fraction, Object startValue, Integer endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;
        int endInt = endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;
        return (startA + (int) (fraction * (endA - startA))) << 24
                | (startR + (int) (fraction * (endR - startR))) << 16
                | (startG + (int) (fraction * (endG - startG))) << 8
                | (startB + (int) (fraction * (endB - startB)));
    }

    public enum Status {
        Drag, Open, Close
    }

    public Status getStatus() {
        if (mMainLeft == 0) {
            mStatus = Status.Close;
        } else if (mMainLeft == mRange) {
            mStatus = Status.Open;
        } else {
            mStatus = Status.Drag;
        }
        return mStatus;
    }

    public void open() {
        open(true);
    }

    public void open(boolean animate) {
        if (animate) {
            //继续滑动
            if (mViewDragHelper.smoothSlideViewTo(mContentView, mRange, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            mContentView.layout(mRange, 0, mRange * 2, mMenuHeight);
            dispatchDragEvent(mRange);
        }
    }

    public void close() {
        close(true);
    }

    public void close(boolean animate) {
        if (animate) {
            //继续滑动
            if (mViewDragHelper.smoothSlideViewTo(mContentView, 0, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            mContentView.layout(0, 0, mMenuWidth, mMenuHeight);
            dispatchDragEvent(0);
        }
    }

    public void setDragListener(DragListener dragListener) {
        mDragListener = dragListener;
    }
}
