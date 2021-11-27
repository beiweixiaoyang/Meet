package com.example.meet.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.meet.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class MeFragment extends Fragment implements View.OnClickListener{

    private CircleImageView iv_mo_photo;
    private LinearLayout ll_me_info;
    private LinearLayout ll_new_friend;
    private LinearLayout ll_private_set;
    private LinearLayout ll_share;
    private LinearLayout ll_setting;
    private LinearLayout ll_notice;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_me,null);
        initView(view);
        return   view;

    }

    /**
     * 初始化View
     */
    private void initView(View view) {
        iv_mo_photo=view.findViewById(R.id.iv_me_photo);
        ll_me_info=view.findViewById(R.id.ll_me_info);
        ll_new_friend=view.findViewById(R.id.ll_new_friend);
        ll_private_set=view.findViewById(R.id.ll_private_set);
        ll_share=view.findViewById(R.id.ll_share);
        ll_setting=view.findViewById(R.id.ll_setting);
        ll_notice=view.findViewById(R.id.ll_notice);
        ll_me_info.setOnClickListener(this);
        ll_new_friend.setOnClickListener(this);
        ll_private_set.setOnClickListener(this);
        ll_share.setOnClickListener(this);
        ll_setting.setOnClickListener(this);
        ll_notice.setOnClickListener(this);
        iv_mo_photo.setImageResource(R.mipmap.meet);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_me_info:
                break;
            case R.id.ll_new_friend:
                break;
            case R.id.ll_private_set:
                break;
            case R.id.ll_share:
                break;
            case R.id.ll_setting:
                break;
            case R.id.ll_notice:
                break;
        }
    }
}
