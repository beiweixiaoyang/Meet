package com.example.meet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.meet.MainActivity;
import com.example.meet.R;
import com.example.meet.base.BaseUIActivity;
import com.example.meet.bmob.BmobManager;
import com.example.meet.bmob.MeetUser;
import com.example.meet.eneity.Constants;
import com.example.meet.manager.DialogManager;
import com.example.meet.utils.LogUtils;
import com.example.meet.utils.SpUtils;
import com.example.meet.view.DialogView;
import com.example.meet.view.LoadingView;
import com.example.meet.view.TouchPictureView;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;

/**
 * 登录页面
 * 1.输入手机号，点击发送验证码
 * 2.图片验证通过后，通过验证码登录
 */
public class LoginActivity extends BaseUIActivity implements View.OnClickListener{

    private EditText et_phone;
    private EditText et_code;
    private Button btn_send_code;
    private Button btn_login;


    private DialogView mDialogView;
    private LoadingView mLoadingView;
    private TouchPictureView mPictureView;

    private static int TIME=60;

    private static final int HANDLER_TIME=102;

    private Handler mHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){
                case HANDLER_TIME:
                    TIME -- ;
                    btn_send_code.setText(TIME+"s"+"后重新发送");
                    if(TIME >0){
                        mHandler.sendEmptyMessageDelayed(HANDLER_TIME,1000);
                    }else{
                        btn_send_code.setEnabled(true);
                        btn_send_code.setText("重新发送");
                    }
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initDialogView();
        initView();
    }

    /**
     * 初始化Dialog
     */
    private void initDialogView() {
        mLoadingView=new LoadingView(this);
        mDialogView= DialogManager.getInstance().initDialogView(this,R.layout.dialog_code_view);
        mPictureView=mDialogView.findViewById(R.id.pictureView);
        mPictureView.setOnViewResultListener(new TouchPictureView.OnViewResultListener() {
            @Override
            public void onResult() {
                DialogManager.getInstance().hideDialog(mDialogView);
                sendSMS();//手指抬起并且验证成功后发送短信
            }
        });
    }

    private void initView() {
        et_phone=findViewById(R.id.et_phone);
        et_code=findViewById(R.id.et_code);
        btn_send_code=findViewById(R.id.btn_send_code);
        btn_login=findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);
        btn_send_code.setOnClickListener(this);
    }

    /**
     * 请求短信验证码
     * 1.手机号不能为空
     * 2.通过Bmob请求短信验证码
     */
    private void sendSMS() {
        LogUtils.i("sendSMS");
        String phone=et_phone.getText().toString().trim();
        if(TextUtils.isEmpty(phone)){
            Toast.makeText(this,"手机号不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        BmobManager.getInstance().requestSMSCode(phone, new QueryListener<Integer>() {
            @Override
            public void done(Integer integer, BmobException e) {
                if(e == null){
                    LogUtils.i("验证码发送成功");
                    btn_send_code.setEnabled(false);
                    mHandler.sendEmptyMessage(HANDLER_TIME);
                    Toast.makeText(LoginActivity.this,"验证码发送成功",
                            Toast.LENGTH_SHORT).show();

                }else {
                    LogUtils.e("验证码发送失败"+e.toString());
                    Toast.makeText(LoginActivity.this,"验证码发送失败",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 通过手机号和验证码进行登录
     */
    private void login() {
        LogUtils.i("login");
        String phone=et_phone.getText().toString().trim();
        if(TextUtils.isEmpty(phone)){
            Toast.makeText(this,"手机号码不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        String code=et_code.getText().toString().trim();
        if(TextUtils.isEmpty(code)){
            Toast.makeText(this,"验证码不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        mLoadingView.show("正在登陆，请稍后");
        BmobManager.getInstance().signOrLoginByMobilePhone(phone, code, new LogInListener<MeetUser>() {
            @Override
            public void done(MeetUser meetUser, BmobException e) {
                if(e == null){
                    LogUtils.i("登录成功");
                    mLoadingView.hide();
                    //跳转到主页
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    SpUtils.getInstance().putString(Constants.SP_PHONE,phone);
                    finish();
                }else{
                    LogUtils.e("登陆失败"+e.toString());
                    Toast.makeText(LoginActivity.this,"登陆失败，请稍后再试",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_login:
                login();
                break;
            case R.id.btn_send_code:
                DialogManager.getInstance().showDialog(mDialogView);
                LogUtils.i(String.valueOf(mDialogView == null));
                break;
        }
    }
}