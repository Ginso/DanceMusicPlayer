package com.ldt.dancemusic.contract;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsDataAdapter<VH extends AbsBindAbleHolder, I> extends RecyclerView.Adapter<VH> {

    private final List<I> mDisplayedData = new ArrayList<>();
    private final List<I> mData = new ArrayList<>();

    public final List<I> getData() {
        return mDisplayedData;
    }

    public final List<I> getAllData() {
        return mData;
    }

    public final void setData(List<I> data) {
        mDisplayedData.clear();
        mData.clear();

        if (data != null) {
            mDisplayedData.addAll(data);
            mData.addAll(data);
        }

        onDataSet();
        onDataChanged();
        notifyDataSetChanged();
    }

    public final void modifyData(List<I> data) {
        if(data != mDisplayedData) {
            mDisplayedData.clear();
            if (data != null) {
                mDisplayedData.addAll(data);
            }
        }
        onDataChanged();
        notifyDataSetChanged();
    }

    protected void onDataChanged() {

    }

    @Override
    public int getItemCount() {
        return mDisplayedData.size();
    }

    protected abstract void onDataSet();

    public void destroy() {
        mDisplayedData.clear();
    }

    public I getDataItem(int i) {
        return mDisplayedData.get(i);
    }

    protected int getMediaHolderPosition(int dataPosition) {
        return dataPosition;
    }

    protected int getDataPosition(int itemHolderPosition) {
        return itemHolderPosition;
    }

}
