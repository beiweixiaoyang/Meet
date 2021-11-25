package com.example.meet.base;

import android.app.Application;

import com.example.meet.bmob.BmobManager;
import com.example.meet.cloud.CloudManager;
import com.example.meet.utils.SpUtils;

/**
 * 在BaseApp中做一些初始化工作
 */
public class BaseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SpUtils.getInstance().initSpUtils(this);
        BmobManager.getInstance().initBmob(this);
        CloudManager.getInstance().initCloud(this);
    }
}
