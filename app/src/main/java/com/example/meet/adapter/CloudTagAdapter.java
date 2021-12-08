package com.example.meet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.meet.R;
import com.example.meet.model.StarUserModel;
import com.moxun.tagcloudlib.view.TagsAdapter;

import java.util.List;

/**
 * 3D星球View适配器
 */
public class CloudTagAdapter extends TagsAdapter {

    private Context mContext;
    private List<StarUserModel> mLists;
    private LayoutInflater mInflater;


    public CloudTagAdapter(Context mContext, List<StarUserModel> mLists) {
        this.mContext = mContext;
        this.mLists = mLists;
        mInflater= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mLists.size();
    }

    @Override
    public View getView(Context context, int position, ViewGroup parent) {
        //返回每个tag实例
        View view=mInflater.inflate(R.layout.layout_stat_view_item,null);
        ImageView iv_star_icon=view.findViewById(R.id.iv_star_icon);
        TextView tv_star_name=view.findViewById(R.id.tv_star_name);
        Glide.with(context)
                .load(mLists.get(position).getPhotoUrl())
                .into(iv_star_icon);
        tv_star_name.setText(mLists.get(position).getNickName());
        return view;
    }

    @Override
    public Object getItem(int position) {
        //根据position返回tag数据
        return mLists.get(position);
    }

    @Override
    public int getPopularity(int position) {
        return 7;
    }

    @Override
    public void onThemeColorChanged(View view, int themeColor) {

    }
}
