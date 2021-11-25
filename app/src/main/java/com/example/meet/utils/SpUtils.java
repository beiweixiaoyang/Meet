package com.example.meet.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * sp工具类，提供一些对sp操作的方法
 */
public class SpUtils {

    private static SpUtils instance;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SpUtils() {
    }

    public static SpUtils getInstance() {
        if (instance == null) {
            synchronized (SpUtils.class) {
                if (instance == null) {
                    instance = new SpUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化SpUtils类
     * MODE_PRIVATE：只限于本应用读写
     * MODE_WORLD_READABLE:支持其他应用读，但是不能写
     * MODE_WORLD_WRITEABLE:其他应用可以写
     */
    public void initSpUtils(Context context){
        LogUtils.i("init-->SpUtils");
        sharedPreferences=context.getSharedPreferences("Meet",Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
    }

    public void putInt(String key,int values){
        editor.putInt(key, values);
        editor.commit();
    }
    public int getInt(String key,int defValues){
        return sharedPreferences.getInt(key, defValues);
    }
    public void putString(String key,String values ){
        editor.putString(key, values);
        editor.commit();
    }
    public String getString(String key,String defValues){
        return sharedPreferences.getString(key, defValues);
    }
    public void putBoolean(String key,boolean values){
        editor.putBoolean(key, values);
        editor.commit();
    }
    public boolean getBoolean(String key,boolean defValues){
        return sharedPreferences.getBoolean(key, defValues);
    }
    public void deleteKey(String key){
        editor.remove(key);
        editor.commit();
    }

}
