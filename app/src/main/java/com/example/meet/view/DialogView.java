package com.example.meet.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

/**
 * 自定义DialogView（点击发送弹出图片验证）
 */
public class DialogView extends Dialog {

    /**
     *
     * @param context 上下文
     * @param layout 布局ID
     * @param style styleID
     * @param gravity 位置
     */
    public DialogView(@NonNull Context context,int layout, int style,int gravity) {
        super(context, style);
        setContentView(layout);//加载布局文件
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width=WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height=WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity=gravity;
        window.setAttributes(layoutParams);
    }
}
