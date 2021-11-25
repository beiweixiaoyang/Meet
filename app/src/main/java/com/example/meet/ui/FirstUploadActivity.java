package com.example.meet.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.meet.R;
import com.example.meet.base.BaseBackActivity;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 上传头像界面
 */
public class FirstUploadActivity extends BaseBackActivity implements View.OnClickListener {

    private CircleImageView iv_photo;
    private EditText et_nickname;
    private Button btn_upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_upload);
        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        iv_photo=findViewById(R.id.iv_photo);
        et_nickname=findViewById(R.id.et_nickname);
        btn_upload=findViewById(R.id.btn_upload);
        btn_upload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_upload){

        }
    }
}