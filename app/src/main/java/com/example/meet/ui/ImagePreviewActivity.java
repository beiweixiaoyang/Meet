package com.example.meet.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.meet.R;
import com.example.meet.base.BaseUIActivity;
import com.example.meet.eneity.Constants;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;

/**
 * 预览图片
 */
public class ImagePreviewActivity extends BaseUIActivity implements View.OnClickListener {


    public static void startActivity(Context context, boolean isUrl,String path) {
        Intent intent = new Intent(context, ImagePreviewActivity.class);
        intent.putExtra(Constants.INTENT_IMAGE_PATH, path);
        intent.putExtra("isUrl",isUrl);
        context.startActivity(intent);
    }

    private PhotoView photo_view;
    private ImageView iv_back;
    private TextView tv_download;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        initView();
    }

    private void initView() {
        photo_view = findViewById(R.id.photo_view);
        iv_back = findViewById(R.id.iv_back);
        tv_download = findViewById(R.id.tv_download);
        iv_back.setOnClickListener(this);
        tv_download.setOnClickListener(this);
        path=getIntent().getStringExtra(Constants.INTENT_IMAGE_PATH);
        boolean isUrl = getIntent().getBooleanExtra("isUrl", false);
        if(isUrl){
            Glide.with(this)
                    .load(path)
                    .into(photo_view);
        }else {
            Glide.with(this)
                    .load(new File(path))
                    .into(photo_view);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_download:
                break;
        }
    }
}