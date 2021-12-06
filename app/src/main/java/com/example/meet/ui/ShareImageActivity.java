package com.example.meet.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.meet.R;
import com.example.meet.base.BaseBackActivity;
import com.example.meet.bmob.BmobManager;
import com.example.meet.bmob.MeetUser;
import com.example.meet.utils.FileUtil;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class ShareImageActivity extends BaseBackActivity implements View.OnClickListener {

    //头像
    private ImageView iv_photo;
    //昵称
    private TextView tv_name;
    //性别
    private TextView tv_sex;
    //年龄
    private TextView tv_age;
    //电话
    private TextView tv_phone;
    //简介
    private TextView tv_desc;
    //二维码
    private ImageView iv_qrcode;
    //根布局
    private LinearLayout ll_content;
    //下载
    private LinearLayout ll_download;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_image);
        initView();
        loadMeInfo();

    }

    /**
     * 创建二维码
     */
    private void createQrCode(String userId) {
        iv_qrcode.post(new Runnable() {
            @Override
            public void run() {
                String textContent = "Meet#" + userId;
                Bitmap bitmap = CodeUtils.createImage(textContent, iv_qrcode.getWidth(), iv_qrcode.getHeight(), null);
                iv_qrcode.setImageBitmap(bitmap);
            }
        });
    }

    /**
     * 加载个人信息
     */
    private void loadMeInfo() {
        BmobManager.getInstance().queryByObjectId(
                BmobManager.getInstance().getCurrentUser().getObjectId(), new FindListener<MeetUser>() {
                    @Override
                    public void done(List<MeetUser> list, BmobException e) {
                        if (e == null) {
                            MeetUser meetUser = list.get(0);
                            Glide.with(ShareImageActivity.this)
                                    .load(meetUser.getPhoto())
                                    .into(iv_photo);
                            tv_sex.setText(meetUser.isSex() ? "男" : "女");
                            tv_age.setText(meetUser.getAge() + "岁");
                            tv_name.setText(meetUser.getNickName());
                            tv_phone.setText(meetUser.getMobilePhoneNumber());
                            tv_desc.setText(meetUser.getDesc());
                            createQrCode(meetUser.getObjectId());
                        }
                    }
                });
    }

    private void initView() {
        iv_photo = findViewById(R.id.iv_photo);
        tv_name = findViewById(R.id.tv_name);
        tv_sex = findViewById(R.id.tv_sex);
        tv_age = findViewById(R.id.tv_age);
        tv_phone = findViewById(R.id.tv_phone);
        tv_desc = findViewById(R.id.tv_desc);
        iv_qrcode = findViewById(R.id.iv_qrcode);
        ll_content = findViewById(R.id.ll_content);
        ll_download = findViewById(R.id.ll_download);
        ll_download.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ll_download) {
            //保存到相册
            ll_content.setDrawingCacheEnabled(true);//保留绘制cache
            //绘制View
            ll_download.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED
                    ), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            ll_content.layout(0, 0, ll_content.getMeasuredWidth(),
                    ll_content.getMeasuredHeight());

            Bitmap mBitmap = ll_content.getDrawingCache();
            if (mBitmap != null) {
                FileUtil.saveBitmapToAlbum(ShareImageActivity.this, mBitmap);
            }
        }
    }

}