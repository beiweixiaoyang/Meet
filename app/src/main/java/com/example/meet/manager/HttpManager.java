package com.example.meet.manager;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 封装http的相关操作
 */
public class HttpManager {
    private static volatile HttpManager httpManager;

    private static final String CLOUD_SECRET="mgy0VKXJjcW";
    private static final String CLOUD_KEY="sfci50a7si1bi";
    private static final String TOKEN_URL="http://api-cn.ronghub.com/user/getToken.json";

    private OkHttpClient okHttpClient;

    private HttpManager() {
        okHttpClient = new OkHttpClient();
    }

    public static HttpManager getInstance() {
        if (httpManager == null) {
            synchronized (HttpManager.class) {
                if (httpManager == null) {
                    httpManager = new HttpManager();
                }
            }
        }
        return httpManager;
    }

    /**
     * 请求融云Token
     */
    public String postCloudToken(HashMap<String, String> map) {
        //参数
        String Timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String random=String.valueOf(Math.floor(Math.random()*1000000));
        String signature=sha1(CloudManager.CLOUD_SECRET +random+Timestamp);
        //参数填充
        FormBody.Builder builder = new FormBody.Builder();
        for (String key : map.keySet()) {
            builder.add(key, map.get(key));
        }
        RequestBody requestBody = builder.build();
        //添加签名规则
        Request request = new Request.Builder()
                .url(CloudManager.TOKEN_URL)
                .addHeader("Timestamp", Timestamp)
                .addHeader("App-Key",CloudManager.CLOUD_KEY)
                .addHeader("Nonce", random)
                .addHeader("Signature", signature)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .build();
        try {
            return okHttpClient.newCall(request).execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 融云Token的加密算法
     */
    public String sha1(String data){
        StringBuffer buffer=new StringBuffer();
        try{
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(data.getBytes());
            byte[] bits = md.digest();
            for(int i = 0 ; i < bits.length;i++){
                int a = bits[i];
                if(a<0) a+=256;
                if(a<16) buffer.append("0");
                buffer.append(Integer.toHexString(a));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return buffer.toString();
    }
}
