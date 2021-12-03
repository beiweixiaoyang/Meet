package com.example.meet.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.meet.R;
import com.example.meet.bmob.BmobManager;
import com.example.meet.bmob.MeetUser;
import com.example.meet.manager.CloudManager;
import com.example.meet.event.EventManager;
import com.example.meet.event.MessageEvent;
import com.example.meet.gson.TextBean;
import com.example.meet.manager.LitePalManager;
import com.example.meet.manager.WindowHelper;
import com.example.meet.utils.LogUtils;
import com.example.meet.utils.SpUtils;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.disposables.Disposable;
import io.rong.calllib.IRongCallListener;
import io.rong.calllib.IRongReceivedCallListener;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.location.message.LocationMessage;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;

public class CloudService extends Service implements View.OnClickListener{

    private String token;
    private Disposable disposable;

    //音频窗口
    private View mFullAudioView;
    //头像
    private CircleImageView audio_iv_photo;
    //状态
    private TextView audio_tv_status;
    //录音图片
    private ImageView audio_iv_recording;
    //录音按钮
    private LinearLayout audio_ll_recording;
    //接听图片
    private ImageView audio_iv_answer;
    //接听按钮
    private LinearLayout audio_ll_answer;
    //挂断图片
    private ImageView audio_iv_hangup;
    //挂断按钮
    private LinearLayout audio_ll_hangup;
    //免提图片
    private ImageView audio_iv_hf;
    //免提按钮
    private LinearLayout audio_ll_hf;
    //最小化
    private ImageView audio_iv_small;

    //视频窗口
    private View mFullVideoView;
    //大窗口
    private RelativeLayout video_big_video;
    //小窗口
    private RelativeLayout video_small_video;
    //头像
    private CircleImageView video_iv_photo;
    //昵称
    private TextView video_tv_name;
    //状态
    private TextView video_tv_status;
    //个人信息窗口
    private LinearLayout video_ll_info;
    //时间
    private TextView video_tv_time;
    //接听
    private LinearLayout video_ll_answer;
    //挂断
    private LinearLayout video_ll_hangup;

    //最小化的音频View
    private WindowManager.LayoutParams lpSmallView;
    private View mSmallAudioView;
    //时间
    private TextView mSmallTime;

    //通话ID
    private String callId = "";

    public CloudService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initWindow();
        linkCloudServer();
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


    /**
     * 初始化窗口
     */
    private void initWindow() {
        mFullAudioView = WindowHelper.getInstance().getView(R.layout.layout_chat_audio);
        audio_iv_photo = mFullAudioView.findViewById(R.id.audio_iv_photo);
        audio_tv_status = mFullAudioView.findViewById(R.id.audio_tv_status);
        audio_iv_recording = mFullAudioView.findViewById(R.id.audio_iv_recording);
        audio_ll_recording = mFullAudioView.findViewById(R.id.audio_ll_recording);
        audio_iv_answer = mFullAudioView.findViewById(R.id.audio_iv_answer);
        audio_ll_answer = mFullAudioView.findViewById(R.id.audio_ll_answer);
        audio_iv_hangup = mFullAudioView.findViewById(R.id.audio_iv_hangup);
        audio_ll_hangup = mFullAudioView.findViewById(R.id.audio_ll_hangup);
        audio_iv_hf = mFullAudioView.findViewById(R.id.audio_iv_hf);
        audio_ll_hf = mFullAudioView.findViewById(R.id.audio_ll_hf);
        audio_iv_small = mFullAudioView.findViewById(R.id.audio_iv_small);

        audio_ll_recording.setOnClickListener(this);
        audio_ll_answer.setOnClickListener(this);
        audio_ll_hangup.setOnClickListener(this);
        audio_ll_hf.setOnClickListener(this);
        audio_iv_small.setOnClickListener(this);

        //视频
        mFullVideoView = WindowHelper.getInstance().getView(R.layout.layout_chat_video);
        video_big_video = mFullVideoView.findViewById(R.id.video_big_video);
        video_small_video = mFullVideoView.findViewById(R.id.video_small_video);
        video_iv_photo = mFullVideoView.findViewById(R.id.video_iv_photo);
        video_tv_name = mFullVideoView.findViewById(R.id.video_tv_name);
        video_tv_status = mFullVideoView.findViewById(R.id.video_tv_status);
        video_ll_info = mFullVideoView.findViewById(R.id.video_ll_info);
        video_tv_time = mFullVideoView.findViewById(R.id.video_tv_time);
        video_ll_answer = mFullVideoView.findViewById(R.id.video_ll_answer);
        video_ll_hangup = mFullVideoView.findViewById(R.id.video_ll_hangup);

        video_ll_answer.setOnClickListener(this);
        video_ll_hangup.setOnClickListener(this);
        video_small_video.setOnClickListener(this);
    }

