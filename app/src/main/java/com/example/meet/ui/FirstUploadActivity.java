package com.example.meet.ui;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.meet.R;
import com.example.meet.base.BaseBackActivity;
import com.example.meet.bmob.BmobManager;
import com.example.meet.manager.DialogManager;
import com.example.meet.utils.FileUtil;
import com.example.meet.utils.LogUtils;
import com.example.meet.view.DialogView;
import com.example.meet.view.LoadingView;

import java.io.File;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 上传头像界面
 */
public class FirstUploadActivity extends BaseBackActivity implements View.OnClickListener {

    private static final String [] CAMERA={Manifest.permission.CAMERA};
    private static final String [] ALBUM={
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    private CircleImageView iv_photo;
    private EditText et_nickname;
    private Button btn_upload;

    private TextView tv_camera;
    private TextView tv_cancel;
    private TextView tv_album;
    private DialogView mSelectPhotoDialog;//选择图片方式的dialog
    private LoadingView mLoadingView;//加载dialog
    private File uploadFile = null;

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
        iv_photo.setOnClickListener(this);
        initSelectPhotoView();
    }

    /**
     * 初始化上传头像dialog
     */
    private void initSelectPhotoView() {
        mSelectPhotoDialog= DialogManager.getInstance().
                initDialogView(this,R.layout.dialog_select_photo, Gravity.BOTTOM);
        mLoadingView=new LoadingView(this);
        tv_camera = mSelectPhotoDialog.findViewById(R.id.tv_camera);
        tv_album = mSelectPhotoDialog.findViewById(R.id.tv_album);
        tv_cancel = mSelectPhotoDialog.findViewById(R.id.tv_cancel);
        tv_camera.setOnClickListener(this);
        tv_album.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);
        et_nickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() > 0){
                    btn_upload.setEnabled(uploadFile != null);
                }else{
                    btn_upload.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_upload:
                uploadPhoto();
                break;
            case R.id.tv_camera:
                DialogManager.getInstance().hideDialog(mSelectPhotoDialog);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestRuntimePermissions(ALBUM, new OnPermissionListener() {
                        @Override
                        public void granted() {
                            FileUtil.getInstance().toCamera(FirstUploadActivity.this);
                        }

                        @Override
                        public void denied(List<String> deniedList) {
                            for(String denied:deniedList){
                                if(denied.equals("android.permission.CAMERA")){
                                    Toast.makeText(FirstUploadActivity.this,

                                            "打开失败，请检查相机权限是否打开",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }else{
                    FileUtil.getInstance().toCamera(FirstUploadActivity.this);
                }
                break;
            case R.id.tv_cancel:
                DialogManager.getInstance().hideDialog(mSelectPhotoDialog);
                break;
            case R.id.tv_album:
                DialogManager.getInstance().hideDialog(mSelectPhotoDialog);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestRuntimePermissions(CAMERA, new OnPermissionListener() {
                        @Override
                        public void granted() {
                            FileUtil.getInstance().toAlbum(FirstUploadActivity.this);
                        }

                        @Override
                        public void denied(List<String> deniedList) {
                            for(String denied:deniedList){
                                if(denied.equals("android.permission.READ_EXTERNAL_STORAGE")){
                                    Toast.makeText(FirstUploadActivity.this,
                                            "打开失败，请检查相机权限是否打开",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }else{
                    FileUtil.getInstance().toAlbum(FirstUploadActivity.this);
                }
                break;
            case R.id.iv_photo:
                DialogManager.getInstance().showDialog(mSelectPhotoDialog);
                break;
        }
    }

    /**
     * 上传头像和昵称到数据库
     */
    private void uploadPhoto() {
        String nickname=et_nickname.getText().toString().trim();
        mLoadingView.show("正在设置，请稍后");
        BmobManager.getInstance().uploadFile(nickname, uploadFile, new BmobManager.OnUploadListener() {
            @Override
            public void onUploadDone() {
                mLoadingView.hide();
                finish();
            }

            @Override
            public void onUploadFailed(BmobException e) {
                mLoadingView.hide();
                Toast.makeText(FirstUploadActivity.this,"设置失败，请稍后再试",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case 1001:
                    uploadFile = FileUtil.getInstance().getTempFile();
                    break;
                case 1111:
                    Uri uri = data.getData();
                    if (uri != null) {
                        LogUtils.i("path:" + uri.getPath());
                        String realPathFromUri =
                                FileUtil.getInstance().getRealPathFromUri(this, uri);
                        if (!TextUtils.isEmpty(realPathFromUri)) {
                            LogUtils.i("realpath:" + realPathFromUri);
                            uploadFile = new File(realPathFromUri);
                        }
                    }
                    break;
            }
        }
        //设置头像
        if (uploadFile != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(uploadFile.getPath());
            iv_photo.setImageBitmap(bitmap);

        }
    }
}