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
import com.example.meet.base.BaseFragment;
import com.example.meet.bmob.BmobManager;
import com.example.meet.bmob.MeetUser;
import com.example.meet.event.EventManager;
import com.example.meet.event.MessageEvent;
import com.example.meet.ui.NewFriendActivity;
import com.example.meet.ui.PersonActivity;
import com.example.meet.ui.PrivateSetActivity;
import com.example.meet.ui.ShareImageActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class MeFragment extends BaseFragment implements View.OnClickListener{

    private CircleImageView iv_me_photo;
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
        iv_me_photo =view.findViewById(R.id.iv_me_photo);
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
        loadMeInfo();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_me_info:
                startActivity(new Intent(getContext(), PersonActivity.class));
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()) {
            case EventManager.EVENT_REFRE_ME_INFO:
                loadMeInfo();
                break;
        }
    }

    /**
     * 读取个人信息
     */
    private void loadMeInfo() {
        BmobManager.getInstance().queryByObjectId(BmobManager.getInstance().getCurrentUser().getObjectId(),
                new FindListener<MeetUser>() {
                    @Override
                    public void done(List<MeetUser> list, BmobException e) {
                        if(e == null){
                            MeetUser meetUser = list.get(0);
                            Glide.with(getContext())
                                    .load(meetUser.getPhoto())
                                    .into(iv_me_photo);
                            tv_nickname.setText(meetUser.getNickName());
                        }
                    }
                });
    }
}
