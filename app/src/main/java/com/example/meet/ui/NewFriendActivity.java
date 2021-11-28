package com.example.meet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meet.R;
import com.example.meet.adapter.CommonAdapter;
import com.example.meet.adapter.CommonViewHolder;
import com.example.meet.base.BaseBackActivity;
import com.example.meet.bmob.BmobManager;
import com.example.meet.bmob.MeetUser;
import com.example.meet.cloud.CloudManager;
import com.example.meet.litepal.LitePalManager;
import com.example.meet.litepal.NewFriend;
import com.example.meet.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 好友申请列表页面
 * 1.查询好友申请信息，（通过RecyclerView显示）
 * 2.同意则添加为自己的好友，同时更新本地数据库
 * 3.给对方发送自定义消息
 */
public class NewFriendActivity extends BaseBackActivity implements View.OnClickListener{

    private View empty_view;
    private Disposable disposable;

    private List<NewFriend> mLists=new ArrayList<>();
    private List<MeetUser> mUserList = new ArrayList<>();
    private RecyclerView mNewFriendView;
    private CommonAdapter<NewFriend> mCommonAdapter;
    private MeetUser meetUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);
        initView();
    }

    private void initView() {
        empty_view=findViewById(R.id.item_empty_view);
        mNewFriendView=findViewById(R.id.mNewFriendView);
        mNewFriendView.setLayoutManager(new LinearLayoutManager(this));
        mNewFriendView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mCommonAdapter=new CommonAdapter<NewFriend>(mLists, new CommonAdapter.OnBindDataListener<NewFriend>() {
            @Override
            public void onBindViewHolder(NewFriend model, CommonViewHolder holder, int type, int position) {
                //根据UserId查询用户信息
                BmobManager.getInstance().queryByObjectId(model.getUserId(), new FindListener<MeetUser>() {
                    @Override
                    public void done(List<MeetUser> list, BmobException e) {
                        if(e == null){
                            if(list.size() > 0){
                                meetUser = list.get(0);
                                mUserList.add(meetUser);
                                holder.setImageUrl(NewFriendActivity.this,R.id.iv_photo,meetUser.getPhoto());
                                holder.setText(R.id.tv_desc,meetUser.getDesc());
                                holder.setImageResource
                                        (R.id.iv_Sex,meetUser.isSex()?R.drawable.img_boy_icon:R.drawable.img_girl_icon);
                                holder.setText(R.id.tv_nickname,meetUser.getNickName());
                                holder.setText(R.id.tv_age,meetUser.getAge()+"岁");
                                holder.setText(R.id.tv_msg,model.getMsg());
                                if(model.getStatus() == 0){
                                    holder.getView(R.id.ll_agree).setVisibility(View.GONE);
                                    holder.getView(R.id.tv_result).setVisibility(View.VISIBLE);
                                    holder.setText(R.id.tv_result,"已同意");
                                }else if(model.getStatus() == 1){
                                    holder.getView(R.id.ll_agree).setVisibility(View.GONE);
                                    holder.getView(R.id.tv_result).setVisibility(View.VISIBLE);
                                    holder.setText(R.id.tv_result,"已拒绝");
                                }
                            }
                        }
                    }
                });
                //设置点击事件
                holder.getView(R.id.ll_yes).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /**
                         * 1.同意，刷新当前Item
                         * 2.将好友添加到自己的好友列表中
                         * 3.通知对方已经同意
                         * 4.对方将我添加到对方的好友列表
                         * 5.刷新好友列表
                         */
                        updateItem(position,0);
                        BmobManager.getInstance().addFriend(meetUser, new SaveListener<String>() {
                            @Override
                            public void done(String s, BmobException e) {
                                if(e == null){
                                    //保存成功，通知对方
                                    CloudManager.getInstance().sendTextMessage("",CloudManager.TYPE_ARGEED_FRIEND
                                    ,meetUser.getObjectId());
                                    //刷新好友列表
                                }
                            }
                        });
                    }
                });
                holder.getView(R.id.ll_no).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateItem(position,1);
                    }
                });
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(NewFriendActivity.this,UserInfoActivity.class);
                        intent.putExtra("objectId",model.getUserId());
                        startActivity(intent);
                    }
                });
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_new_friend_item;
            }
        });
        mNewFriendView.setAdapter(mCommonAdapter);
        queryFriendApply();
    }

    /**
     * 更新Item信息
     * @param position 当前list位置
     * @param i 申请状态（-1，0，1）
     */
    private void updateItem(int position, int i) {
        NewFriend newFriend = mLists.get(position);
        //更新数据库
        LitePalManager.getInstance().updateFriend(newFriend.getUserId(),i);
        //更新本地数据
        newFriend.setStatus(i);
        mLists.set(position,newFriend);
        mCommonAdapter.notifyDataSetChanged();//刷新列表
    }

    private void queryFriendApply() {
        //在子线程中获取好友申请列表，在主线程中更新UI(RxJava)
        disposable= Observable.create(new ObservableOnSubscribe<List<NewFriend>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<NewFriend>> emitter) throws Exception {
                emitter.onNext(LitePalManager.getInstance().queryNewFriend());
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<NewFriend>>() {
                    @Override
                    public void accept(List<NewFriend> newFriends) throws Exception {
                        if(newFriends.size()> 0){
                            mLists.addAll(newFriends);
                        }else{
                            empty_view.setVisibility(View.VISIBLE);
                            mNewFriendView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(disposable.isDisposed()){
            disposable.dispose();
        }
    }
}