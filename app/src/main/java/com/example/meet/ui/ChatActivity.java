package com.example.meet.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meet.R;
import com.example.meet.adapter.CommonAdapter;
import com.example.meet.adapter.CommonViewHolder;
import com.example.meet.base.BaseBackActivity;
import com.example.meet.bmob.BmobManager;
import com.example.meet.eneity.Constants;
import com.example.meet.model.ChatModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天会话界面
 */
public class ChatActivity extends BaseBackActivity implements View.OnClickListener {


    public static void startActivity(Context context,String objectId,String friendName,String friendPhoto){
        Intent intent=new Intent(context,ChatActivity.class);
        intent.putExtra(Constants.INTENT_USER_ID,objectId);
        intent.putExtra(Constants.INTENT_USER_NICKNAME,friendName);
        intent.putExtra(Constants.INTENT_USER_PHOTO,friendPhoto);
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
    private List<ChatModel> mLists=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initView();
    }


    private void initView() {
        mRecyclerView=findViewById(R.id.mChatView);
        et_input_msg=findViewById(R.id.et_input_msg);
        btn_send_msg=findViewById(R.id.btn_send_msg);
        ll_voice=findViewById(R.id.ll_voice);
        ll_camera=findViewById(R.id.ll_camera);
        ll_pic=findViewById(R.id.ll_pic);
        ll_location=findViewById(R.id.ll_location);
        et_input_msg.setOnClickListener(this);
        btn_send_msg.setOnClickListener(this);
        ll_voice.setOnClickListener(this);
        ll_camera.setOnClickListener(this);
        ll_pic.setOnClickListener(this);
        ll_location.setOnClickListener(this);
        loadInfo();
        initRecyclerView();
    }

    private void initRecyclerView() {
        mCommonAdapter=new CommonAdapter<ChatModel>(mLists, new CommonAdapter.OnMoreBindDataListener<ChatModel>() {
            @Override
            public int getItemType(int position) {
                return 0;
            }

            @Override
            public void onBindViewHolder(ChatModel model, CommonViewHolder holder, int type, int position) {

            }

            @Override
            public int getLayoutId(int type) {
                return 0;
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * 加载双方信息
     */
    private void loadInfo() {
//        myPhoto= BmobManager.getInstance().getCurrentUser().getPhoto();
        myPhoto="http://b-ssl.duitang.com/uploads/item/201607/27/20160727143727_v5kRZ.jpeg";
        friendId=getIntent().getStringExtra(Constants.INTENT_USER_ID);
        friendName=getIntent().getStringExtra(Constants.INTENT_USER_NICKNAME);
        friendPhoto=getIntent().getStringExtra(Constants.INTENT_USER_PHOTO);
        getSupportActionBar().setTitle(friendName);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_send_msg:
                break;
            case R.id.ll_voice:
                break;
            case R.id.ll_camera:
                break;
            case R.id.ll_pic:
                break;
            case R.id.ll_location:
                break;
        }
    }
}