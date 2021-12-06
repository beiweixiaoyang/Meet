package com.example.meet.manager;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.example.meet.utils.LogUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.rong.calllib.IRongCallListener;
import io.rong.calllib.IRongReceivedCallListener;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.location.message.LocationMessage;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;

/**
 * 封装融云的一些相关方法
 */
public class CloudManager {
    //Url
    public static final String TOKEN_URL = "http://api-cn.ronghub.com/user/getToken.json";
    //Key
    public static final String CLOUD_KEY = "sfci50a7si1bi";
    public static final String CLOUD_SECRET = "mgy0VKXJjcW";

    //消息类型  文本 图片 位置
    public static final String MESSAGE_TEXT_NAME = "RC:TxtMsg";
    public static final String MESSAGE_IMAGE_NAME = "RC:ImgMsg";
    public static final String MESSAGE_LOCATION_NAME = "RC:LBSMsg";


    //消息类型
    //普通消息
    public static final String TYPE_TEXT = "TYPE_TEXT";
    //添加好友消息
    public static final String TYPE_ADD_FRIEND = "TYPE_ADD_FRIEND";
    //同意添加好友的消息
    public static final String TYPE_ARGEED_FRIEND = "TYPE_ARGEED_FRIEND";

    //来电铃声
    public static final String callAudioPath = "http://downsc.chinaz.net/Files/DownLoad/sound1/201501/5363.wav";
    //挂断铃声
    public static final String callAudioHangup = "http://downsc.chinaz.net/Files/DownLoad/sound1/201501/5351.wav";


    private volatile static CloudManager cloudManager;

    public CloudManager() {
    }

