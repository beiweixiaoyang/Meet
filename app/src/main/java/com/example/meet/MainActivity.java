package com.example.meet;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.meet.base.BaseActivity;
import com.example.meet.bmob.BmobManager;
import com.example.meet.eneity.Constants;
import com.example.meet.fragment.ChatFragment;
import com.example.meet.fragment.MeFragment;
import com.example.meet.fragment.SquareFragment;
import com.example.meet.fragment.StarFragment;
import com.example.meet.gson.TokenBean;
import com.example.meet.manager.HttpManager;
import com.example.meet.services.CloudService;
import com.example.meet.ui.FirstUploadActivity;
import com.example.meet.manager.DialogManager;
import com.example.meet.utils.LogUtils;
import com.example.meet.utils.SpUtils;
import com.example.meet.view.DialogView;
import com.google.gson.Gson;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * APP主界面
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private FrameLayout mMainLayout;

    private LinearLayout ll_star;
    private LinearLayout ll_square;
    private LinearLayout ll_me;
    private LinearLayout ll_chat;

    private TextView tv_star;
    private TextView tv_me;
    private TextView tv_chat;
    private TextView tv_square;

    private ImageView iv_star;
    private ImageView iv_me;
    private ImageView iv_chat;
    private ImageView iv_square;

    private SquareFragment squareFragment;
    private ChatFragment chatFragment;
    private MeFragment meFragment;
    private StarFragment starFragment;
    private FragmentTransaction starTransaction;
    private FragmentTransaction meTransaction;
    private FragmentTransaction squareTransaction;
    private FragmentTransaction chatTransaction;

    private DialogView mUploadDialog;
    private ImageView iv_go_upload;

    private Disposable disposable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initFragment();
        checkMainTab(0);
        checkToken();
        //模拟数据
