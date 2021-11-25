package com.example.meet.ui;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.example.meet.R;
import com.example.meet.adapter.BasePageAdapter;
import com.example.meet.base.BaseUIActivity;
import com.example.meet.utils.AnimUtils;
import com.example.meet.utils.LogUtils;
import com.example.meet.utils.MediaPlayerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 第一次打开APP，进入引导页面
 */
public class GuideActivity extends BaseUIActivity implements View.OnClickListener{

    private ViewPager mViewPager;
    private TextView tv_guide_skip;
    private ImageView iv_music_switch;
    private ImageView iv_guide_point_1;
    private ImageView iv_guide_point_2;
    private ImageView iv_guide_point_3;
    private View view1,view2,view3;


    private LayoutInflater inflater;
    private List<View> mPagerList=new ArrayList<>();
    private BasePageAdapter mPageAdapter;

    private MediaPlayerUtils mediaPlayerUtils;

    private ObjectAnimator animator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        initView();
    }

    private void initView() {
        LogUtils.i("init---> GuideActivity");
        inflater=LayoutInflater.from(this);
        mViewPager=findViewById(R.id.mViewPager);
        tv_guide_skip=findViewById(R.id.tv_guide_skip);
        iv_music_switch=findViewById(R.id.iv_music_switch);
        iv_guide_point_1=findViewById(R.id.iv_guide_point_1);
        iv_guide_point_2=findViewById(R.id.iv_guide_point_2);
        iv_guide_point_3=findViewById(R.id.iv_guide_point_3);
        tv_guide_skip.setOnClickListener(this);
        iv_music_switch.setOnClickListener(this);
        //加载布局文件
        view1=inflater.inflate(R.layout.layout_pager_guide_1,null);
        view2=inflater.inflate(R.layout.layout_pager_guide_2,null);
        view3=inflater.inflate(R.layout.layout_pager_guide_3,null);
        mPagerList.add(view1);
        mPagerList.add(view2);
        mPagerList.add(view3);
        mViewPager.setOffscreenPageLimit(mPagerList.size());//设置页面数量
        mPageAdapter=new BasePageAdapter(mPagerList);
        mViewPager.setAdapter(mPageAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                
            }

            @Override
            public void onPageSelected(int position) {
                selectPage(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //播放帧动画
        ImageView iv_guide_star=view1.findViewById(R.id.iv_guide_star);
        ImageView iv_guide_night=view2.findViewById(R.id.iv_guide_night);
        ImageView iv_guide_smile=view3.findViewById(R.id.iv_guide_smile);
        AnimationDrawable anim1= (AnimationDrawable)iv_guide_star .getBackground();
        AnimationDrawable anim2= (AnimationDrawable) iv_guide_night.getBackground();
        AnimationDrawable anim3= (AnimationDrawable) iv_guide_smile.getBackground();
        anim1.start();
        anim2.start();
        anim3.start();
        startMusic();
    }

    /**
     * 播放音乐
     */
    private void startMusic() {
        mediaPlayerUtils=new MediaPlayerUtils();
        //获取到资源文件
        mediaPlayerUtils.startPlay(getResources().openRawResourceFd(R.raw.guide));
        mediaPlayerUtils.setLooping(true);
        //属性动画
        animator= AnimUtils.rotation(iv_music_switch);
        animator.start();
    }

    private void selectPage(int position) {
        switch (position){
            case 0:
                iv_guide_point_1.setImageResource(R.drawable.img_guide_point_p);
                iv_guide_point_2.setImageResource(R.drawable.img_guide_point);
                iv_guide_point_3.setImageResource(R.drawable.img_guide_point);
                break;
            case 1:
                iv_guide_point_1.setImageResource(R.drawable.img_guide_point);
                iv_guide_point_2.setImageResource(R.drawable.img_guide_point_p);
                iv_guide_point_3.setImageResource(R.drawable.img_guide_point);
                break;
            case 2:
                iv_guide_point_1.setImageResource(R.drawable.img_guide_point);
                iv_guide_point_2.setImageResource(R.drawable.img_guide_point);
                iv_guide_point_3.setImageResource(R.drawable.img_guide_point_p);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_music_switch:
                if(mediaPlayerUtils.MEDIA_STATUS == MediaPlayerUtils.MEDIA_STATUS_PAUSE){
                    mediaPlayerUtils.continuePlay();
                    animator.start();
                    iv_music_switch.setImageResource(R.drawable.img_guide_music);
                }else if(mediaPlayerUtils.MEDIA_STATUS == MediaPlayerUtils.MEDIA_STATUS_PLAY){
                    animator.pause();
                    mediaPlayerUtils.pausePlay();
                    iv_music_switch.setImageResource(R.drawable.img_guide_music_off);
                }
                break;
            case R.id.tv_guide_skip:
                startActivity(new Intent(GuideActivity.this,LoginActivity.class));
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayerUtils.stopPlay();
    }
}