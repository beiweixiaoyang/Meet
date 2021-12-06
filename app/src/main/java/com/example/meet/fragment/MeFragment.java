package com.example.meet.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.meet.R;
import com.example.meet.ui.NewFriendActivity;
import com.example.meet.ui.PrivateSetActivity;
import com.example.meet.ui.ShareImageActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class MeFragment extends Fragment implements View.OnClickListener{

    private CircleImageView iv_mo_photo;
    private TextView tv_nickname;
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
        return view;

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
        tv_nickname=view.findViewById(R.id.tv_nickname);
        ll_me_info.setOnClickListener(this);
        ll_new_friend.setOnClickListener(this);
        ll_private_set.setOnClickListener(this);
        ll_share.setOnClickListener(this);
        ll_setting.setOnClickListener(this);
        ll_notice.setOnClickListener(this);
//        Glide.with(this)
//                .load(BmobManager.getInstance().getCurrentUser().getPhoto())
//                .into(iv_mo_photo);
//        tv_nickname.setText(BmobManager.getInstance().getCurrentUser().getNickName());
        Glide.with(this)
                .load("http://b-ssl.duitang.com/uploads/item/201607/27/20160727143727_v5kRZ.jpeg")
                .into(iv_mo_photo);
        tv_nickname.setText("卑微小杨");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_me_info:
                break;
            case R.id.ll_new_friend:
                startActivity(new Intent(getContext(), NewFriendActivity.class));
                break;
            case R.id.ll_private_set:
                startActivity(new Intent(getContext(), PrivateSetActivity.class));
                break;
            case R.id.ll_share:
                startActivity(new Intent(getContext(), ShareImageActivity.class));
                break;
            case R.id.ll_setting:
                break;
            case R.id.ll_notice:
                break;
        }
    }
}
