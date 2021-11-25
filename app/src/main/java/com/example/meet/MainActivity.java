package com.example.meet;


import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.meet.base.BaseActivity;
import com.example.meet.fragment.ChatFragment;
import com.example.meet.fragment.MeFragment;
import com.example.meet.fragment.SquareFragment;
import com.example.meet.fragment.StarFragment;
import com.example.meet.utils.LogUtils;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initFragment();
        checkMainTab(0);
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
        LogUtils.i("init--> MainActivity");
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
        }
    }
}