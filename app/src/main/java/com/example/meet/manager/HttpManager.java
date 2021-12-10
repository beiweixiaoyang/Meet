package com.example.meet.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

    /**
     * 下载 是否要增加一个定时清理Meet文件夹的机制？
     * @param url
     * @param saveDir
     * @param listener
     */
    public void download(final String url, final String saveDir, final OnDownloadListener listener) {
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onDownloadFailed(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                //储存下载文件的目录
                //String savePath = isExistDir(saveDir);
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    //不用从url 直接从path
                    File file = new File(saveDir);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        listener.onDownloading(progress);
                    }
                    fos.flush();
                    //下载完成
                    listener.onDownloadSuccess(file.getAbsolutePath());
                } catch (Exception e) {
                    listener.onDownloadFailed(e);
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {

                    }
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {

                    }
                }
            }
        });
    }

    /**
     * 下载进度监听
     */
    public interface OnDownloadListener {
        /**
         * 下载成功
         */
        void onDownloadSuccess(String path);

        /**
         * 下载进度
         *
         * @param progress
         */
        void onDownloading(int progress);

        /**
         * 下载失败
         */
        void onDownloadFailed(Exception e);
    }
}
