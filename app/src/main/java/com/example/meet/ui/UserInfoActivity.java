package com.example.meet.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meet.R;
import com.example.meet.adapter.CommonAdapter;
import com.example.meet.adapter.CommonViewHolder;
import com.example.meet.base.BaseUIActivity;
import com.example.meet.bmob.BmobManager;
import com.example.meet.bmob.MeetUser;
import com.example.meet.manager.CloudManager;
import com.example.meet.model.UserInfoModel;
import com.example.meet.manager.DialogManager;
import com.example.meet.utils.LogUtils;
import com.example.meet.view.DialogView;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import io.rong.calllib.RongCallCommon;

public class UserInfoActivity extends BaseUIActivity implements View.OnClickListener {

    private String objectId;
    private MeetUser meetUser;

    private RelativeLayout ll_back;
    private LinearLayout ll_isFriend;
    private TextView tv_nickname;
    private TextView tv_desc;
    private ImageView iv_user_photo;
    private Button btn_add_friend;
    private Button btn_chat;
    private Button btn_audio_chat;
    private Button btn_video_chat;

    private RecyclerView mUserInfoView;
    private CommonAdapter<UserInfoModel> mCommonAdapter;
    private List<UserInfoModel> mUserLists = new ArrayList<>();
    private int[] mColor = {0x881E90FF, 0x8800FF7F, 0x88FFD700, 0x88FF6347, 0x88F08080, 0x8840E0D0};

    private DialogView mAddFriendDialog;
    private EditText et_msg;
    private TextView tv_cancel, tv_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        objectId = getIntent().getStringExtra("objectId");
        initView();
        initDialog();
    }

    private void initView() {
        ll_back = findViewById(R.id.ll_back);
        ll_isFriend = findViewById(R.id.ll_is_friend);
        tv_nickname = findViewById(R.id.tv_nickname);
        tv_desc = findViewById(R.id.tv_desc);
        iv_user_photo = findViewById(R.id.iv_user_photo);
        btn_add_friend = findViewById(R.id.btn_add_friend);
        btn_chat = findViewById(R.id.btn_chat);
        btn_audio_chat = findViewById(R.id.btn_audio_chat);
        btn_video_chat = findViewById(R.id.btn_video_chat);
        mUserInfoView = findViewById(R.id.mUserInfoView);
        btn_chat.setOnClickListener(this);
        ll_back.setOnClickListener(this);
        btn_add_friend.setOnClickListener(this);
        btn_video_chat.setOnClickListener(this);
        btn_audio_chat.setOnClickListener(this);
        initRecyclerView();
        queryUserInfo();
    }

    /**
     * 通过objectId查询用户相关信息，显示到界面中
     */
    private void queryUserInfo() {
        if (TextUtils.isEmpty(objectId)) {
            return;
        }
        //查询
        BmobManager.getInstance().queryByObjectId(objectId, new FindListener<MeetUser>() {
            @Override
            public void done(List<MeetUser> list, BmobException e) {
                if (e == null) {
                    LogUtils.i("查询用户信息成功");
                    if (list.size() != 0) {
                        meetUser = list.get(0);
                        updateUserInfo(meetUser);
                    }
                } else {
                    LogUtils.i("查询用户信息失败：" + e.toString());
                }
            }
        });
        //3.判断是否是好友关系
        BmobManager.getInstance().queryMyFriend(new FindListener<BmobManager.Friend>() {
            @Override
            public void done(List<BmobManager.Friend> list, BmobException e) {
                if (e == null) {
                    if (list.size() > 0) {
                        //存在好友列表
                        for (int i = 0; i < list.size(); i++) {
                            BmobManager.Friend friend = list.get(i);
                            if (friend.getFriendUser().getObjectId().equals(objectId)) {
                                //是好友关系
                                btn_add_friend.setVisibility(View.GONE);
                                ll_isFriend.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 更新用户信息显示到界面中
     *
     * @param meetUser
     */
    private void updateUserInfo(MeetUser meetUser) {
        Glide.with(this)
                .load(meetUser.getPhoto())
                .into(iv_user_photo);
        tv_desc.setText(meetUser.getDesc());
        tv_nickname.setText(meetUser.getNickName());
        addUserModel(mColor[0], "性别", meetUser.isSex() ? "男" : "女");
        addUserModel(mColor[1], "年龄", meetUser.getAge() + "岁");
        addUserModel(mColor[2], "生日", meetUser.getBirthday());
        addUserModel(mColor[3], "星座", meetUser.getConstellation());
        addUserModel(mColor[4], "爱好", meetUser.getHobby());
        addUserModel(mColor[5], "单身状态", meetUser.getStatus());
        mCommonAdapter.notifyDataSetChanged();
    }

    /**
     * 添加数据到list中
     *
     * @param color   背景颜色
     * @param title   标题
     * @param content 内容
     */
    private void addUserModel(int color, String title, String content) {
        UserInfoModel model = new UserInfoModel();
        model.setContent(content);
        model.setType(title);
        model.setBackgroundColor(color);
        mUserLists.add(model);
    }

    /**
     * 初始化recyclerview
     */
    private void initRecyclerView() {
        mCommonAdapter = new CommonAdapter<UserInfoModel>(mUserLists, new CommonAdapter.OnBindDataListener<UserInfoModel>() {
            @Override
            public void onBindViewHolder(UserInfoModel model, CommonViewHolder holder, int type, int position) {
                holder.setText(R.id.tv_type, model.getType());
                holder.setText(R.id.tv_content, model.getContent());
                holder.setBackgroundColor(R.id.ll_bg, model.getBackgroundColor());
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_user_info_item;
            }
        });
        mUserInfoView.setAdapter(mCommonAdapter);
        mUserInfoView.setLayoutManager(new GridLayoutManager(this, 3));
    }

    /**
     * 初始化添加好友提示框
     */
    private void initDialog() {
        mAddFriendDialog = DialogManager.getInstance()
                .initDialogView(this, R.layout.dialog_send_friend);
        et_msg = mAddFriendDialog.findViewById(R.id.et_msg);
        tv_cancel = mAddFriendDialog.findViewById(R.id.tv_cancel);
        tv_add = mAddFriendDialog.findViewById(R.id.tv_add_friend);
        tv_cancel.setOnClickListener(this);
        tv_add.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add_friend:
                DialogManager.getInstance().showDialog(mAddFriendDialog);
                break;
            case R.id.btn_chat:
                ChatActivity.startActivity(UserInfoActivity.this, meetUser.getObjectId(),
                        meetUser.getNickName(), meetUser.getPhoto());
                break;
            case R.id.btn_video_chat:
                CloudManager.getInstance().startCall(UserInfoActivity.this,objectId, RongCallCommon.CallMediaType.VIDEO);
                break;
            case R.id.btn_audio_chat:
                CloudManager.getInstance().startCall(UserInfoActivity.this,objectId,RongCallCommon.CallMediaType.AUDIO);
                break;
            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.tv_add_friend:
                String msg = et_msg.getText().toString().trim();
                if (TextUtils.isEmpty(msg)) {
                    msg = "加个好友把！";
                }
                CloudManager.getInstance().sendTextMessage(msg, CloudManager.TYPE_ADD_FRIEND, objectId);
                DialogManager.getInstance().hideDialog(mAddFriendDialog);
                Toast.makeText(UserInfoActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_cancel:
                DialogManager.getInstance().hideDialog(mAddFriendDialog);
                break;
        }
    }
}