package com.huweiqiang.customviewstudy.customviewgroup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Checkable;
import android.widget.FrameLayout;

import com.huweiqiang.customviewstudy.R;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static android.content.ContentValues.TAG;

/**
 * Created by huweiqiang on 2017/3/29.
 */

public class TagFlowLayout extends FlowLayout implements TagAdapter.OnDataChangeListener {
    private TagAdapter mTagAdapter;
    private MotionEvent mMotionEvent;

    private OnTagClickListener mOnTagClickListener;
    private boolean mMultiSupport;
    private int mMaxSelect;

    private Set<Integer> mSelectedView = new HashSet<>();

    private OnSelectListener mOnSelectListener;

    public TagFlowLayout(Context context) {
        this(context, null);
    }

    public TagFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParams(context, attrs);
    }

    private void initParams(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TagFlowLayout);
        mMultiSupport = typedArray.getBoolean(R.styleable.TagFlowLayout_multi_support, false);
        mMaxSelect = typedArray.getInteger(R.styleable.TagFlowLayout_max_select, -1);
        typedArray.recycle();
    }

    public void setAdapter(TagAdapter adapter) {
        mTagAdapter = adapter;
        mTagAdapter.setOnDataChangedListener(this);
        mSelectedView.clear();
        changeAdapter();
    }

    public void setOnTagClickListener(OnTagClickListener mOnTagClickListener) {
        this.mOnTagClickListener = mOnTagClickListener;
        if (mOnTagClickListener != null) {
            setClickable(true);
        }
    }

    public void setOnSelectListener(OnSelectListener onSelectListener) {
        mOnSelectListener = onSelectListener;
        if (mOnSelectListener != null) setClickable(true);
    }

    @Override
    public void onChanged() {
        mSelectedView.clear();
        changeAdapter();
    }

    public void setMaxSelectCount(int count) {
        if (mSelectedView.size() > count) {
            Log.w(TAG, "you has already select more than " + count + " views , so it will be clear .");
            mSelectedView.clear();
        }
        mMaxSelect = count;
    }

    public Set<Integer> getSelectedList() {
        return new HashSet<>(mSelectedView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0, childCount = getChildCount(); i < childCount; i++) {
            TagView tagView = (TagView) getChildAt(i);
            if (tagView.getVisibility() == View.GONE) continue;
            if (tagView.getTagView().getVisibility() == View.GONE) {
                tagView.setVisibility(View.GONE);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mMotionEvent = MotionEvent.obtain(event);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        if (mMotionEvent == null) {
            return super.performClick();
        }

        int x = (int) mMotionEvent.getX();
        int y = (int) mMotionEvent.getY();
        mMotionEvent = null;

        TagView child = findChild(x, y);
        if (child == null) {
            return super.performClick();
        }

        int pos = findPosByView(child);
        if (pos == -1) {
            return super.performClick();
        }

        doSelect(child, pos);
        if (mOnTagClickListener != null) {
            return mOnTagClickListener.onTagClick(child, pos, this);
        }
        return super.performClick();
    }

    private void changeAdapter() {
        removeAllViews();
        TagAdapter adapter = mTagAdapter;
        TagView tagViewContainer;
        HashSet<Integer> preCheckedList = mTagAdapter.getPreCheckedList();

        for (int i = 0; i < adapter.getCount(); i++) {
            View tagView = adapter.getView(this, i, adapter.getItem(i));

            tagViewContainer = new TagView(getContext());
            tagView.setDuplicateParentStateEnabled(true);

            if (tagView.getLayoutParams() != null) {
                tagViewContainer.setLayoutParams(tagView.getLayoutParams());
            } else {
                MarginLayoutParams lp = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                lp.setMargins(dip2px(5), dip2px(5), dip2px(5), dip2px(5));
                tagViewContainer.setLayoutParams(lp);
            }
            tagViewContainer.addView(tagView);
            addView(tagViewContainer);

            if (preCheckedList.contains(i)) {
                tagViewContainer.setChecked(true);
            }

            if (mTagAdapter.setSelected(i, adapter.getItem(i))) {
                mSelectedView.add(i);
                tagViewContainer.setChecked(true);
            }
        }
        mSelectedView.addAll(preCheckedList);
    }

    private int dip2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void doSelect(TagView child, int pos) {
        if (!mMultiSupport) {
            return;
        }

        if (!child.isChecked()) {
            if (mMaxSelect == 1 && mSelectedView.size() == 1) {
                Iterator<Integer> iterator = mSelectedView.iterator();
                Integer preIndex = iterator.next();
                TagView pre = (TagView) getChildAt(preIndex);
                pre.setChecked(false);
                child.setChecked(true);
                mSelectedView.remove(preIndex);
                mSelectedView.add(pos);
            } else if (mMaxSelect > 0 && mSelectedView.size() > mMaxSelect) {
                return;
            }
            child.setChecked(true);
            mSelectedView.add(pos);
        } else {
            child.setChecked(false);
            mSelectedView.remove(pos);
        }
        if (mOnSelectListener != null) {
            mOnSelectListener.onSelected(new HashSet<>(mSelectedView));
        }
    }

    private int findPosByView(TagView child) {
        for (int i = 0, childCount = getChildCount(); i < childCount; i++) {
            TagView childView = (TagView) getChildAt(i);
            if (child == childView) {
                return i;
            }
        }
        return -1;
    }

    private TagView findChild(int x, int y) {
        for (int i = 0, childCount = getChildCount(); i < childCount; i++) {
            TagView tagView = (TagView) getChildAt(i);
            if (tagView.getVisibility() == GONE) {
                continue;
            }

            Rect outRect = new Rect();
            tagView.getHitRect(outRect);
            if (outRect.contains(x, y)) {
                return tagView;
            }
        }
        return null;
    }

    public interface OnTagClickListener {
        boolean onTagClick(View view, int position, FlowLayout parent);
    }

    public interface OnSelectListener {
        void onSelected(Set<Integer> selectPosSet);
    }


    private static final String KEY_CHOOSE_POS = "key_choose_pos";
    private static final String KEY_DEFAULT = "key_default";

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_DEFAULT, super.onSaveInstanceState());

        String selectPos = "";
        if (mSelectedView.size() > 0) {
            for (int key : mSelectedView) {
                selectPos += key + "|";
            }
            selectPos = selectPos.substring(0, selectPos.length() - 1);
        }
        bundle.putString(KEY_CHOOSE_POS, selectPos);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            String selectPos = bundle.getString(KEY_CHOOSE_POS);
            if (!TextUtils.isEmpty(selectPos)) {
                String[] split = selectPos.split("\\|");
                for (String pos : split) {
                    int index = Integer.parseInt(pos);
                    mSelectedView.add(index);

                    TagView tagView = (TagView) getChildAt(index);
                    if (tagView != null)
                        tagView.setChecked(true);
                }

            }
            super.onRestoreInstanceState(bundle.getParcelable(KEY_DEFAULT));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    private static class TagView extends FrameLayout implements Checkable {
        private boolean isChecked;
        private static final int[] CHECK_STATE = new int[]{android.R.attr.state_checked};

        public TagView(Context context) {
            this(context, null);
        }

        public TagView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public View getTagView() {
            return getChildAt(0);
        }

        public boolean isChecked() {
            return isChecked;
        }

        @Override
        public void toggle() {
            setChecked(!isChecked);
        }

        public void setChecked(boolean checked) {
            if (isChecked != checked) {
                this.isChecked = checked;
                refreshDrawableState();
            }
        }

        @Override
        public int[] onCreateDrawableState(int extraSpace) {
            int[] states = super.onCreateDrawableState(extraSpace + 1);
            if (isChecked()) {
                mergeDrawableStates(states, CHECK_STATE);
            }
            return states;
        }
    }
}
