package com.example.meet.ui;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.example.meet.MainActivity;
import com.example.meet.R;
import com.example.meet.base.BaseUIActivity;
import com.example.meet.eneity.Constants;
import com.example.meet.utils.SpUtils;

/**
 * 启动页面
 * 1.启动页面全屏
 * 2.延迟三秒后通过判断进入主页或者其他页面(通过Handler)
 * 3.适配刘海屏（在Manifest中进行配置）
 */
public class IndexActivity extends BaseUIActivity {

    private static final int HANDLER_SKIP=100;

    private boolean isFirstRun;//是否是第一次启动App

    private Handler mHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if(msg.what == HANDLER_SKIP){
                skipToMain();
            }
            return false;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        mHandler.sendEmptyMessageDelayed(HANDLER_SKIP,3*1000);
    }

    /**
     * 跳转页面
     * 第一次启动进入引导页
     * 非第一次启动 根据token判断是否登录
     * 登录则进入 主界面
     * 否则进入登录页面
     */
    private void skipToMain() {
        isFirstRun= SpUtils.getInstance().getBoolean(Constants.SP_IS_FIRST_RUN,true);
        Intent intent=new Intent();
        if(isFirstRun){
            intent.setClass(this,GuideActivity.class);
            SpUtils.getInstance().putBoolean(Constants.SP_IS_FIRST_RUN,false);
        }else{
            String token=SpUtils.getInstance().getString(Constants.SP_TOKEN,"");
            if(TextUtils.isEmpty(token)){
                //token为空，进入登陆页面
                intent.setClass(this,LoginActivity.class);
            }else{
                intent.setClass(this, MainActivity.class);
            }
        }
        startActivity(intent);
        finish();
    }
}