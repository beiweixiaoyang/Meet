package com.example.meet.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meet.R;
import com.example.meet.adapter.CommonAdapter;
import com.example.meet.adapter.CommonViewHolder;
import com.example.meet.base.BaseBackActivity;
import com.example.meet.bmob.BmobManager;
import com.example.meet.bmob.MeetUser;
import com.example.meet.model.AddFriendModel;
import com.example.meet.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * 添加好友界面
 */
public class AddFriendActivity extends BaseBackActivity implements View.OnClickListener{


    private static final int TYPE_TITLE=0;
    private static final int TYPE_CONTENT=1;

    private LinearLayout ll_to_contact;
    private EditText et_phone;
    private ImageView iv_search;
    private View layout_empty_view;

    private RecyclerView mSearchResultView;
    private CommonAdapter<AddFriendModel> mCommonAdapter;
    private List<AddFriendModel> mLists=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        initView();
    }

    private void initView() {
        ll_to_contact=findViewById(R.id.ll_to_contact);
        et_phone=findViewById(R.id.et_phone);
        iv_search=findViewById(R.id.iv_search);
        mSearchResultView=findViewById(R.id.mSearchResultView);
        layout_empty_view=findViewById(R.id.layout_empty_view);
        ll_to_contact.setOnClickListener(this);
        iv_search.setOnClickListener(this);
        initRecyclerView();
    }

    /**
     * 初始化recyclerView
     */
    private void initRecyclerView() {
        mSearchResultView.setLayoutManager(new LinearLayoutManager(this));
        mSearchResultView.addItemDecoration
                (new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mCommonAdapter=new CommonAdapter<AddFriendModel>(mLists, new CommonAdapter.OnMoreBindDataListener<AddFriendModel>() {
            @Override
            public int getItemType(int position) {
                return mLists.get(position).getType();
            }

            @Override
            public void onBindViewHolder(AddFriendModel model, CommonViewHolder holder, int type, int position) {
                if(type == TYPE_TITLE){
                    holder.setText(R.id.tv_title,"查询结果");
                }else if(type == TYPE_CONTENT){
                    holder.setImageUrl(AddFriendActivity.this,R.id.iv_photo,model.getPhoto());
                    holder.setText(R.id.tv_desc, model.getDesc());
                    holder.setText(R.id.tv_nickName, model.getNickname());
                    holder.setText(R.id.tv_age, model.getAge() + "岁");
                    holder.setImageResource
                            (R.id.iv_Sex, model.isSex() ? R.drawable.img_boy_icon : R.drawable.img_girl_icon);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent=new Intent(AddFriendActivity.this,UserInfoActivity.class);
                            intent.putExtra("objectId",model.getObjectId());
                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public int getLayoutId(int type) {
                if(type == TYPE_TITLE){
                    return R.layout.layout_search_title_item;
                }else if(type == TYPE_CONTENT){
                    return R.layout.layout_search_user_item;
                }
                return 0;
            }
        });
        mSearchResultView.setAdapter(mCommonAdapter);
    }

    /**
     * 通过手机号码查询用户
     */
    private void queryPhoneUser() {
        String phone=et_phone.getText().toString().trim();
        if(TextUtils.isEmpty(phone)){
            Toast.makeText(this,"电话号码不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        //通过Bmob查询数据
        BmobManager.getInstance().queryByPhone(phone, new FindListener<MeetUser>() {
            @Override
            public void done(List<MeetUser> list, BmobException e) {
                if(e == null){
                    LogUtils.i("查询成功");
                    if(list.size() == 0){
                        layout_empty_view.setVisibility(View.VISIBLE);
                        mSearchResultView.setVisibility(View.GONE);
                    }else{
                        MeetUser meetUser=list.get(0);
                        layout_empty_view.setVisibility(View.GONE);
                        mSearchResultView.setVisibility(View.VISIBLE);
                        mLists.clear();//每次查询之前清空list
                        addTitle("查询结果");
                        addContent(meetUser);
                        mCommonAdapter.notifyDataSetChanged();
                    }
                    //推荐好友
                    pushUser();
                }else{
                    LogUtils.e("查询失败"+e.toString());
                }
            }
        });
    }

    //添加推荐好友
    private void pushUser() {
        BmobManager.getInstance().queryAllUser(new FindListener<MeetUser>() {
            @Override
            public void done(List<MeetUser> list, BmobException e) {
                if(e == null){
                    LogUtils.i("查询所有好友成功");
                    if(list.size() != 0){
                        addTitle("推荐好友");
                        int number=list.size()>100?100:list.size();
                        for (int i = 0; i < number; i++) {
                            addContent(list.get(i));
                        }
                        mCommonAdapter.notifyDataSetChanged();
                    }
                }else{
                    LogUtils.i("查询所有好友失败");
                }
            }
        });
    }

    /**
     * 添加内容
     * @param meetUser
     */
    private void addContent(MeetUser meetUser) {
        AddFriendModel model=new AddFriendModel();
        model.setType(TYPE_CONTENT);
        model.setPhoto(meetUser.getPhoto());
        model.setDesc(meetUser.getDesc());
        model.setNickname(meetUser.getNickName());
        model.setAge(meetUser.getAge());
        model.setObjectId(meetUser.getObjectId());
        mLists.add(model);
    }

    /**
     * 设置标题
     * @param title
     */
    private void addTitle(String title) {
        AddFriendModel model=new AddFriendModel();
        model.setType(TYPE_TITLE);
        model.setTitle(title);
        mLists.add(model);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_to_contact:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    requestRuntimePermissions(new String[]{Manifest.permission.READ_CONTACTS}, new OnPermissionListener() {
                        @Override
                        public void granted() {
                            startActivity(new Intent(AddFriendActivity.this,ContactFriendActivity.class));
                        }

                        @Override
                        public void denied(List<String> deniedList) {
                            for(String denied:deniedList){
                                if(denied.equals("android.permission.READ_CONTACTS")){
                                    Toast.makeText(AddFriendActivity.this,
                                            "打开失败，请检查读取联系人权限是否打开",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }else{
                    startActivity(new Intent(AddFriendActivity.this,ContactFriendActivity.class));
                }
                break;
            case R.id.iv_search:
                queryPhoneUser();
                break;
        }
    }
}