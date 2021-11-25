package com.example.meet.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.meet.R;
import com.example.meet.base.BaseBackActivity;
import com.example.meet.base.BaseUIActivity;
import com.example.meet.utils.LogUtils;

public class UserInfoActivity extends BaseUIActivity implements View.OnClickListener {

    private String objectId;

    private RelativeLayout ll_back;
    private LinearLayout ll_isFriend;
    private TextView tv_nickname;
    private TextView tv_desc;
    private ImageView iv_user_photo;
    private Button btn_add_friend;
    private Button btn_chat;
    private Button btn_audio_chat;
    private Button btn_video_chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        objectId=getIntent().getStringExtra("objectId");
        initView();
    }

    private void initView() {
        LogUtils.i("init-->UserInfoActivity");
        ll_back=findViewById(R.id.ll_back);
        ll_isFriend=findViewById(R.id.ll_is_friend);
        tv_nickname=findViewById(R.id.tv_nickname);
        tv_desc=findViewById(R.id.tv_desc);
        iv_user_photo=findViewById(R.id.iv_user_photo);
        btn_add_friend=findViewById(R.id.btn_add_friend);
        btn_chat=findViewById(R.id.btn_chat);
        btn_audio_chat=findViewById(R.id.btn_audio_chat);
        btn_video_chat=findViewById(R.id.btn_video_chat);
        btn_chat.setOnClickListener(this);
        ll_back.setOnClickListener(this);
        btn_add_friend.setOnClickListener(this);
        btn_video_chat.setOnClickListener(this);
        btn_audio_chat.setOnClickListener(this);
        initRecyclerView();
    }

    private void initRecyclerView() {
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_add_friend:
                break;
            case R.id.btn_chat:
                break;
            case R.id.btn_video_chat:
                break;
            case R.id.btn_audio_chat:
                break;
            case R.id.ll_back:
                onBackPressed();
                break;
        }
    }
}