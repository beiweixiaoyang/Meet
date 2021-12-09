package com.example.meet.ui;

import static com.example.meet.utils.FileUtil.ALBUM_REQUEST_CODE;
import static com.example.meet.utils.FileUtil.CAMERA_REQUEST_CODE;
import static com.example.meet.utils.FileUtil.MUSIC_REQUEST_CODE;
import static com.example.meet.utils.FileUtil.VIDEO_REQUEST_CODE;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.meet.R;
import com.example.meet.base.BaseBackActivity;
import com.example.meet.bmob.BmobManager;
import com.example.meet.bmob.SquareSet;
import com.example.meet.utils.FileUtil;
import com.example.meet.utils.LogUtils;
import com.example.meet.view.LoadingView;

import java.io.File;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;

/**
 * 发布广场界面
 */
public class PushSquareActivity extends BaseBackActivity implements View.OnClickListener {

    //输入框
    private EditText et_content;
    //蚊子数量
    private TextView tv_content_size;
    private LinearLayout ll_media;
    //清空
    private ImageView iv_error;
    //媒体路径
    private TextView tv_media_path;
    //媒体类型
    private LinearLayout ll_media_type;
    //相机
    private LinearLayout ll_camera;
    //相册
    private LinearLayout ll_ablum;
    //音乐
    private LinearLayout ll_music;
    //    视频
    private LinearLayout ll_video;

    private LoadingView mLoadingView;

    private File uploadFile;

    private static int MEDIA_TYPE = SquareSet.PUSH_TEXT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_square);
        initView();
    }


    private void initView() {
        mLoadingView=new LoadingView(this);
        et_content = findViewById(R.id.et_content);
        tv_content_size = findViewById(R.id.tv_content_size);
        ll_media = findViewById(R.id.ll_media);
        iv_error = findViewById(R.id.iv_error);
        tv_media_path = findViewById(R.id.tv_media_path);
        ll_media_type = findViewById(R.id.ll_media_type);
        ll_camera = findViewById(R.id.ll_camera);
        ll_ablum = findViewById(R.id.ll_ablum);
        ll_music = findViewById(R.id.ll_music);
        ll_video = findViewById(R.id.ll_video);

        iv_error.setOnClickListener(this);
        ll_camera.setOnClickListener(this);
        ll_ablum.setOnClickListener(this);
        ll_music.setOnClickListener(this);
        ll_video.setOnClickListener(this);

        et_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tv_content_size.setText(s.length() + "/140");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_error:
                ll_media_type.setVisibility(View.VISIBLE);
                ll_media.setVisibility(View.GONE);
                uploadFile=null;
                MEDIA_TYPE=SquareSet.PUSH_TEXT;
                break;
            case R.id.ll_camera:
                FileUtil.getInstance().toCamera(PushSquareActivity.this);
                break;
            case R.id.ll_ablum:
                FileUtil.getInstance().toAlbum(PushSquareActivity.this);
                break;
            case R.id.ll_music:
                FileUtil.getInstance().toMusic(PushSquareActivity.this);
                break;
            case R.id.ll_video:
                FileUtil.getInstance().toVideo(PushSquareActivity.this);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE:
                    uploadFile = FileUtil.getInstance().getTempFile();
                    break;
                case ALBUM_REQUEST_CODE:
                case MUSIC_REQUEST_CODE:
                case VIDEO_REQUEST_CODE:
                    //相册，音乐，视频 都是获取一个uri
                    if (data != null) {
                        Uri uri = data.getData();
                        //通过uri获取到真实路径
                        String realPath = FileUtil.getInstance().
                                getRealPathFromUri(PushSquareActivity.this, uri);
                        LogUtils.e(realPath);
                        if (!TextUtils.isEmpty(realPath)) {
                            if (realPath.endsWith(".jpg") ||
                                    realPath.endsWith(".png") ||
                                    realPath.endsWith(".jpeg")) {
                                tv_media_path.setText("图片");
                                MEDIA_TYPE = SquareSet.PUSH_MUSIC;

                            } else if (realPath.endsWith(".mp3")) {
                                tv_media_path.setText("音乐");
                                MEDIA_TYPE = SquareSet.PUSH_IMAGE;

                            } else if (realPath.endsWith("mp4") ||
                                    realPath.endsWith("wav") ||
                                    realPath.endsWith("avi")) {
                                tv_media_path.setText("视频");
                                MEDIA_TYPE = SquareSet.PUSH_VIDEO;
                            }

                            uploadFile = new File(realPath);
                            ll_media_type.setVisibility(View.GONE);
                            ll_media.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.input_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_input){
          inputSquare();
        }
        return true;
    }

    /**
     * 发布动态
     */
    private void inputSquare() {
        final String content = et_content.getText().toString().trim();
        if (TextUtils.isEmpty(content) && uploadFile == null) {
            Toast.makeText(this, "请输入文字或者上传文件", Toast.LENGTH_SHORT).show();
            return;
        }
        mLoadingView.show();
        if(uploadFile != null){
            BmobFile bmobFile=new BmobFile(uploadFile);
            bmobFile.uploadblock(new UploadFileListener() {
                @Override
                public void done(BmobException e) {
                    if(e == null){
                        push(content,bmobFile.getFileUrl());
                    }
                }
            });
        }else{
            push(content,"");
        }
    }

    private void push(String content,String path){
        BmobManager.getInstance().pushSquare(MEDIA_TYPE, content, path, new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                mLoadingView.hide();
                if(e == null){
                    setResult(Activity.RESULT_OK);
                    finish();
                }else{
                    Toast.makeText(PushSquareActivity.this,"发表失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}