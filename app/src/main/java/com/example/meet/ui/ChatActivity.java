package com.example.meet.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meet.R;
import com.example.meet.adapter.CommonAdapter;
import com.example.meet.adapter.CommonViewHolder;
import com.example.meet.base.BaseBackActivity;
import com.example.meet.bmob.BmobManager;
import com.example.meet.cloud.CloudManager;
import com.example.meet.eneity.Constants;
import com.example.meet.event.EventManager;
import com.example.meet.event.MessageEvent;
import com.example.meet.gson.TextBean;
import com.example.meet.model.ChatModel;
import com.example.meet.utils.FileUtil;
import com.example.meet.utils.LogUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;

/**
 * 聊天会话界面
 */
public class ChatActivity extends BaseBackActivity implements View.OnClickListener {

    //左边
    public static final int TYPE_LEFT_TEXT = 0;
    public static final int TYPE_LEFT_IMAGE = 1;
    public static final int TYPE_LEFT_LOCATION = 2;
    //右边
    public static final int TYPE_RIGHT_TEXT = 3;
    public static final int TYPE_RIGHT_IMAGE = 4;
    public static final int TYPE_RIGHT_LOCATION = 5;

    //权限
    private static final String [] PERMISSIONS=
            new String[]{Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };


    /**
     * 外部通过startActivity跳转到聊天界面
     *
     * @param context
     * @param objectId    对方objectId
     * @param friendName  对方name
     * @param friendPhoto 对方 头像
     */
    public static void startActivity(Context context, String objectId, String friendName, String friendPhoto) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Constants.INTENT_USER_ID, objectId);
        intent.putExtra(Constants.INTENT_USER_NICKNAME, friendName);
        intent.putExtra(Constants.INTENT_USER_PHOTO, friendPhoto);
        context.startActivity(intent);
    }

    private EditText et_input_msg;
    private Button btn_send_msg;
    private LinearLayout ll_voice;
    private LinearLayout ll_camera;
    private LinearLayout ll_pic;
    private LinearLayout ll_location;


    private String friendId;
    private String friendName;
    private String friendPhoto;

    private String myPhoto;

    private RecyclerView mRecyclerView;
    private CommonAdapter<ChatModel> mCommonAdapter;
    private List<ChatModel> mLists = new ArrayList<>();

    private File file;//发送的图片

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initView();
    }


    private void initView() {
        mRecyclerView = findViewById(R.id.mChatView);
        et_input_msg = findViewById(R.id.et_input_msg);
        btn_send_msg = findViewById(R.id.btn_send_msg);
        ll_voice = findViewById(R.id.ll_voice);
        ll_camera = findViewById(R.id.ll_camera);
        ll_pic = findViewById(R.id.ll_pic);
        ll_location = findViewById(R.id.ll_location);
        et_input_msg.setOnClickListener(this);
        btn_send_msg.setOnClickListener(this);
        ll_voice.setOnClickListener(this);
        ll_camera.setOnClickListener(this);
        ll_pic.setOnClickListener(this);
        ll_location.setOnClickListener(this);
        loadInfo();
        initRecyclerView();
        queryMessage();
    }

    /**
     * 查询历史会话记录
     */
    private void queryMessage() {
        CloudManager.getInstance().getHistoryMessages(friendId, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                try {
                    parseListMessages(messages);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtils.e("查询历史记录失败："+errorCode.toString());
            }
        });
    }

    /**
     * 解析messages
     * @param messages
     */
    private void parseListMessages(List<Message> messages) throws Exception{
        //对messages进行反转
        Collections.reverse(messages);
        if(messages.size() > 0){
            for (int i = 0; i < messages.size(); i++) {
                Message message=messages.get(i);
                String objectName = message.getObjectName();
                if(objectName.equals(CloudManager.MESSAGE_TEXT_NAME)){
                    TextMessage textMessage= (TextMessage) message.getContent();
                    String msg=textMessage.getContent();
                    TextBean textBean=new Gson().fromJson(msg,TextBean.class);
                    if(textBean.getType().equals(CloudManager.TYPE_TEXT)){
                        //添加到UI中(判断是自身还是对方)
                        if(message.getSenderUserId().equals(friendId)){
                            addText(0,textBean.getMsg());
                        }else{
                            addText(1,textBean.getMsg());
                        }
                    }
                }else if(objectName.equals(CloudManager.MESSAGE_IMAGE_NAME)){
                    ImageMessage imageMessage= (ImageMessage) message.getContent();
                    String url=imageMessage.getRemoteUri().toString();
                    if(!TextUtils.isEmpty(url)){
                        if(message.getSenderUserId().equals(friendId)){
                            addImage(0,url);
                        }else{
                            addImage(1,url);
                        }
                    }
                }else if(objectName.equals(CloudManager.MESSAGE_LOCATION_NAME)){}
            }
        }else{
            queryRemoteMessage();
        }
    }

    /**
     * 查询服务器历史记录
     */
    private void queryRemoteMessage() {
        CloudManager.getInstance().getRemoteHistoryMessages(friendId, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                try {
                    parseListMessages(messages);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtils.e("查询服务器记录失败："+errorCode.toString());
            }
        });
    }

    private void initRecyclerView() {
        mCommonAdapter = new CommonAdapter<ChatModel>(mLists, new CommonAdapter.OnMoreBindDataListener<ChatModel>() {
            @Override
            public int getItemType(int position) {
                return mLists.get(position).getType();
            }

            @Override
            public void onBindViewHolder(ChatModel model, CommonViewHolder holder, int type, int position) {
                switch (type){
                    case TYPE_LEFT_TEXT:
                        holder.setText(R.id.tv_left_text,model.getText());
                        holder.setImageUrl(ChatActivity.this,R.id.iv_left_photo,friendPhoto);
                        break;
                    case TYPE_LEFT_IMAGE:
                        holder.setImageUrl(ChatActivity.this,R.id.iv_left_photo,friendPhoto);
                        holder.setImageUrl(ChatActivity.this,R.id.iv_left_img,model.getImgUrl());
                        holder.getView(R.id.iv_left_img).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImagePreviewActivity.startActivity(ChatActivity.this,true,model.getImgUrl());
                            }
                        });
                        break;
                    case TYPE_LEFT_LOCATION:
                        break;
                    case TYPE_RIGHT_TEXT:
                        holder.setText(R.id.tv_right_text,model.getText());
                        holder.setImageUrl(ChatActivity.this,R.id.iv_right_photo,myPhoto);
                        break;
                    case TYPE_RIGHT_IMAGE:
                        if(model.getImgUrl()!=null){
                            holder.getView(R.id.iv_right_img).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ImagePreviewActivity.startActivity(ChatActivity.this,true,model.getImgUrl());
                                }
                            });
                            holder.setImageUrl(ChatActivity.this,R.id.iv_right_img,model.getImgUrl());
                        }else{
                            holder.setImageFile(ChatActivity.this,R.id.iv_right_img,model.getLocalFile());
                            holder.getView(R.id.iv_right_img).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ImagePreviewActivity.startActivity(ChatActivity.this,false,model.getLocalFile().getPath());
                                }
                            });
                        }
                        holder.setImageUrl(ChatActivity.this,R.id.iv_right_photo,myPhoto);
                        break;
                    case TYPE_RIGHT_LOCATION:
                        break;
                }
            }

            @Override
            public int getLayoutId(int type) {
                if (type == TYPE_LEFT_TEXT) {
                    return R.layout.layout_chat_left_text;
                } else if (type == TYPE_LEFT_IMAGE) {
                    return R.layout.layout_chat_left_img;
                } else if (type == TYPE_LEFT_LOCATION) {
                    return R.layout.layout_chat_left_location;
                } else if (type == TYPE_RIGHT_TEXT) {
                    return R.layout.layout_chat_right_text;
                } else if (type == TYPE_RIGHT_IMAGE) {
                    return R.layout.layout_chat_right_img;
                } else if (type == TYPE_RIGHT_LOCATION) {
                    return R.layout.layout_chat_right_location;
                }
                return 0;
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mCommonAdapter);
    }

    /**
     * 加载双方信息
     */
    private void loadInfo() {
        myPhoto= BmobManager.getInstance().getCurrentUser().getPhoto();
        friendId = getIntent().getStringExtra(Constants.INTENT_USER_ID);
        friendName = getIntent().getStringExtra(Constants.INTENT_USER_NICKNAME);
        friendPhoto = getIntent().getStringExtra(Constants.INTENT_USER_PHOTO);
        getSupportActionBar().setTitle(friendName);
    }


    private void baseAddItem(ChatModel model){
        mLists.add(model);
        mCommonAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(mLists.size() -1);
    }

    /**
     * 添加文本数据
     * @param index 0：左边；1：右边
     * @param text 文本内容
     */
    private void addText(int index,String text){
        ChatModel model=new ChatModel();
        if(index == 0){
            model.setType(TYPE_LEFT_TEXT);
        }else{
            model.setType(TYPE_RIGHT_TEXT);
        }
        model.setText(text);
        baseAddItem(model);
    }

    /**
     * 添加图片
     * @param index
     * @param url 图片url
     */
    private void addImage(int index,String url){
        ChatModel model=new ChatModel();
        if(index == 0){
            model.setType(TYPE_LEFT_IMAGE);
        }else{
            model.setType(TYPE_RIGHT_IMAGE);
        }
        model.setImgUrl(url);
        baseAddItem(model);
    }

    /**
     * 加载图片
     * @param index
     * @param file 本地文件
     */
    private void addImage(int index,File file){
        ChatModel model=new ChatModel();
        if(index == 0){
            model.setType(TYPE_LEFT_IMAGE);
        }else{
            model.setType(TYPE_RIGHT_IMAGE);
        }
        model.setLocalFile(file);
        baseAddItem(model);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send_msg:
                String input_msg=et_input_msg.getText().toString().trim();
                if(TextUtils.isEmpty(input_msg)){
                    Toast.makeText(ChatActivity.this,"输入文本不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                CloudManager.getInstance().sendTextMessage(input_msg,CloudManager.TYPE_TEXT,friendId);
                addText(1,input_msg);
                //清空edittext中内容
                et_input_msg.setText("");
                break;
            case R.id.ll_voice:
                break;
            case R.id.ll_camera:
                FileUtil.getInstance().toCamera(ChatActivity.this);
                break;
            case R.id.ll_pic:
                FileUtil.getInstance().toAlbum(ChatActivity.this);
                break;
            case R.id.ll_location:
                //申请权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestRuntimePermissions(PERMISSIONS, new OnPermissionListener() {
                        @Override
                        public void granted() {

                        }

                        @Override
                        public void denied(List<String> deniedList) {
                            for(String denied:deniedList){
                                if(denied.equals("android.permission.READ_PHONE_STATE")){
                                    Toast.makeText(ChatActivity.this,
                                            "打开失败，请检查读取手机状态权限是否打开",
                                            Toast.LENGTH_SHORT).show();
                                }
                                if(denied.equals("android.permission.ACCESS_COARSE_LOCATION")){
                                    Toast.makeText(ChatActivity.this,
                                            "打开失败，请检查地理位置权限是否打开",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent messageEvent) {
        if(!messageEvent.getUserId().equals(friendId)){
            return;
        }
        switch (messageEvent.getType()){
            case EventManager.FLAG_SEND_TEXT:
                addText(0,messageEvent.getText());
                break;
            case EventManager.FLAG_SEND_IMAGE:
                addImage(0,messageEvent.getImgUrl());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == FileUtil.CAMERA_REQUEST_CODE){
                file = FileUtil.getInstance().getTempFile();
            }else if(requestCode == FileUtil.ALBUM_REQUEST_CODE){
                Uri uri = data.getData();
                if (uri != null) {
                    LogUtils.i("path:" + uri.getPath());
                    String realPathFromUri =
                            FileUtil.getInstance().getRealPathFromUri(this, uri);
                    if (!TextUtils.isEmpty(realPathFromUri)) {
                        LogUtils.i("realpath:" + realPathFromUri);
                        file = new File(realPathFromUri);
                    }
                }
            }
            if (file != null){
                CloudManager.getInstance().sendImageMessage(file, friendId);
                addImage(1, file);
                file = null;
            }
        }
    }
}