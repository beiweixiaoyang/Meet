package com.example.meet.adapter;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView的适配器
 * @param <V> 数据源类型
 */
public class CommonAdapter<V> extends RecyclerView.Adapter<CommonViewHolder> {

    private List<V> mLists;

    private OnBindDataListener<V> onBindDataListener;
    private OnMoreBindDataListener<V> onMoreBindDataListener;

    public CommonAdapter(List<V> mLists, OnBindDataListener<V> onBindDataListener) {
        this.mLists = mLists;
        this.onBindDataListener = onBindDataListener;
    }

    public CommonAdapter(List<V> mLists, OnMoreBindDataListener<V> onMoreBindDataListener) {
        this.mLists = mLists;
        this.onBindDataListener = onMoreBindDataListener;
        this.onMoreBindDataListener = onMoreBindDataListener;
    }

    @NonNull
    @Override
    public CommonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId=onBindDataListener.getLayoutId(viewType);
        CommonViewHolder holder=CommonViewHolder.getViewHolder(parent,layoutId);
        return holder;
    }

    @Override
    public int getItemCount() {
        return mLists.size();
    }

    @Override
    public void onBindViewHolder(@NonNull CommonViewHolder holder, int position) {
        onBindDataListener.onBindViewHolder
                (mLists.get(position),holder,getItemViewType(position),position);
    }

    @Override
    public int getItemViewType(int position) {
        if(onMoreBindDataListener !=null){
            return onMoreBindDataListener.getItemType(position);
        }
        return 0;
    }

    public interface OnBindDataListener<V>{
        void onBindViewHolder(V model,CommonViewHolder holder,int type,int position);
        int getLayoutId(int type);
    }

    public interface OnMoreBindDataListener<V> extends OnBindDataListener<V>{
        int getItemType(int position);
    }

}
