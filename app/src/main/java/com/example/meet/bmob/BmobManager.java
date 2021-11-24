package com.example.meet.bmob;

import android.content.Context;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * 对bmob的一些函数进行封装
 */
public class BmobManager {

    private static BmobManager instance;

    private static final String BMOB_ID = "c78dc48c4a7dc77d74d061b2f8bc662e";

    private BmobManager() {
    }

    public static BmobManager getInstance() {
        if (instance == null) {
            synchronized (BmobManager.class) {
                if (instance == null) {
                    instance = new BmobManager();
                }
            }
        }
        return instance;
    }

    public void initBmob(Context context) {
        Bmob.initialize(context, BMOB_ID);
    }

    /**
     * 请求发送短信验证码
     * @param phone 电话号码
     * @param listener 回调函数
     */
    public void requestSMSCode(String phone, QueryListener<Integer> listener) {
        BmobSMS.requestSMSCode(phone, "", listener);
    }

    /**
     * 验证短信验证码
     * @param phone 电话号码
     * @param SMSCode 短信验证码
     * @param listener 回调函数
     */
    public void verifySmsCode(String phone, String SMSCode, UpdateListener listener){
        BmobSMS.verifySmsCode(phone,SMSCode,listener);
    }
}
