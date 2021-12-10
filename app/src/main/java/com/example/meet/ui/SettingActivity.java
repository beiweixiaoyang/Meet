package com.example.meet.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meet.R;
import com.example.meet.base.BaseBackActivity;
import com.example.meet.eneity.Constants;
import com.example.meet.manager.CloudManager;
import com.example.meet.manager.DialogManager;
import com.example.meet.services.CloudService;
import com.example.meet.utils.ActivityUtils;
import com.example.meet.utils.LanguaueUtils;
import com.example.meet.utils.SpUtils;
import com.example.meet.utils.UpdateUtils;
import com.example.meet.view.DialogView;

import cn.bmob.v3.BmobUser;

public class SettingActivity extends BaseBackActivity implements View.OnClickListener {

    private Switch sw_app_tips;
    private RelativeLayout rl_app_tips;
    private TextView tv_cache_size;
    private RelativeLayout rl_clear_cache;
    private TextView tv_current_languaue;
    private RelativeLayout rl_update_languaue;
    private RelativeLayout rl_check_permissions;
    private TextView tv_app_version;
    private Button btn_logout;
    private TextView tv_new_version;
    private RelativeLayout rl_check_version;
    private RelativeLayout rl_app_show;
    private RelativeLayout rl_chat_theme;

    private DialogView mLanguaueDialog;
    private TextView tv_zh;
    private TextView tv_en;
    private TextView tv_cancel;

    private boolean isTips;

    private UpdateUtils updateUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
    }

    private void initView() {

        sw_app_tips = (Switch) findViewById(R.id.sw_app_tips);
        rl_app_tips = (RelativeLayout) findViewById(R.id.rl_app_tips);

        tv_cache_size = (TextView) findViewById(R.id.tv_cache_size);
        rl_clear_cache = (RelativeLayout) findViewById(R.id.rl_clear_cache);

        tv_current_languaue = (TextView) findViewById(R.id.tv_current_languaue);
        rl_update_languaue = (RelativeLayout) findViewById(R.id.rl_update_languaue);

        rl_check_permissions = (RelativeLayout) findViewById(R.id.rl_check_permissions);
        tv_new_version = findViewById(R.id.tv_new_version);

        rl_app_show = (RelativeLayout) findViewById(R.id.rl_app_show);
        rl_app_show.setOnClickListener(this);

        tv_app_version = (TextView) findViewById(R.id.tv_app_version);
        rl_check_version = findViewById(R.id.rl_check_version);

        rl_chat_theme = findViewById(R.id.rl_chat_theme);

        btn_logout = (Button) findViewById(R.id.btn_logout);

        btn_logout.setOnClickListener(this);
        rl_clear_cache.setOnClickListener(this);
        rl_app_tips.setOnClickListener(this);
        rl_update_languaue.setOnClickListener(this);
        rl_check_permissions.setOnClickListener(this);
        rl_check_version.setOnClickListener(this);
        rl_chat_theme.setOnClickListener(this);

        try {
            tv_app_version.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        isTips = SpUtils.getInstance().getBoolean("isTips", true);
        sw_app_tips.setChecked(isTips);

        tv_cache_size.setText("0.0 MB");

        int languaue = SpUtils.getInstance().getInt(Constants.SP_LANGUAUE, 0);
        tv_current_languaue.setText(languaue == 1 ? "English" : "中文");

        initLanguaueDialog();

        updateUtils = new UpdateUtils(this);
        updateApp();
    }

    private void updateApp() {
        updateUtils.updateApp(isUpdate -> tv_new_version.setVisibility(isUpdate ? View.VISIBLE : View.GONE));
    }

    private void initLanguaueDialog() {
        mLanguaueDialog = DialogManager.getInstance().initDialogView(this, R.layout.dialog_select_photo, Gravity.BOTTOM);
        tv_zh = (TextView) mLanguaueDialog.findViewById(R.id.tv_camera);
        tv_en = (TextView) mLanguaueDialog.findViewById(R.id.tv_album);
        tv_cancel = (TextView) mLanguaueDialog.findViewById(R.id.tv_cancel);

        tv_zh.setText("中文");
        tv_en.setText("English");

        tv_zh.setOnClickListener(view -> {
            selectLanguaue(0);
            DialogManager.getInstance().hideDialog(mLanguaueDialog);
        });

        tv_en.setOnClickListener(view -> {
            selectLanguaue(1);
            DialogManager.getInstance().hideDialog(mLanguaueDialog);
        });

        tv_cancel.setOnClickListener(view -> DialogManager.getInstance().hideDialog(mLanguaueDialog));
    }

    /**
     * @param index
     */
    private void selectLanguaue(int index) {
        if (LanguaueUtils.SYS_LANGUAGE == index) {
            return;
        }
        SpUtils.getInstance().putInt(Constants.SP_LANGUAUE, index);
        //EventManager.post(EventManager.EVENT_RUPDATE_LANGUAUE);
        Toast.makeText(this, "Test Model , Reboot App ", Toast.LENGTH_SHORT).show();
        //暂时先重启处理
        finishAffinity();
        System.exit(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_app_tips:
                isTips = !isTips;
                sw_app_tips.setChecked(isTips);
                SpUtils.getInstance().putBoolean("isTips", isTips);
                break;
            case R.id.rl_chat_theme:
                startActivity(new Intent(this, ChatThemeActivity.class));
                break;
            case R.id.rl_clear_cache:

                break;
            case R.id.rl_update_languaue:
                DialogManager.getInstance().showDialog(mLanguaueDialog);
                break;
            case R.id.rl_check_permissions:
                openWindow();
                break;
            case R.id.rl_check_version:
                updateApp();
                break;
            case R.id.btn_logout:
                /**
                 * 退出登录的逻辑
                 * 1.通过一个管理类管理好所有的Activity
                 * 2.清空Token
                 * 3.清空Bmob
                 * 4.断开服务连接
                 * 5.跳转至登录页
                 * 7.停止云服务
                 */
                logout();
                break;
            case R.id.rl_app_show:
                Uri uri = Uri.parse("https://www.pgyer.com/imoocmeet");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
        }
    }

    /**
     * 退出登录
     */
    private void logout() {

        //删除Token
        SpUtils.getInstance().deleteKey(Constants.SP_TOKEN);
        //Bmob退出登录
        BmobUser.logOut();
        //融云
        CloudManager.getInstance().disconnect();
        CloudManager.getInstance().logout();

        ActivityUtils.getInstance().exit();

        stopService(new Intent(this, CloudService.class));

        //跳转到登录页
        Intent intent_login = new Intent();
        intent_login.setClass(SettingActivity.this, LoginActivity.class);
        intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent_login);
        finish();
    }

    private void openWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                            , Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "已授予窗口权限", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "已授予窗口权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}