    /**
     * 连接云服务
     */
    private void linkCloudServer() {
        LogUtils.i("linkCloudServer");
        //获取Token
        token = SpUtils.getInstance().getString("token", "");
        CloudManager.getInstance().connectCloud(token);
        //消息监听
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
        //来电监听
        CloudManager.getInstance().setReceivedCallListener(new IRongReceivedCallListener() {
            @Override
            public void onReceivedCall(RongCallSession callSession) {
                String callId = callSession.getCallId();//通话id
                String callerUserId = callSession.getCallerUserId();//呼叫方id
                String targetId = callSession.getTargetId();//接收方id
                updateWindowInfo(1,callerUserId);
                if(callSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)){
                    LogUtils.i("被叫方语音通话");
                    WindowHelper.getInstance().showView(mFullAudioView);
                }else if(callSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)){
                    LogUtils.i("被叫方视频通话");
                }
            }

            @Override
            public void onCheckPermission(RongCallSession callSession) {

            }
        });
        //通话状态监听
        CloudManager.getInstance().setVoIPCallListener(new IRongCallListener() {
            @Override
            public void onCallOutgoing(RongCallSession callSession, SurfaceView localVideo) {
                LogUtils.i("onCallOutgoing");
                String targetId = callSession.getTargetId();
                updateWindowInfo(0,targetId);
                if(callSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)){
                    LogUtils.i("呼叫方语音通话");
                    WindowHelper.getInstance().showView(mFullAudioView);
                }else if(callSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)){
                    LogUtils.i("呼叫方视频通话");
                }
            }

            @Override
            public void onCallConnected(RongCallSession callSession, SurfaceView localVideo) {
                LogUtils.i("onCallConnected");
            }

            @Override
            public void onCallDisconnected(RongCallSession callSession, RongCallCommon.CallDisconnectedReason reason) {
                LogUtils.i("onCallDisconnected");
            }

            @Override
            public void onRemoteUserRinging(String userId) {

            }

            @Override
            public void onRemoteUserAccept(String userId, RongCallCommon.CallMediaType mediaType) {

            }

            @Override
            public void onRemoteUserJoined(String userId, RongCallCommon.CallMediaType mediaType, int userType, SurfaceView remoteVideo) {

            }

            @Override
            public void onRemoteUserInvited(String userId, RongCallCommon.CallMediaType mediaType) {

            }

            @Override
            public void onRemoteUserLeft(String userId, RongCallCommon.CallDisconnectedReason reason) {

            }

            @Override
            public void onMediaTypeChanged(String userId, RongCallCommon.CallMediaType mediaType, SurfaceView video) {

            }

            @Override
            public void onError(RongCallCommon.CallErrorCode errorCode) {

            }

            @Override
            public void onRemoteCameraDisabled(String userId, boolean disabled) {

            }

            @Override
            public void onRemoteMicrophoneDisabled(String userId, boolean disabled) {

            }

            @Override
            public void onNetworkReceiveLost(String userId, int lossRate) {

            }

            @Override
            public void onNetworkSendLost(int lossRate, int delay) {

            }

            @Override
            public void onFirstRemoteVideoFrame(String userId, int height, int width) {

            }

            @Override
            public void onAudioLevelSend(String audioLevel) {

            }

            @Override
            public void onAudioLevelReceive(HashMap<String, String> audioLevel) {

            }

            @Override
            public void onRemoteUserPublishVideoStream(String userId, String streamId, String tag, SurfaceView surfaceView) {

            }

            @Override
            public void onRemoteUserUnpublishVideoStream(String userId, String streamId, String tag) {

            }
        });
    }

    /**
     * 更新窗口上的用户信息
     * @param index 0：呼叫方 1：被叫方
     * @param objectId
     */
    private void updateWindowInfo(int index,String objectId) {
        BmobManager.getInstance().queryByObjectId(objectId, new FindListener<MeetUser>() {
            @Override
            public void done(List<MeetUser> list, BmobException e) {
                if(e == null){
                    if(list.size() > 0){
                        MeetUser meetUser = list.get(0);
                        Glide.with(CloudService.this)
                                .load(meetUser.getPhoto())
                                .into(audio_iv_photo);
                        if(index == 0){
                            audio_tv_status.setText("正在呼叫"+" "+meetUser.getNickName());
                        }else if(index == 1){
                            audio_tv_status.setText(meetUser.getNickName()+"的来电");
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.audio_ll_recording:
                //录音
                break;
            case R.id.audio_ll_answer:
                //接听
                break;
            case R.id.audio_ll_hangup:
                //挂断
                break;
            case R.id.audio_ll_hf:
                //免提
                break;
            case R.id.audio_iv_small:
//                最小化
                break;
        }
    }
}