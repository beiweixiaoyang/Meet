package com.example.meet.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.meet.R;
import com.example.meet.bmob.BmobManager;
import com.example.meet.bmob.MeetUser;
import com.example.meet.manager.CloudManager;
import com.example.meet.event.EventManager;
import com.example.meet.event.MessageEvent;
import com.example.meet.gson.TextBean;
import com.example.meet.manager.LitePalManager;
import com.example.meet.manager.MediaPlayerManager;
import com.example.meet.manager.WindowHelper;
import com.example.meet.utils.LogUtils;
import com.example.meet.utils.SpUtils;
import com.example.meet.utils.TimeUtil;
import com.google.gson.Gson;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

public class CloudService extends Service implements View.OnClickListener {


    private static final int TIME_HANDLER = 2000;
    private int callTime = 0;//通话时间
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull android.os.Message msg) {
            switch (msg.what) {
                case TIME_HANDLER:
                    callTime++;
                    String time = TimeUtil.formatDuring(callTime * 1000);
                    audio_tv_status.setText(time);
                    video_tv_time.setText(time);
                    mSmallTime.setText(time);
                    mHandler.sendEmptyMessageDelayed(TIME_HANDLER, 1000);
                    break;
            }
            return false;
        }
    });
    private String token;
    private Disposable disposable;

    //--------音频窗口--------
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

    //--------视频窗口--------
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

    //来电铃声
    private MediaPlayerManager mAudioCallManager;
    //挂断铃声
    private MediaPlayerManager mAudioHangUpManager;

    //摄像类
    private SurfaceView mLocalView;//本地surface
    private SurfaceView mRemoteView;//对方surface

    //是否小窗口显示本地视频
    private boolean isSmallShowLocal = false;

    //拨打状态
    private int isCallTo = 0;
    //接听状态
    private int isReceiverTo = 0;
    //拨打还是接听
    private boolean isCallOrReceiver = true;

    public CloudService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventManager.register(this);
        initService();
        initWindow();
        linkCloudServer();
    }

    private void initService() {
        mAudioCallManager = new MediaPlayerManager();
        mAudioHangUpManager = new MediaPlayerManager();
        //循环播放来电铃声
        mAudioCallManager.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mAudioCallManager.startPlay(CloudManager.callAudioPath);
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
        EventManager.unregister(this);
        if (disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent messageEvent) {
        switch (messageEvent.getType()){
            case EventManager.FLAG_SEND_CAMERA_VIEW:
                SurfaceView sv = messageEvent.getmSurfaceView();
                if (sv != null) {
                    mRemoteView = sv;
                }
                updateVideoView();
                break;
        }
    }


    /**
     * 初始化窗口
     */
    private void initWindow() {
        //语音通话
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

        //视频通话
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
                parseMessage(message);
                return false;
            }
        });
        //设置来电监听器
        CloudManager.getInstance().setReceivedCallListener(iRongReceivedCallListener);
        //设置通话状态监听器
        CloudManager.getInstance().setVoIPCallListener(iRongCallListener);
    }

    /**
     * 来电监听器
     */
    private IRongReceivedCallListener iRongReceivedCallListener = new IRongReceivedCallListener() {
        @Override
        public void onReceivedCall(RongCallSession callSession) {
            //检查设备可用
            if (!CloudManager.getInstance().isVoIPEnabled(CloudService.this)) {
                return;
            }
            callId = callSession.getCallId();//通话id
            String callerUserId = callSession.getCallerUserId();//呼叫方id
            //播放来电铃声
            mAudioCallManager.startPlay(CloudManager.callAudioPath);
            updateWindowInfo(1, callSession.getMediaType(), callerUserId);
            if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                LogUtils.i("被叫方语音通话");
                WindowHelper.getInstance().showView(mFullAudioView);
            } else if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                LogUtils.i("被叫方视频通话");
                WindowHelper.getInstance().showView(mFullVideoView);
            }
        }

        @Override
        public void onCheckPermission(RongCallSession callSession) {

        }
    };
    /**
     * 通话状态监听
     */
    private IRongCallListener iRongCallListener = new IRongCallListener() {

        @Override
        public void onCallOutgoing(RongCallSession callSession, SurfaceView localVideo) {
            //电话拨出
            LogUtils.i("onCallOutgoing");
            String targetId = callSession.getTargetId();
            callId = callSession.getCallId();
            updateWindowInfo(0, callSession.getMediaType(), targetId);
            if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                LogUtils.i("呼叫方语音通话");
                WindowHelper.getInstance().showView(mFullAudioView);
            } else if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                LogUtils.i("呼叫方视频通话");
                WindowHelper.getInstance().showView(mFullVideoView);
                //显示摄像头
                mLocalView = localVideo;
                video_big_video.addView(mLocalView);
            }
        }

        @Override
        public void onCallConnected(RongCallSession callSession, SurfaceView localVideo) {
            //建立通话
            LogUtils.i("onCallConnected");
            /**
             * 1.通话时长开始计时
             * 2.关闭来电铃声
             * 3.更新按钮状态
             */
            //关闭铃声
            if (mAudioCallManager.isPlaying()) {
                mAudioCallManager.stopPlay();
            }
            //开始计时
            mHandler.sendEmptyMessage(TIME_HANDLER);
            //更新按钮状态
            if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                goneAudioView(true, false, true, true, true);
            } else if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                goneVideoView(false, true, true, false, true, true);
                mLocalView = localVideo;
            }
        }

        @Override
        public void onCallDisconnected(RongCallSession callSession, RongCallCommon.CallDisconnectedReason reason) {
            //通话断开连接
            LogUtils.i("onCallDisconnected");
            String callUserId = callSession.getCallerUserId();//自身id
            String recevierId = callSession.getTargetId();//对方id
            //关闭计时
            mHandler.removeMessages(TIME_HANDLER);
            //重置计时时间
            callTime = 0;
            //播放挂断音乐
            mAudioCallManager.stopPlay();
            mAudioCallManager.startPlay(CloudManager.callAudioHangup);
            //隐藏window窗口
            WindowHelper.getInstance().hideView(mFullAudioView);
            WindowHelper.getInstance().hideView(mFullVideoView);
        }

        @Override
        public void onRemoteUserRinging(String userId) {
            //被叫端正在响铃
        }

        @Override
        public void onRemoteUserAccept(String userId, RongCallCommon.CallMediaType mediaType) {
            //通话中某一个好友邀请其他人加入
        }

        @Override
        public void onRemoteUserJoined(String userId, RongCallCommon.CallMediaType mediaType, int userType, SurfaceView remoteVideo) {
            //被叫方加入视频
            MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_CAMERA_VIEW);
            event.setmSurfaceView(remoteVideo);
            EventManager.post(event);
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

    };

    /**
     * 解析音视频之外的消息
     * 文本消息，图片消息，地理位置消息，添加同意好友消息
     *
     * @param message
     */
    private void parseMessage(Message message) {
        String objectName = message.getObjectName();
        //文本消息
        if (objectName.equals(CloudManager.MESSAGE_TEXT_NAME)) {
            //获取消息主体
            TextMessage textMessage = (TextMessage) message.getContent();
            String content = textMessage.getContent();
            TextBean textBean = new Gson().fromJson(content, TextBean.class);
            if (textBean.getType().equals(CloudManager.TYPE_TEXT)) {
                //普通文本消息
                MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_TEXT);
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
                        if (e == null) {
                            //刷新好友列表
                            LogUtils.i(s);
                        }
                    }
                });
            }
        } else if (objectName.equals(CloudManager.MESSAGE_IMAGE_NAME)) {
            ImageMessage imageMessage = (ImageMessage) message.getContent();
            String url = imageMessage.getRemoteUri().toString();
            if (!TextUtils.isEmpty(url)) {
                MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_IMAGE);
                event.setImgUrl(url);
                event.setUserId(message.getSenderUserId());
                EventManager.post(event);
            }
        } else if (objectName.equals(CloudManager.MESSAGE_LOCATION_NAME)) {
            LocationMessage locationMessage = (LocationMessage) message.getContent();
            MessageEvent event = new MessageEvent(EventManager.FLAG_SEND_LOCATION);
            event.setLa(locationMessage.getLat());
            event.setLo(locationMessage.getLng());
            event.setAddress(locationMessage.getPoi());
            event.setUserId(message.getSenderUserId());
            EventManager.post(event);
        }
    }


    /**
     * 控制语音通话时window窗口中view的显示状态
     *
     * @param recording 录音
     * @param answer    接听
     * @param hangup    挂断
     * @param hf        免提
     * @param small     最小化
     */
    private void goneAudioView(boolean recording, boolean answer, boolean hangup, boolean hf,
                               boolean small) {
        // 录音 接听 挂断 免提 最小化
        audio_ll_recording.setVisibility(recording ? View.VISIBLE : View.GONE);
        audio_ll_answer.setVisibility(answer ? View.VISIBLE : View.GONE);
        audio_ll_hangup.setVisibility(hangup ? View.VISIBLE : View.GONE);
        audio_ll_hf.setVisibility(hf ? View.VISIBLE : View.GONE);
        audio_iv_small.setVisibility(small ? View.VISIBLE : View.GONE);
    }

    /**
     * 控制视频通话时window窗口中view的显示状态
     *
     * @param info   个人信息
     * @param small  最小化按钮
     * @param big    大窗口
     * @param answer 接听
     * @param hangup 挂断
     * @param time   通话时间
     */
    private void goneVideoView(boolean info, boolean small,
                               boolean big, boolean answer, boolean hangup,
                               boolean time) {
        // 个人信息 小窗口  接听  挂断 时间
        video_ll_info.setVisibility(info ? View.VISIBLE : View.GONE);
        video_small_video.setVisibility(small ? View.VISIBLE : View.GONE);
        video_big_video.setVisibility(big ? View.VISIBLE : View.GONE);
        video_ll_answer.setVisibility(answer ? View.VISIBLE : View.GONE);
        video_ll_hangup.setVisibility(hangup ? View.VISIBLE : View.GONE);
        video_tv_time.setVisibility(time ? View.VISIBLE : View.GONE);
    }

    /**
     * 更新窗口上的用户信息
     * @param type 媒体类型 audio video
     * @param index    0：呼叫方 1：被叫方
     * @param objectId
     */
    private void updateWindowInfo(int index, RongCallCommon.CallMediaType type, String objectId) {
        //音频
        if (type.equals(RongCallCommon.CallMediaType.AUDIO)) {
            if (index == 0) {
                goneAudioView(false, true, true, false, false);
            } else if (index == 1) {
                goneAudioView(false, false, true, false, false);
            }
            //视频
        } else if (type.equals(RongCallCommon.CallMediaType.VIDEO)) {
            if (index == 0) {
                goneVideoView(true, false, false, true, true, false);
            } else if (index == 1) {
                goneVideoView(true, false, true, false, true, false);
            }
        }
        BmobManager.getInstance().queryByObjectId(objectId, new FindListener<MeetUser>() {
            @Override
            public void done(List<MeetUser> list, BmobException e) {
                if (e == null) {
                    if (list.size() > 0) {
                        MeetUser meetUser = list.get(0);
                        if (type.equals(RongCallCommon.CallMediaType.AUDIO)) {
                            Glide.with(CloudService.this)
                                    .load(meetUser.getPhoto())
                                    .into(audio_iv_photo);
                            if (index == 0) {
                                audio_tv_status.setText("正在语音呼叫" + " " + meetUser.getNickName());
                            } else if (index == 1) {
                                audio_tv_status.setText(meetUser.getNickName() + "的语音来电");
                            }
                        } else if (type.equals(RongCallCommon.CallMediaType.VIDEO)) {
                            Glide.with(CloudService.this)
                                    .load(meetUser.getPhoto())
                                    .into(video_iv_photo);
                            if (index == 0) {
                                video_tv_name.setText("正在视频呼叫" + " " + meetUser.getNickName());
                            } else if (index == 1) {
                                video_tv_name.setText(meetUser.getNickName() + "的视频来电");
                            }
                        }

                    }
                }
            }
        });
    }


    private boolean isRecording = false;//是否正在录音
    private boolean isHF = false;//是否开启免提

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audio_ll_recording:
                //录音
