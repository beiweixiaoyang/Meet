package com.example.meet.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    /**
     * 保存图片到相册
     * @param mBitmap
     */
    public static void saveBitmapToAlbum(Context context,Bitmap mBitmap) {
        File rootPath=new File(Environment.getExternalStorageDirectory().getPath());//根路径
        if(!rootPath.exists()){
            rootPath.mkdirs();//创建目录
        }
        File file=new File(rootPath,System.currentTimeMillis()+".png");
        try {
            FileOutputStream outputStream=new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.PNG,90,outputStream);
            outputStream.flush();
            outputStream.close();
            Toast.makeText(context,"已保存到相册",Toast.LENGTH_SHORT).show();
            updateAlbum(context,file.getPath());
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(context,"保存失败",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 更新图库
     */
    private static void updateAlbum(Context context,String path) {
        //通过广播的方式更新图库
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(path)));

        //通过数据库的方式插入
        ContentValues values = new ContentValues(4);
        values.put(MediaStore.Video.Media.TITLE, "");
        values.put(MediaStore.Video.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Video.Media.DATA, path);
        values.put(MediaStore.Video.Media.DURATION, 0);
        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }
}
