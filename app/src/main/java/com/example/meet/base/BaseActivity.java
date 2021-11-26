package com.example.meet.base;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * 所有Activity的父类
 * 统一实现app的一些功能
 */
public class BaseActivity extends AppCompatActivity {

    private OnPermissionListener mListener;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 申请权限
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void requestRuntimePermissions(String [] permissions, OnPermissionListener listener){
        this.mListener=listener;
        List<String> permissionList=new ArrayList<>();
        //遍历每一个申请的权限，将没有通过的权限放在permissionList中
        for(String permission:permissions){
            if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
                permissionList.add(permission);
            }else{
                mListener.granted();//权限申请全部通过
            }
        }
        //申请没有通过的权限
        if(!permissionList.isEmpty()){
            requestPermissions(permissionList.toArray(new String[permissionList.size()]),1);
        }
    }

    /**
     * 申请后的处理
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0){
            List<String> deniedList=new ArrayList<>();
            //遍历所有申请的权限，将被拒绝的权限放入list中
            for (int i = 0; i < grantResults.length; i++) {
                int grantResult = grantResults[i];
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    mListener.granted();
                } else {
                    deniedList.add(permissions[i]);
                }
            }
            if (!deniedList.isEmpty()) {
                mListener.denied(deniedList);
            }
        }
    }

    /**
     * 判断窗口权限
     */
    protected boolean checkWindowPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    protected void requestWindowPermission(int requestCode){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                , Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent,requestCode);
    }

    public interface OnPermissionListener {
        void granted();

        void denied(List<String> deniedList);
    }

}
