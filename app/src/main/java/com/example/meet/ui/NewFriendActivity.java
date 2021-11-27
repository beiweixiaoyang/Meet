package com.example.meet.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.meet.R;
import com.example.meet.base.BaseBackActivity;

/**
 * 好友申请列表页面
 * 1.查询好友申请信息，（通过RecyclerView显示）
 * 2.同意则添加为自己的好友，同时更新本地数据库
 * 3.给对方发送自定义消息
 */
public class NewFriendActivity extends BaseBackActivity {

    private View empty_view;
    private LinearLayout ll_yes;
    private LinearLayout ll_no;
    private LinearLayout ll_agree;
    private TextView tv_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);
    }
}