package com.example.meet.fragment.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.meet.R;
import com.example.meet.adapter.CommonAdapter;
import com.example.meet.adapter.CommonViewHolder;
import com.example.meet.bmob.BmobManager;
import com.example.meet.bmob.MeetUser;
import com.example.meet.cloud.CloudManager;
import com.example.meet.gson.TextBean;
import com.example.meet.model.ChatRecordModel;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.message.TextMessage;

/**
 * 聊天记录tab
 */
public class ChatRecordFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mChatRecordRefreshLayout;
    private RecyclerView mRecyclerView;
    private View empty_view;

    private CommonAdapter<ChatRecordModel> mCommonAdapter;
    private List<ChatRecordModel> mLists = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_chat_record, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mChatRecordRefreshLayout = view.findViewById(R.id.mChatRecordRefreshLayout);
        mRecyclerView = view.findViewById(R.id.mChatRecordView);
        empty_view = view.findViewById(R.id.item_empty_view);
        mChatRecordRefreshLayout.setOnRefreshListener(this);
        mCommonAdapter = new CommonAdapter<ChatRecordModel>(mLists, new CommonAdapter.OnBindDataListener<ChatRecordModel>() {
            @Override
            public void onBindViewHolder(ChatRecordModel model, CommonViewHolder holder, int type, int position) {
                holder.setImageUrl(getActivity(), R.id.iv_photo, model.getUrl());
                holder.setText(R.id.tv_nickname, model.getNickName());
                holder.setText(R.id.tv_content, model.getEndMsg());
                holder.setText(R.id.tv_time, model.getTime());
                if (model.getUnReadSize() == 0) {
                    holder.getView(R.id.tv_un_read).setVisibility(View.GONE);
                } else {
                    holder.getView(R.id.tv_un_read).setVisibility(View.VISIBLE);
                    holder.setText(R.id.tv_un_read, model.getUnReadSize() + "");
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_chat_record_item;
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mCommonAdapter);
        queryChatRecord();
    }

    /**
     *
     */
    private void queryChatRecord() {
        mChatRecordRefreshLayout.setRefreshing(true);
        CloudManager.getInstance().getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                mChatRecordRefreshLayout.setRefreshing(false);
                if (conversations.size() > 0) {
                    if(mLists.size() > 0){
                        mLists.clear();
                    }
                    for (int i = 0; i < conversations.size(); i++) {
                        Conversation conversation = conversations.get(i);
                        String targetId = conversation.getTargetId();
                        //根据objectId查询对方相关信息
                        BmobManager.getInstance().queryByObjectId(targetId, new FindListener<MeetUser>() {
                            @Override
                            public void done(List<MeetUser> list, BmobException e) {
                                if (e == null) {
                                    MeetUser meetUser = list.get(0);
                                    ChatRecordModel chatRecordModel = new ChatRecordModel();
                                    chatRecordModel.setUserId(meetUser.getObjectId());
                                    chatRecordModel.setNickName(meetUser.getNickName());
                                    chatRecordModel.setTime(new SimpleDateFormat("HH:mm:ss")
                                            .format(conversation.getReceivedTime()));
                                    chatRecordModel.setUnReadSize(conversation.getUnreadMessageCount());
                                    chatRecordModel.setUrl(meetUser.getPhoto());

                                    String objectName = conversation.getObjectName();
                                    if (objectName.equals(CloudManager.MESSAGE_TEXT_NAME)) {
                                        TextMessage textMessage = (TextMessage) conversation.getLatestMessage();
                                        String msg = textMessage.getContent();
                                        TextBean bean = new Gson().fromJson(msg, TextBean.class);
                                        if (bean.getType().equals(CloudManager.TYPE_TEXT)) {
                                            chatRecordModel.setEndMsg(bean.getMsg());
                                            mLists.add(chatRecordModel);
                                        }
                                    } else if (objectName.equals(CloudManager.MESSAGE_IMAGE_NAME)) {
                                        chatRecordModel.setEndMsg("[图片]");
                                        mLists.add(chatRecordModel);
                                    } else if (objectName.equals(CloudManager.MESSAGE_LOCATION_NAME)) {
                                        chatRecordModel.setEndMsg("[位置]");
                                        mLists.add(chatRecordModel);
                                    }
                                    mCommonAdapter.notifyDataSetChanged();
                                    if (mLists.size() > 0) {
                                        empty_view.setVisibility(View.GONE);
                                        mRecyclerView.setVisibility(View.VISIBLE);
                                    } else {
                                        empty_view.setVisibility(View.VISIBLE);
                                        mRecyclerView.setVisibility(View.GONE);
                                    }
                                }
                            }
                        });
                    }
                } else {
                    mChatRecordRefreshLayout.setRefreshing(false);
                    empty_view.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                }

            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                mChatRecordRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onRefresh() {
        if (mChatRecordRefreshLayout.isRefreshing()) {
            queryChatRecord();
        }
    }
}
