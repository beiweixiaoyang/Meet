package com.example.meet.ui;


import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

import com.example.meet.R;
import com.example.meet.base.BaseBackActivity;
import com.example.meet.bmob.BmobManager;
import com.example.meet.bmob.PrivateSet;
import com.example.meet.utils.LogUtils;

import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class PrivateSetActivity extends BaseBackActivity {

    private Switch sw_kill_contact;

    //是否选中
    private boolean isCheck = false;

    //当前ID
    private String currentId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_set);
        initView();
    }

    private void initView() {
        LogUtils.i(String.valueOf(isCheck));
        sw_kill_contact=findViewById(R.id.sw_kill_contact);
        queryPrivateSet();
        sw_kill_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCheck=!isCheck;
                sw_kill_contact.setChecked(isCheck);
                if(isCheck){
                    addPrivateSet();
                }else{
                    delPrivateSet();
                }
            }
        });
    }

    private void queryPrivateSet() {
        BmobManager.getInstance().queryPrivateSet(new FindListener<PrivateSet>() {
            @Override
            public void done(List<PrivateSet> list, BmobException e) {
                if(e == null){
                    if(list.size() > 0){
                        PrivateSet set=list.get(0);
                        currentId=set.getObjectId();
                        if(currentId.equals(BmobManager.getInstance().getCurrentUser().getObjectId())){
                            isCheck=true;
                        }
                        sw_kill_contact.setChecked(isCheck);
                    }
                }
            }
        });
    }

    //添加到私有库
    private void addPrivateSet() {
        LogUtils.i("addPrivateSet");
        BmobManager.getInstance().addPrivateSet(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if(e == null){
                    currentId=s;
                }
            }
        });
    }

    //删除私有库
    private void delPrivateSet() {
        LogUtils.i("delPrivateSet");
        BmobManager.getInstance().delPrivateSet(currentId, new UpdateListener() {
            @Override
            public void done(BmobException e) {

            }
        });
    }

}