package com.example.meet.cloud;

import android.content.Context;

import com.example.meet.utils.LogUtils;

import org.json.JSONObject;

import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

/**
 * 封装融云的一些相关方法
 */
public class CloudManager {
    //Url
    public static final String TOKEN_URL = "http://api-cn.ronghub.com/user/getToken.json";
    //Key
    public static final String CLOUD_KEY="sfci50a7si1bi";
    public static final String CLOUD_SECRET="mgy0VKXJjcW";

    //消息类型  文本 图片 位置
    public static final String MESSAGE_TEXT_NAME="RC:TxtMsg";
    public static final String MESSAGE_IMAGE_NAME="RC:ImgMsg";
    public static final String MESSAGE_LOCATION_NAME="RC:LBSMsg";


    //消息类型
    //普通消息
    public static final String TYPE_TEXT = "TYPE_TEXT";
    //添加好友消息
    public static final String TYPE_ADD_FRIEND = "TYPE_ADD_FRIEND";
    //同意添加好友的消息
    public static final String TYPE_ARGEED_FRIEND = "TYPE_ARGEED_FRIEND";


    private volatile static CloudManager cloudManager;
    public CloudManager(){}
    public static CloudManager getInstance(){
        if (cloudManager == null) {
            synchronized (CloudManager.class){
                if (cloudManager == null) {
                    cloudManager=new CloudManager();
                }
            }
        }
        return cloudManager;
    }

    /**
     * 连接融云服务
     * @param token token
     * timeLimit：超时时间（秒）。超时后不再重连。取值 <=0 则将一直连接，直到连接成功或者发生业务错误。
     * RongIMClient.ConnectCallback：连接回调
     */
    public void connectCloud(String token){

        RongIMClient.connect(token, 0, new RongIMClient.ConnectCallback() {
            @Override
            public void onSuccess(String t) {
                LogUtils.i("融云连接成功");
            }

            @Override
            public void onError(RongIMClient.ConnectionErrorCode e) {
                LogUtils.e("融云连接失败");
            }

            @Override
            public void onDatabaseOpened(RongIMClient.DatabaseOpenStatus code) {
                if(RongIMClient.DatabaseOpenStatus.DATABASE_OPEN_SUCCESS.equals(code)) {
                    //本地数据库打开，跳转到会话列表页面
                } else {
                    //数据库打开失败，可以弹出 toast 提示。
                }
            }
        });
    }
    /**
     * 断开融云连接
     */
    public void disconnect(){
        RongIMClient.getInstance().disconnect();
    }

    /**
     * 注销登录信息
     */
    public void logout(){
        RongIMClient.getInstance().logout();
    }
    /**
     * 添加消息监听器
     */
    public void setOnReceiveMessageListener(RongIMClient.OnReceiveMessageWrapperListener listener){
        RongIMClient.setOnReceiveMessageListener(listener);
    }

    private IRongCallback.ISendMessageCallback iSendMessageCallback=
            new IRongCallback.ISendMessageCallback() {
                @Override
                public void onAttached(Message message) {
                    //消息存储到数据库
                    LogUtils.i("onAttached");
                }

                @Override
                public void onSuccess(Message message) {
                    LogUtils.i("消息发送成功"+message);
                }

                @Override
                public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                    LogUtils.e("消息发送失败"+errorCode);
                }
            };

    /**
     * 发送文本消息
     * @param msg 文本消息
     * @param targetId 目标id
     */
    public void sendTextMessage(String msg,String targetId){
        TextMessage textMessage=TextMessage.obtain(msg);
        RongIMClient.getInstance().sendMessage(Conversation.ConversationType.PRIVATE,
                targetId,
                textMessage,
                null,
                null,
                iSendMessageCallback);
    }

    /**
     * 发送文本消息
     * @param msg 文本内容
     * @param type 消息类型
     * @param targetId 目标Id
     */
    public void sendTextMessage(String msg,String type,String targetId){
        JSONObject jsonObject=new JSONObject();
        try{
            jsonObject.put("msg", msg);
            //如果没有这个Type 就是一条普通消息
            jsonObject.put("type", type);
            sendTextMessage(jsonObject.toString(), targetId);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}