package com.example.meet.base;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.meet.event.EventManager;
import com.example.meet.event.MessageEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class BaseFragment extends Fragment {


    private OnPermissionListener mListener;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventManager.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventManager.unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {

    }

    /**
     * 在对应位置动态申请权限
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void requestRuntimePermissions(String[] permissions, BaseFragment.OnPermissionListener listener) {
        this.mListener = listener;
        List<String> permissionList = new ArrayList<>();
        //遍历每一个申请的权限，将没有通过的权限放在permissionList中
        for (String permission : permissions) {
            if (getActivity().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            } else {
                mListener.granted();//权限申请全部通过
            }
        }
        //申请没有通过的权限
        if (!permissionList.isEmpty()) {
            requestPermissions(permissionList.toArray(new String[permissionList.size()]), 1);
        }
    }

    /**
     * 申请后的处理
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            List<String> deniedList = new ArrayList<>();
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
     * 申请窗口权限
     */
    protected void requestWindowPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(activity)) {
            Toast.makeText(activity, "当前无权限，请授权", Toast.LENGTH_SHORT).show();
            activity.startActivity(
                    new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + activity.getPackageName())));
        }
    }

    public interface OnPermissionListener {
        void granted();

        void denied(List<String> deniedList);
    }
}
