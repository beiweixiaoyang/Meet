package com.example.meet.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.meet.R;

import java.io.File;

public class CommonViewHolder extends RecyclerView.ViewHolder {

    //子View的集合
    private SparseArray<View> views;
    private View mContentView;

    public CommonViewHolder(@NonNull View itemView) {
        super(itemView);
        views=new SparseArray<>();
        mContentView=itemView;
    }

    /**
     * 获取CommonViewHolder实体
     * @param parent
     * @param layoutId
     * @return
     */
    public static CommonViewHolder getViewHolder(ViewGroup parent, int layoutId) {
        return new CommonViewHolder(View.inflate(parent.getContext(),layoutId,null));
    }

    /**
     * 提供外部访问View的办法
     */

    public<V extends  View> V getView(int viewId){
        View view=views.get(viewId);
        if(view == null){
            view=mContentView.findViewById(viewId);
            views.put(viewId,view);
        }
        return (V) view;
    }
    /**
     * 设置文本内容
     */
    public CommonViewHolder setText(int viewId,String text){
        TextView textView=getView(viewId);
        textView.setText(text);
        return this;
    }

    /**
     * 设置图片Url
     */
    public CommonViewHolder setImageUrl(Context context, int viewId, String url){
        ImageView imageView=getView(viewId);
        Glide.with(context).load(url).into(imageView);
        return this;
    }

    /**
     * 设置图片Url
     */
    public CommonViewHolder setImageUrl(Context context, int viewId, String url,int w,int h){
        ImageView imageView=getView(viewId);
        Glide.with(context.getApplicationContext())
                .load(url)
                .override(w, h)
                .format(DecodeFormat.PREFER_RGB_565)
                // 取消动画，防止第一次加载不出来
                .dontAnimate()
                //加载缩略图
                .thumbnail(0.3f)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
        return this;
    }

    /**
     * 设置图片资源
     */
    public CommonViewHolder setImageFile(Context context, int viewId, File file){
        ImageView imageView=getView(viewId);
        Glide.with(context).load(file).into(imageView);
        return this;
    }

    /**
     * 根据资源文件设置图片
     */

    public CommonViewHolder setImageResource(int viewId,int resourceId){
        ImageView imageView=getView(viewId);
        imageView.setImageResource(resourceId);
        return this;
    }

    /**
     * 设置背景颜色
     */
    public CommonViewHolder setBackgroundColor(int viewId,int color){
        View View=getView(viewId);
        View.setBackgroundColor(color);
        return this;
    }

    /**
     * 设置view是否可见
     */
    public CommonViewHolder setVisibility(int viewId,int isVisibility){
        LinearLayout linearLayout=getView(viewId);
        linearLayout.setVisibility(isVisibility);
        return this;
    }

    /**
     * 设置文本颜色
     *
     * @param viewId
     * @param color
     * @return
     */
    public CommonViewHolder setTextColor(int viewId, int color) {
        TextView tv = getView(viewId);
        tv.setTextColor(color);
        return this;
    }

}
