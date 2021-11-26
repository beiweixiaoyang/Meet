package com.example.meet.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 文件操作工具类
 */
public class FileUtil {


    public static final int CAMERA_REQUEST_CODE = 1110;
    public static final int ALBUM_REQUEST_CODE = 1111;
    private SimpleDateFormat simpleDateFormat;

    private static FileUtil instance;

    private File tempFile=null;
    private Uri imageUri;

    public FileUtil() {
        simpleDateFormat=new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
    }

    public static FileUtil getInstance() {
        if (instance == null) {
            synchronized (FileUtil.class) {
                if (instance == null) {
                    instance = new FileUtil();
                }
            }
        }
        return instance;
    }
    public File getTempFile(){
        return tempFile;
    }

    /**
     * 跳转到相机
     */

    public void toCamera(Activity activity){
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        activity.startActivity(intent);
        String filename=simpleDateFormat.format(new Date());
        tempFile=new File(Environment.getExternalStorageDirectory(),filename+".jpg");
        LogUtils.i("Directory:"+Environment.getExternalStorageDirectory());
        //兼容android7.0
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.N){
            imageUri=Uri.fromFile(tempFile);
        }else{
            //7.0之后利用FileProvider
            imageUri= FileProvider.getUriForFile
                    (activity,activity.getPackageName()+".fileprovider",tempFile);
            //添加权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        activity.startActivityForResult(intent,CAMERA_REQUEST_CODE);
    }

    /**
     * 跳转到相册
     */
    public void toAlbum(Activity activity){
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activity.startActivityForResult(intent,ALBUM_REQUEST_CODE);
    }

    /**
     * 获取相册中图片的真实uri
     */
    public String getRealPathFromUri(Context context, Uri uri){
        String  realPath="";
        String [] proj={MediaStore.Images.Media.DATA};
        LogUtils.i(MediaStore.Images.Media.DATA);
        CursorLoader cursorLoader=
                new CursorLoader(context,uri,proj,null,null,null);
        Cursor cursor = cursorLoader.loadInBackground();
        if(cursor != null){
            if(cursor.moveToFirst()){
                int index=cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                realPath=cursor.getString(index);
            }
        }
        return realPath;
    }
}
