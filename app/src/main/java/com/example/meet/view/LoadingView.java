package com.example.meet.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.meet.R;
import com.example.meet.utils.AnimUtils;
import com.example.meet.utils.DialogUtils;

/**
 * 登录提示框
 */
public class LoadingView {

    private DialogView mLoadingView;
    private ImageView iv_loading;
    private TextView tv_loading;
    private ObjectAnimator mAnim;

    public LoadingView(Context context){
        mLoadingView= DialogUtils.getInstance().initDialogView(context, R.layout.dialog_loading);
        iv_loading=mLoadingView.findViewById(R.id.iv_loading);
        tv_loading=mLoadingView.findViewById(R.id.tv_loading);
        mAnim= AnimUtils.rotation(iv_loading);
    }

    public void show(){
        mAnim.start();
        DialogUtils.getInstance().showDialog(mLoadingView);
    }
    public void show(String text){
        mAnim.start();
        if(!TextUtils.isEmpty(text)){
            tv_loading.setText(text);
        }
        DialogUtils.getInstance().showDialog(mLoadingView);
    }
    public void hide(){
        mAnim.pause();
        DialogUtils.getInstance().hideDialog(mLoadingView);
    }
}
