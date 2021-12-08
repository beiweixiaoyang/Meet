package com.example.meet.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;

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
import com.example.meet.bmob.PrivateSet;
import com.example.meet.litepal.LitePalManager;
import com.example.meet.litepal.NewFriend;
import com.example.meet.model.AddFriendModel;
import com.example.meet.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 通讯录导入好友
 */
public class ContactFriendActivity extends BaseBackActivity {


    private static final int TYPE_CONTENT = 1;
    private RecyclerView mContactRecyclerView;
    private Map<String, String> contactMap = new HashMap<>();
    private CommonAdapter<AddFriendModel> mCommonAdapter;
    private List<AddFriendModel> mLists = new ArrayList<>();
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_friend);
        initView();
    }

    private void initView() {
        mContactRecyclerView = findViewById(R.id.contactRecyclerView);
        mContactRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mContactRecyclerView.addItemDecoration
                (new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mCommonAdapter = new CommonAdapter<AddFriendModel>(mLists, new CommonAdapter.OnMoreBindDataListener<AddFriendModel>() {
            @Override
            public int getItemType(int position) {
                return mLists.get(position).getType();
            }

            @Override
            public void onBindViewHolder(AddFriendModel model, CommonViewHolder holder, int type, int position) {
                holder.setVisibility(R.id.ll_contact_info, View.VISIBLE);
                holder.setImageUrl(ContactFriendActivity.this, R.id.iv_photo, model.getPhoto());
                holder.setText(R.id.tv_desc, model.getDesc());
                holder.setText(R.id.tv_nickName, model.getNickname());
                holder.setText(R.id.tv_age, model.getAge() + "岁");
                holder.setImageResource(R.id.iv_Sex, model.isSex() ? R.drawable.img_boy_icon : R.drawable.img_girl_icon);
                holder.setText(R.id.tv_contact_phone, model.getContactPhone());
                holder.setText(R.id.tv_contact_name, model.getContactName());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(ContactFriendActivity.this,UserInfoActivity.class);
                        intent.putExtra("objectId",model.getObjectId());
                        startActivity(intent);
                    }
                });
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_search_user_item;
            }
        });
        mContactRecyclerView.setAdapter(mCommonAdapter);
        loadUser();
    }

    /**
     * 读取用户信息
     * 过滤掉PrivateSet中的用户
     */
    private void loadUser() {
        disposable= Observable.create(new ObservableOnSubscribe<List<PrivateSet>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<PrivateSet>> emitter) throws Exception {
                loadContact();//读取联系人信息
                BmobManager.getInstance().queryPrivateSet(new FindListener<PrivateSet>() {
                    @Override
                    public void done(List<PrivateSet> list, BmobException e) {
                        emitter.onNext(list);
                        emitter.onComplete();
                    }
                });

            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<PrivateSet>>() {
                    @Override
                    public void accept(List<PrivateSet> privateSets) throws Exception {
                        fixPrivateSet(privateSets);
                    }
                });
    }

    /**
     * 从查询到的联系人中过滤掉privateSet中的用户
     * @param privateSets
     */
    private void fixPrivateSet(List<PrivateSet> privateSets) {
        List<String> userPhone=new ArrayList<>();
        for (int i = 0; i < privateSets.size(); i++) {
            userPhone.add(privateSets.get(i).getPhone());
        }
        if (contactMap.size() > 0) {
            for (Map.Entry<String, String> entry : contactMap.entrySet()) {
                //手机号码存在，跳过本次循环，过滤掉
                if(userPhone.contains(entry.getValue())){
                    continue;
                }
                BmobManager.getInstance().queryByPhone(entry.getKey(), new FindListener<MeetUser>() {
                    @Override
                    public void done(List<MeetUser> list, BmobException e) {
                        if (e == null) {
                            LogUtils.i("通讯录查询好友成功");
                            if (list.size() > 0) {
                                MeetUser meetUser = list.get(0);
                                addContent(meetUser,entry.getKey(),entry.getValue());
                            }
                        } else {
                            LogUtils.i("通讯录查询好友失败：" + e.toString());
                        }
                    }
                });
            }
        }
    }

    /**
     * 添加内容
     * @param key 姓名
     * @param value  电话号码
     */
    private void addContent(MeetUser meetUser, String key, String value) {
        AddFriendModel model=new AddFriendModel();
        model.setType(TYPE_CONTENT);
        model.setAge(meetUser.getAge());
        model.setDesc(meetUser.getDesc());
        model.setPhoto(meetUser.getPhoto());
        model.setNickname(meetUser.getNickName());
        model.setContact(true);
        model.setContactName(key);
        model.setContactPhone(value);
        mLists.add(model);
        mCommonAdapter.notifyDataSetChanged();
    }

    /**
     * 读取联系人信息
     */
    private void loadContact() {
        LogUtils.i("loadContact");
        Cursor cursor = getContentResolver().query
                (ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null);
        String name;
        String phone;
        while (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            //String是不可变的字符串
            String replace = phone.replace(" ", "");
            contactMap.put(name, replace);
        }
    }
}