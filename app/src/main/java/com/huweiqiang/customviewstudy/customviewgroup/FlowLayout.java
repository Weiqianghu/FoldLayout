package com.huweiqiang.customviewstudy.customviewgroup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huweiqiang on 2017/3/29.
 */

public class FlowLayout extends ViewGroup {
    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = 0, height = 0;

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        View childView;
        int childWidth, childHeight, lineWidth = 0, lineHeight = 0;
        MarginLayoutParams childParams;
        for (int i = 0, childCount = getChildCount(); i < childCount; i++) {
            childView = getChildAt(i);
            if (childView.getVisibility() == GONE) {
                continue;
            }
            childParams = (MarginLayoutParams) childView.getLayoutParams();
            childWidth = childView.getMeasuredWidth() + childParams.leftMargin + childParams.rightMargin;
            childHeight = childView.getMeasuredHeight() + childParams.topMargin + childParams.bottomMargin;

            if (lineWidth + childWidth > widthSize) {
                width = Math.max(lineWidth, width);
                lineWidth = childWidth;
                height += childHeight;
                lineHeight = childHeight;
            } else {
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }

            if (i == childCount - 1) {
                width = Math.max(lineWidth, childWidth);
                height += lineHeight;
            }
        }
        setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? widthSize : width, heightMode == MeasureSpec.EXACTLY ? heightSize : height);
    }

    private List<List<View>> mAllViews = new ArrayList<>();
    private List<Integer> mLineHeight = new ArrayList<>();

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mAllViews.clear();
        mLineHeight.clear();

        int width = getWidth();
        int lineWidth = 0;
        int lineHeight = 0;

        List<View> lineViews = new ArrayList<>();
        View childView;
        MarginLayoutParams childParams;
        int childWidth = 0, childHeight = 0;
        for (int i = 0, childCount = getChildCount(); i < childCount; i++) {
            childView = getChildAt(i);
            childParams = (MarginLayoutParams) childView.getLayoutParams();
            childWidth = childView.getMeasuredWidth() + childParams.leftMargin + childParams.rightMargin;
            childHeight = childView.getMeasuredHeight() + childParams.topMargin + childParams.bottomMargin;

            if (lineWidth + childWidth > width) {
                mLineHeight.add(lineHeight);
                mAllViews.add(lineViews);
                lineWidth = 0;
                lineHeight = 0;
                lineViews = new ArrayList<>();
            }
            lineWidth += childWidth;
            lineHeight = Math.max(lineHeight, childHeight);
            lineViews.add(childView);
        }

        mLineHeight.add(lineHeight);
        mAllViews.add(lineViews);

        int left = 0;
        int top = 0;
        for (int i = 0, lines = mAllViews.size(); i < lines; i++) {
            lineViews = mAllViews.get(i);
            lineHeight = mLineHeight.get(i);
            for (int j = 0, views = lineViews.size(); j < views; j++) {
                View child = lineViews.get(j);
                if (child.getVisibility() == GONE) {
                    continue;
                }
                MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();

                int childLeft = left + layoutParams.leftMargin;
                int childTop = top + layoutParams.topMargin;
                int childRight = childLeft + child.getMeasuredWidth();
                int childBottom = childTop + child.getMeasuredHeight();

                child.layout(childLeft, childTop, childRight, childBottom);

                left += child.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
            }
            left = 0;
            top += lineHeight;
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }
}
