package com.example.meet.utils;

import android.content.Context;
import android.view.Gravity;

import com.example.meet.R;
import com.example.meet.view.DialogView;

public class DialogUtils {
    private static DialogUtils dialogManager;

    private DialogUtils() {
    }

    public static DialogUtils getInstance() {
        if (dialogManager == null) {
            synchronized (DialogUtils.class) {
                if (dialogManager == null) {
                    dialogManager = new DialogUtils();
                }
            }
        }
        return dialogManager;
    }

    /**
     * 初始化dialogView
     * @param context 上下文
     * @param layout 布局ID
     * @return
     */
    public DialogView initDialogView(Context context, int layout){
        return new DialogView(context,layout,R.style.DialogTheme, Gravity.CENTER);
    }
    public DialogView initDialogView(Context context,int layout,int gravity){
        return new DialogView(context,layout,R.style.DialogTheme,gravity);
    }

    /**
     * 显示dialog
     * @param view
     */
    public void showDialog(DialogView view){
        if (view != null) {
            if(!view.isShowing()){
                view.show();
            }
        }
    }

    /**
     * 隐藏dialog
     * @param view
     */
    public void hideDialog(DialogView view){
        if (view != null) {
            if(view.isShowing()){
                view.hide();
            }
        }
    }
}
