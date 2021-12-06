package com.example.meet.base;

import android.app.Application;

import com.example.meet.bmob.BmobManager;
import com.example.meet.manager.CloudManager;
import com.example.meet.manager.MapManager;
import com.example.meet.manager.WindowHelper;
import com.example.meet.utils.SpUtils;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import org.litepal.LitePal;

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
        LitePal.initialize(this);
        MapManager.getInstance().initMap(this);
        WindowHelper.getInstance().initWindow(this);
        ZXingLibrary.initDisplayOpinion(this);//初始化二维码
    }
}
