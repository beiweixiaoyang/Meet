package com.example.meet.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.meet.cloud.CloudManager;
import com.example.meet.gson.TextBean;
import com.example.meet.utils.LogUtils;
import com.example.meet.utils.SpUtils;
import com.google.gson.Gson;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

public class CloudService extends Service {

    private String token;
    public CloudService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        linkCloudServer();
    }

    /**
     * 连接云服务
     */
    private void linkCloudServer() {
        LogUtils.i("linkCloudServer");
        //获取Token
        token= SpUtils.getInstance().getString("token","");
        CloudManager.getInstance().connectCloud(token);
        CloudManager.getInstance().setOnReceiveMessageListener(new RongIMClient.OnReceiveMessageWrapperListener() {
            @Override
            public boolean onReceived(Message message, int i, boolean b, boolean b1) {
                String objectName=message.getObjectName();
                //文本消息
                if(objectName.equals(CloudManager.MESSAGE_TEXT_NAME)){
                    //获取消息主体
                    TextMessage textMessage= (TextMessage) message.getContent();
                    String content = textMessage.getContent();
                    TextBean textBean = new Gson().fromJson(content, TextBean.class);
                    if(textBean.getType().equals(CloudManager.TYPE_TEXT)){
                        //普通文本消息
                    }else if(textBean.getType().equals(CloudManager.TYPE_ADD_FRIEND)){
                        //添加好友消息,存储到本地·数据库中·
                    }else if(textBean.getType().equals(CloudManager.TYPE_ARGEED_FRIEND)){
                        //同意好友消息
                    }
                }
                return false;
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}