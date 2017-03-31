package com.huweiqiang.customviewstudy.customviewgroup;

import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by huweiqiang on 2017/3/30.
 */

public abstract class TagAdapter<T> {
    private List<T> mTagData;
    private OnDataChangeListener mOnDataChangeListener;
    private HashSet<Integer> mCheckedPosList = new HashSet<>();

    public TagAdapter(List<T> data) {
        mTagData = data;
    }

    public TagAdapter(T[] data) {
        mTagData = new ArrayList<T>(Arrays.asList(data));
    }


    public boolean setSelected(int i, Object item) {
        return false;
    }

    public void setSelectedList(Set<Integer> set) {
        mCheckedPosList.clear();
        if (set != null)
            mCheckedPosList.addAll(set);
        notifyDataChanged();
    }

    public void setSelectedList(int... poses) {
        Set<Integer> set = new HashSet<>();
        for (int pos : poses) {
            set.add(pos);
        }
        setSelectedList(set);
    }

    HashSet<Integer> getPreCheckedList() {
        return mCheckedPosList;
    }

    static interface OnDataChangeListener {
        void onChanged();
    }

    void setOnDataChangedListener(OnDataChangeListener listener) {
        mOnDataChangeListener = listener;
    }

    public int getCount() {
        return mTagData == null ? 0 : mTagData.size();
    }

    public void notifyDataChanged() {
        mOnDataChangeListener.onChanged();
    }

    public T getItem(int position) {
        return mTagData.get(position);
    }

    public abstract View getView(FlowLayout parent, int position, T t);
}