//                if (isRecording) {
//                    isRecording = false;
//                    CloudManager.getInstance().stopAudioRecording();
//                    audio_iv_recording.setImageResource(R.drawable.img_recording);
//                } else {
//                    isRecording = true;
//                    //录音
//                    CloudManager.getInstance().startAudioRecording(
//                            "/sdcard/Meet/" + System.currentTimeMillis() + "wav");
//                    audio_iv_recording.setImageResource(R.drawable.img_recording_p);
//                }
                break;
            case R.id.audio_ll_answer:
            case R.id.video_ll_answer:
                //接听
                CloudManager.getInstance().acceptCall(callId);
                break;
            case R.id.audio_ll_hangup:
            case R.id.video_ll_hangup:
                //挂断
                CloudManager.getInstance().hangUpCall(callId);
                break;
            case R.id.audio_ll_hf:
                //免提
                isHF = !isHF;
                CloudManager.getInstance().setEnableSpeakerphone(isHF);
                audio_iv_hf.setImageResource(isHF ? R.drawable.img_hf_p : R.drawable.img_hf);
                break;
            case R.id.audio_iv_small:
//                最小化
                break;
            case R.id.video_small_video:
                isSmallShowLocal = !isSmallShowLocal;
                //小窗切换
                updateVideoView();
                break;
        }
    }

    /**
     * 更新双方视频窗口
     */
    private void updateVideoView() {
        video_small_video.removeAllViews();
        video_big_video.removeAllViews();
        //判断本地窗口是否是小窗口
        if(isSmallShowLocal){
            if (mLocalView != null) {
                video_small_video.addView(mLocalView);
                mLocalView.setZOrderOnTop(true);//设置surface在其他surface上层显示
            }
            if(mRemoteView!=null){
                video_small_video.addView(mRemoteView);
                mRemoteView.setZOrderOnTop(false);
            }
        }else{
            if (mLocalView != null) {
                video_big_video.addView(mLocalView);
                mLocalView.setZOrderOnTop(false);
            }
            if (mRemoteView != null) {
                video_small_video.addView(mRemoteView);
                mRemoteView.setZOrderOnTop(false);
            }
        }
    }
}