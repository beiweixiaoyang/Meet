package com.example.meet.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.meet.R;
import com.example.meet.base.BaseUIActivity;
import com.example.meet.utils.FileUtil;
import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;

/**
 * 二维码扫描界面
 * 解析二维码
 *
 */
public class QrCodeActivity extends BaseUIActivity implements View.OnClickListener {


    /**
     * 二维码解析回调函数
     */
    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            //解析成功
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS);
            bundle.putString(CodeUtils.RESULT_STRING, result);
            resultIntent.putExtras(bundle);
            finish();
        }

        @Override
        public void onAnalyzeFailed() {
            //解析失败
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_FAILED);
            bundle.putString(CodeUtils.RESULT_STRING, "");
            resultIntent.putExtras(bundle);
            finish();
        }
    };

    //返回键
    private ImageView iv_back;
    //相册选择
    private TextView iv_to_ablum;
    //闪光灯
    private ImageView iv_flashlight;

    //是否打开闪光灯
    private boolean isOpenFlash = false;
    private static final int REQUEST_IMAGE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);
        initQrCode();
        initView();
    }

    /**
     * 初始化二维码
     */
    private void initQrCode() {
        /**
         * 执行扫面Fragment的初始化操作
         */
        CaptureFragment captureFragment = new CaptureFragment();
        // 为二维码扫描界面设置定制化界面
        CodeUtils.setFragmentArgs(captureFragment, R.layout.layout_qrcode);

        captureFragment.setAnalyzeCallback(analyzeCallback);
        /**
         * 替换我们的扫描控件
         */
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_my_container, captureFragment).commit();
    }

    private void initView() {
        iv_back = findViewById(R.id.iv_back);
        iv_to_ablum = findViewById(R.id.iv_to_ablum);
        iv_flashlight = findViewById(R.id.iv_flashlight);
        iv_to_ablum.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        iv_flashlight.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_to_ablum:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
                break;
            case R.id.iv_flashlight:
                isOpenFlash = !isOpenFlash;
                CodeUtils.isLightEnable(isOpenFlash);//控制闪光灯的开启
                iv_flashlight.setImageResource(isOpenFlash ? R.drawable.img_flashlight_p : R.drawable.img_flashlight);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE) {
                if (data != null) {
                    Uri uri = data.getData();
                    String path = FileUtil.getInstance()
                            .getRealPathFromUri(QrCodeActivity.this, uri);
                    try {
                        CodeUtils.analyzeBitmap(path, new CodeUtils.AnalyzeCallback() {
                            @Override
                            public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                                analyzeCallback.onAnalyzeSuccess(mBitmap,result);
                            }

                            @Override
                            public void onAnalyzeFailed() {
                                analyzeCallback.onAnalyzeFailed();
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}