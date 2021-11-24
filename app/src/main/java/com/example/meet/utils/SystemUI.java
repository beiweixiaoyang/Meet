package com.example.meet.utils;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

/**
 * 修改系统UI
 */
public class SystemUI {

    public static void fixSystemUI(Activity activity){
        //获取最顶层View
        //View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN:全屏显示
        activity.getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        //设置statusBar颜色为透明
        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
    }
}
