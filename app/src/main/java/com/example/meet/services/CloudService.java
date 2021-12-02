package com.example.meet.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.meet.bmob.BmobManager;
import com.example.meet.cloud.CloudManager;
import com.example.meet.event.EventManager;
import com.example.meet.event.MessageEvent;
import com.example.meet.gson.TextBean;
import com.example.meet.litepal.LitePalManager;
import com.example.meet.litepal.NewFriend;
import com.example.meet.utils.LogUtils;
import com.example.meet.utils.SpUtils;
import com.google.gson.Gson;

import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.location.message.LocationMessage;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;

public class CloudService extends Service {

    private String token;
    private Disposable disposable;

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
        token = SpUtils.getInstance().getString("token", "");
        CloudManager.getInstance().connectCloud(token);
        CloudManager.getInstance().setOnReceiveMessageListener(new RongIMClient.OnReceiveMessageWrapperListener() {
            @Override
            public boolean onReceived(Message message, int i, boolean b, boolean b1) {
                String objectName = message.getObjectName();
                //文本消息
                if (objectName.equals(CloudManager.MESSAGE_TEXT_NAME)) {
                    //获取消息主体
                    TextMessage textMessage = (TextMessage) message.getContent();
                    String content = textMessage.getContent();
                    TextBean textBean = new Gson().fromJson(content, TextBean.class);
                    if (textBean.getType().equals(CloudManager.TYPE_TEXT)) {
                        //普通文本消息
                        MessageEvent event=new MessageEvent(EventManager.FLAG_SEND_TEXT);
                        event.setUserId(message.getSenderUserId());
                        event.setText(textBean.getMsg());
                        EventManager.post(event);
                    } else if (textBean.getType().equals(CloudManager.TYPE_ADD_FRIEND)) {
                        //查询本地数据库，如果有重复的则不添加
                        LitePalManager.getInstance().
                                saveNewFriend(textBean.getMsg(), message.getSenderUserId());
                    } else if (textBean.getType().equals(CloudManager.TYPE_ARGEED_FRIEND)) {
                        //同意添加好友消息
                        BmobManager.getInstance().addFriend(message.getSenderUserId(), new SaveListener<String>() {
                            @Override
                            public void done(String s, BmobException e) {
                                if(e == null){
                                    //刷新好友列表
                                    LogUtils.i(s);
                                }
                            }
                        });
                    }
                }else if(objectName.equals(CloudManager.MESSAGE_IMAGE_NAME)){
                    ImageMessage imageMessage= (ImageMessage) message.getContent();
                    String url = imageMessage.getRemoteUri().toString();
                    if (!TextUtils.isEmpty(url)) {
                        MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_IMAGE);
                        event.setImgUrl(url);
                        event.setUserId(message.getSenderUserId());
                        EventManager.post(event);
                    }
                }else if(objectName.equals(CloudManager.MESSAGE_LOCATION_NAME)){
                    LocationMessage locationMessage= (LocationMessage) message.getContent();
                    MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_LOCATION);
                    event.setLa(locationMessage.getLat());
                    event.setLo(locationMessage.getLng());
                    event.setAddress(locationMessage.getPoi());
                    event.setUserId(message.getSenderUserId());
                    EventManager.post(event);
                }
                return false;
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(disposable.isDisposed()){
            disposable.dispose();
        }
    }
}