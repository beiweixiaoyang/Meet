package com.example.meet.fragment.chat;

import android.content.Intent;
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
import com.example.meet.base.BaseFragment;
import com.example.meet.bmob.BmobManager;
import com.example.meet.bmob.Friend;
import com.example.meet.bmob.MeetUser;
import com.example.meet.model.AllFriendModel;
import com.example.meet.ui.UserInfoActivity;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

//好友列表tab
public class AllFriendFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mAllFriendRefreshLayout;
    private RecyclerView mRecyclerView;
    private View empty_view;

    private CommonAdapter<AllFriendModel>mCommonAdapter;
    private List<AllFriendModel>mLists=new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=LayoutInflater.from(getContext()).inflate(R.layout.fragment_all_friend,null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mAllFriendRefreshLayout=view.findViewById(R.id.mAllFriendRefreshLayout);
        mRecyclerView=view.findViewById(R.id.mAllFriendView);
        empty_view=view.findViewById(R.id.item_empty_view);
        mAllFriendRefreshLayout.setOnRefreshListener(this);
        mCommonAdapter=new CommonAdapter<AllFriendModel>(mLists, new CommonAdapter.OnBindDataListener<AllFriendModel>() {
            @Override
            public void onBindViewHolder(AllFriendModel model, CommonViewHolder holder, int type, int position) {
                holder.setImageUrl(getActivity(),R.id.iv_photo,model.getUrl());
                holder.setText(R.id.tv_nickname,model.getNickName());
                holder.setText(R.id.tv_desc,model.getDesc());
                holder.setImageResource(R.id.iv_sex,model.isSex()?R.drawable.img_boy_icon:R.drawable.img_girl_icon);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), UserInfoActivity.class)
                        .putExtra("objectId",model.getUserId()));
                    }
                });
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_all_friend_item;
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mCommonAdapter);
        queryMyFriends();
    }

    @Override
    public void onRefresh() {
        if(mAllFriendRefreshLayout.isRefreshing()){
            queryMyFriends();
        }
    }

    /**
     * 查询我的好友
     */
    private void queryMyFriends() {
        mAllFriendRefreshLayout.setRefreshing(true);
        BmobManager.getInstance().queryMyFriend(new FindListener<Friend>() {
            @Override
            public void done(List<Friend> list, BmobException e) {
                mAllFriendRefreshLayout.setRefreshing(false);
                String myObjectId=BmobManager.getInstance().getCurrentUser().getObjectId();
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getFriendUser().getObjectId().equals(myObjectId)){
                        list.remove(i);
                    }
                }
                if(e == null){
                    if(list.size() > 0){
                        empty_view.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        if(mLists.size() > 0){
                            mLists.clear();
                        }
                        for (int i = 0; i < list.size(); i++) {
                            String objectId = list.get(i).getFriendUser().getObjectId();
                            BmobManager.getInstance().queryByObjectId(objectId, new FindListener<MeetUser>() {
                                @Override
                                public void done(List<MeetUser> list, BmobException e) {
                                    if(e == null){
                                        MeetUser meetUser = list.get(0);
                                        AllFriendModel allFriendModel=new AllFriendModel();
                                        allFriendModel.setNickName(meetUser.getNickName());
                                        allFriendModel.setDesc(meetUser.getDesc());
                                        allFriendModel.setSex(meetUser.isSex());
                                        allFriendModel.setUrl(meetUser.getPhoto());
                                        allFriendModel.setUserId(meetUser.getObjectId());
                                        mLists.add(allFriendModel);
                                        mCommonAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }else{
                        empty_view.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
}