//        SimulationData.testData();
    }

    /**
     * 检查Token
     */

    private void checkToken() {
        LogUtils.i("checkToken");
        //获取Token，需要三个参数：用户id，头像地址，昵称
        String token= SpUtils.getInstance().getString(Constants.SP_TOKEN,"");
        if(TextUtils.isEmpty(token)){
//            String tokenNickname=BmobManager.getInstance().getCurrentUser().getTokenNickName();
//            String tokenPhoto= BmobManager.getInstance().getCurrentUser().getTokenPhoto();
            String tokenPhoto="http://b-ssl.duitang.com/uploads/item/201607/27/20160727143727_v5kRZ.jpeg";
            String tokenNickname="啊哈哈哈";
            if(TextUtils.isEmpty(tokenPhoto) && TextUtils.isEmpty(tokenNickname)){
                createUploadDialog();
            }else{
                createToken();
            }
        }
        else{
            startCloudService();
        }
    }

    /**
     * 创建Token
     */
    private void createToken() {
        LogUtils.i("createToken");
        //判断当前是否有用户处于登陆状态
        if( BmobManager.getInstance().getCurrentUser() == null){
            Toast.makeText(this, "登录异常", Toast.LENGTH_SHORT).show();
            return;
        }
        //去融云后台获取Token，连接融云
        HashMap<String,String> map=new HashMap<>();
//        map.put("userId", BmobManager.getInstance().getCurrentUser().getObjectId());
//        map.put("portraitUri", BmobManager.getInstance().getCurrentUser().getTokenPhoto());
//        map.put("name", BmobManager.getInstance().getCurrentUser().getTokenNickName());
        map.put("userId","6015b1078c");
        map.put("name","啊哈哈哈");
        map.put("portraitUri","http://b-ssl.duitang.com/uploads/item/201607/27/20160727143727_v5kRZ.jpeg");
        //通过OkHttp请求Token
        //线程调度
        disposable = Observable.create((ObservableOnSubscribe<String>) emitter -> {
            //执行请求过程
            String json = HttpManager.getInstance().postCloudToken(map);
            LogUtils.i("json:" + json);
            emitter.onNext(json);
            emitter.onComplete();
        }).subscribeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> parsingCloudToken(s));
    }

    /**
     * 解析token
     * @param s
     */
    private void parsingCloudToken(String s) {
        LogUtils.i("parsingCloudToken:" + s);
        try {
            TokenBean tokenBean = new Gson().fromJson(s, TokenBean.class);
            if (tokenBean.getCode() == 200) {
                if (!TextUtils.isEmpty(tokenBean.getToken())) {
                    //保存Token
                    SpUtils.getInstance().putString(Constants.SP_TOKEN, tokenBean.getToken());
                    startCloudService();
                }
            } else if (tokenBean.getCode() == 2007) {
                Toast.makeText(this, "注册人数已达上限，请替换成自己的Key", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            LogUtils.i("parsingCloudToken:" + e.toString());
        }
    }

    /**
     * 启动云服务
     */
    private void startCloudService() {
        LogUtils.i("startCloudService");
        startService(new Intent(this, CloudService.class));
    }

    /**
     * 创建上传头像提示框
     */
    private void createUploadDialog() {
        mUploadDialog= DialogManager.getInstance().initDialogView(this,R.layout.dialog_first_upload);
        iv_go_upload=mUploadDialog.findViewById(R.id.iv_go_upload);
        iv_go_upload.setOnClickListener(this);
        DialogManager.getInstance().showDialog(mUploadDialog);
    }

    /**
     * 选择显示的tab
     *
     * @param i
     */
    private void checkMainTab(int i) {
        switch (i) {
            case 0:
                showFragment(starFragment);
                iv_star.setImageResource(R.drawable.img_star_p);
                iv_square.setImageResource(R.drawable.img_square);
                iv_chat.setImageResource(R.drawable.img_chat);
                iv_me.setImageResource(R.drawable.img_me);
                tv_star.setTextColor(getResources().getColor(R.color.colorAccent));
                tv_square.setTextColor(Color.BLACK);
                tv_chat.setTextColor(Color.BLACK);
                tv_me.setTextColor(Color.BLACK);
                break;
            case 1:
                showFragment(squareFragment);
                iv_star.setImageResource(R.drawable.img_star);
                iv_square.setImageResource(R.drawable.img_square_p);
                iv_chat.setImageResource(R.drawable.img_chat);
                iv_me.setImageResource(R.drawable.img_me);
                tv_star.setTextColor(Color.BLACK);
                tv_square.setTextColor(getResources().getColor(R.color.colorAccent));
                tv_chat.setTextColor(Color.BLACK);
                tv_me.setTextColor(Color.BLACK);
                break;
            case 2:
                showFragment(chatFragment);
                iv_star.setImageResource(R.drawable.img_star);
                iv_square.setImageResource(R.drawable.img_square);
                iv_chat.setImageResource(R.drawable.img_chat_p);
                iv_me.setImageResource(R.drawable.img_me);
                tv_star.setTextColor(Color.BLACK);
                tv_square.setTextColor(Color.BLACK);
                tv_chat.setTextColor(getResources().getColor(R.color.colorAccent));
                tv_me.setTextColor(Color.BLACK);
                break;
            case 3:
                showFragment(meFragment);
                iv_star.setImageResource(R.drawable.img_star);
                iv_square.setImageResource(R.drawable.img_square);
                iv_chat.setImageResource(R.drawable.img_chat);
                iv_me.setImageResource(R.drawable.img_me_p);
                tv_star.setTextColor(Color.BLACK);
                tv_square.setTextColor(Color.BLACK);
                tv_chat.setTextColor(Color.BLACK);
                tv_me.setTextColor(getResources().getColor(R.color.colorAccent));
                break;
        }
    }

    /**
     * 显示指定的Fragment
     */
    private void showFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
            hideAllFragment(transaction);
            transaction.show(fragment);
            transaction.commit();
        }
    }

    /**
     * 隐藏所有的Fragment
     * @param transaction
     */
    private void hideAllFragment(FragmentTransaction transaction) {
        if(starFragment != null){
            transaction.hide(starFragment);
        }
        if(meFragment != null){
            transaction.hide(meFragment);
        }
        if(squareFragment != null){
            transaction.hide(squareFragment);
        }
        if(chatFragment != null){
            transaction.hide(chatFragment);
        }
    }

    /**
     * 初始化Fragment
     */
    private void initFragment() {
        if (starFragment == null) {
            starFragment = new StarFragment();
            starTransaction = getSupportFragmentManager().beginTransaction();
            starTransaction.add(R.id.mMainLayout, starFragment);//加载到Activity定义的占位符中
            starTransaction.commit();
        }
        if (meFragment == null) {
            meFragment = new MeFragment();
            meTransaction = getSupportFragmentManager().beginTransaction();
            meTransaction.add(R.id.mMainLayout, meFragment);//加载到Activity定义的占位符中
            meTransaction.commit();
        }
        if (squareFragment == null) {
            squareFragment = new SquareFragment();
            squareTransaction = getSupportFragmentManager().beginTransaction();
            squareTransaction.add(R.id.mMainLayout, squareFragment);//加载到Activity定义的占位符中
            squareTransaction.commit();
        }
        if (chatFragment == null) {
            chatFragment = new ChatFragment();
            chatTransaction = getSupportFragmentManager().beginTransaction();
            chatTransaction.add(R.id.mMainLayout, chatFragment);//加载到Activity定义的占位符中
            chatTransaction.commit();
        }
    }

    private void initView() {
        ll_me = findViewById(R.id.ll_me);
        ll_star = findViewById(R.id.ll_star);
        ll_square = findViewById(R.id.ll_square);
        ll_chat = findViewById(R.id.ll_chat);
        tv_me = findViewById(R.id.tv_me);
        tv_chat = findViewById(R.id.tv_chat);
        tv_square = findViewById(R.id.tv_square);
        tv_star = findViewById(R.id.tv_star);
        iv_me = findViewById(R.id.iv_me);
        iv_chat = findViewById(R.id.iv_chat);
        iv_square = findViewById(R.id.iv_square);
        iv_star = findViewById(R.id.iv_star);
        mMainLayout=findViewById(R.id.mMainLayout);
        ll_me.setOnClickListener(this);
        ll_chat.setOnClickListener(this);
        ll_star.setOnClickListener(this);
        ll_square.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_chat:
                checkMainTab(2);
                break;
            case R.id.ll_square:
                checkMainTab(1);
                break;
            case R.id.ll_me:
                checkMainTab(3);
                break;
            case R.id.ll_star:
                checkMainTab(0);
                break;
            case R.id.iv_go_upload:
                startActivity(new Intent(MainActivity.this, FirstUploadActivity.class));
                DialogManager.getInstance().hideDialog(mUploadDialog);
                break;
        }
    }
}