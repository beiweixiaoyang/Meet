package com.example.meet.base;

import android.app.Application;

import com.example.meet.bmob.BmobManager;

/**
 * 在BaseApp中做一些初始化工作
 */
public class BaseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BmobManager.getInstance().initBmob(this);
    }
}