    public static CloudManager getInstance() {
        if (cloudManager == null) {
            synchronized (CloudManager.class) {
                if (cloudManager == null) {
                    cloudManager = new CloudManager();
                }
            }
        }
        return cloudManager;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void initCloud(Context context) {
        RongIMClient.init(context, CloudManager.CLOUD_KEY);
    }

    /**
     * 连接融云服务
     *
     * @param token token
     *              timeLimit：超时时间（秒）。超时后不再重连。取值 <=0 则将一直连接，直到连接成功或者发生业务错误。
     *              RongIMClient.ConnectCallback：连接回调
     */
    public void connectCloud(String token) {

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
                if (RongIMClient.DatabaseOpenStatus.DATABASE_OPEN_SUCCESS.equals(code)) {
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
    public void disconnect() {
        RongIMClient.getInstance().disconnect();
    }

    /**
     * 注销登录信息
     */
    public void logout() {
        RongIMClient.getInstance().logout();
    }

    //------------------------------即时通讯--------------------------------------------

    /**
     * 添加消息监听器
     */
    public void setOnReceiveMessageListener(RongIMClient.OnReceiveMessageWrapperListener listener) {
        RongIMClient.setOnReceiveMessageListener(listener);
    }

    private RongIMClient.SendImageMessageCallback messageCallback = new RongIMClient.SendImageMessageCallback() {
        @Override
        public void onAttached(Message message) {

        }

        @Override
        public void onError(Message message, RongIMClient.ErrorCode errorCode) {

        }

        @Override
        public void onSuccess(Message message) {

        }

        @Override
        public void onProgress(Message message, int i) {

        }
    };

    private IRongCallback.ISendMessageCallback iSendMessageCallback =
            new IRongCallback.ISendMessageCallback() {
                @Override
                public void onAttached(Message message) {
                    //消息存储到数据库
                    LogUtils.i("onAttached");
                }

                @Override
                public void onSuccess(Message message) {
                    LogUtils.i("消息发送成功" + message);
                }

                @Override
                public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                    LogUtils.e("消息发送失败" + errorCode);
                }
            };

    /**
     * 发送文本消息
     *
     * @param msg      文本消息
     * @param targetId 目标id
     */
    private void sendTextMessage(String msg, String targetId) {
        TextMessage textMessage = TextMessage.obtain(msg);
        RongIMClient.getInstance().sendMessage(Conversation.ConversationType.PRIVATE,
                targetId,
                textMessage,
                null,
                null,
                iSendMessageCallback);
    }

    /**
     * 发送文本消息
     *
     * @param msg      文本内容
     * @param type     消息类型
     * @param targetId 目标Id
     */
    public void sendTextMessage(String msg, String type, String targetId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("msg", msg);
            //如果没有这个Type 就是一条普通消息
            jsonObject.put("type", type);
            sendTextMessage(jsonObject.toString(), targetId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 发送图片消息
     *
     * @param file     发送的文件
     * @param targetId 目标id
     */
    public void sendImageMessage(File file, String targetId) {
        ImageMessage imageMessage = ImageMessage.obtain(Uri.fromFile(file), Uri.fromFile(file));
        RongIMClient.getInstance().sendImageMessage(Conversation.ConversationType.PRIVATE,
                targetId,
                imageMessage,
                null,
                null, messageCallback);
    }

    public void sendLocationMessage(String targetId, double lat, double lnt, String poi) {
        LocationMessage locationMessage = LocationMessage.obtain(lat, lnt, poi, null);
        Message message = Message.obtain(targetId, Conversation.ConversationType.PRIVATE, locationMessage);
        RongIMClient.getInstance().sendLocationMessage(message, null, null, iSendMessageCallback);
    }

    /**
     * 查询本地会话记录
     *
     * @param callback 回调函数
     */
    public void getConversationList(RongIMClient.ResultCallback<List<Conversation>> callback) {
        RongIMClient.getInstance().getConversationList(callback);
    }

    /**
     * 获取指定目标的会话记录
     *
     * @param targetId 目标id
     * @param callback 回调
     */
    public void getHistoryMessages(String targetId, RongIMClient.ResultCallback<List<Message>> callback) {
        RongIMClient.getInstance().getHistoryMessages(Conversation.ConversationType.PRIVATE, targetId,
                -1, 1000, callback);
    }

    /**
     * 获取服务器的历史记录
     *
     * @param targetId
     * @param callback
     */
    public void getRemoteHistoryMessages(String targetId, RongIMClient.ResultCallback<List<Message>> callback) {
        RongIMClient.getInstance().getRemoteHistoryMessages(Conversation.ConversationType.PRIVATE, targetId,
                0, 20, callback);
    }


    //------------------------------音视频童话--------------------------------------------


    /**
     * 发起语音或者视频通话
     * @param targetId 目标id
     * @param mediaType 电话类型 （AUDIO：0，VIDEO：1）
     */
    public void startCall(Context context,String targetId, RongCallCommon.CallMediaType mediaType) {
        //检查设备是否可用
        if(!isVoIPEnabled(context)){
            Toast.makeText(context,"当前设备不可用",Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> usersId = new ArrayList<>();
        usersId.add(targetId);
        RongCallClient.getInstance().startCall(Conversation.ConversationType.PRIVATE,
                targetId,
                usersId,
                null,
                mediaType,
                null);
    }

    /**
     * 来电监听
     * @param listener
     */
    public void setReceivedCallListener(IRongReceivedCallListener listener){
        RongCallClient.setReceivedCallListener(listener);
    }

    /**
     * 通话状态的监听
     * @param callListener
     */
    public void setVoIPCallListener(IRongCallListener callListener){
        RongCallClient.getInstance().setVoIPCallListener(callListener);
    }

    /**
     * 接听通话
     * @param callId
     */
    public void acceptCall(String callId){
        RongCallClient.getInstance().acceptCall(callId);
    }

    /**
     * 挂断
     * @param callId
     */
    public void hangUpCall(String callId) {
        LogUtils.i("hangUpCall:" + callId);
        RongCallClient.getInstance().hangUpCall(callId);
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        RongCallClient.getInstance().switchCamera();
    }

    /**
     * 摄像头开关
     *
     * @param enabled
     */
    public void setEnableLocalVideo(boolean enabled) {
        RongCallClient.getInstance().setEnableLocalVideo(enabled);
    }

    /**
     * 音频开关
     *
     * @param enabled
     */
    public void setEnableLocalAudio(boolean enabled) {
        RongCallClient.getInstance().setEnableLocalAudio(enabled);
    }

    /**
     * 免提开关
     *
     * @param enabled
     */
    public void setEnableSpeakerphone(boolean enabled) {
        RongCallClient.getInstance().setEnableSpeakerphone(enabled);
    }

    /**
     * 检查设备是否可用通话
     *
     * @param mContext
     */
    public boolean isVoIPEnabled(Context mContext) {
        if (!RongCallClient.getInstance().isVoIPEnabled(mContext)) {
            Toast.makeText(mContext, "设备不支持音视频通话", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


}
