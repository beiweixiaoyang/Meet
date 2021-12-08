package com.example.meet.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.meet.R;
import com.example.meet.adapter.CloudTagAdapter;
import com.example.meet.base.BaseFragment;
import com.example.meet.bmob.BmobManager;
import com.example.meet.bmob.MeetUser;
import com.example.meet.model.StarUserModel;
import com.example.meet.ui.AddFriendActivity;
import com.example.meet.ui.QrCodeActivity;
import com.example.meet.ui.ShareImageActivity;
import com.example.meet.ui.UserInfoActivity;
import com.example.meet.utils.LogUtils;
import com.example.meet.utils.PairFriendUtil;
import com.example.meet.view.LoadingView;
import com.moxun.tagcloudlib.view.TagCloudView;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class StarFragment extends BaseFragment implements View.OnClickListener {

    private ImageView iv_add, iv_camera;
    private LinearLayout ll_random, ll_soul, ll_fate, ll_love;

    private TagCloudView mCloudView;
    private CloudTagAdapter mCloudAdapter;
    private List<MeetUser> mAllUserList = new ArrayList<>();
    private List<StarUserModel> mStarList = new ArrayList<>();
    private static final int REQUEST_CODE = 1235;

    private LoadingView
            mLoadingView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_star, null);
        initView(view);
        return view;

    }

    /**
     * 初始化view
     *
     * @param view 父容器
     */
    private void initView(View view) {
        mLoadingView = new LoadingView(getContext());
        iv_add = view.findViewById(R.id.iv_add);
        iv_camera = view.findViewById(R.id.iv_camera);
        mCloudView = view.findViewById(R.id.mCloudView);
        ll_random = view.findViewById(R.id.ll_random);
        ll_soul = view.findViewById(R.id.ll_soul);
        ll_fate = view.findViewById(R.id.ll_fate);
        ll_love = view.findViewById(R.id.ll_love);
        iv_add.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
        ll_love.setOnClickListener(this);
        ll_fate.setOnClickListener(this);
        ll_soul.setOnClickListener(this);
        ll_random.setOnClickListener(this);
        loadUser();
        mCloudAdapter = new CloudTagAdapter(getContext(), mStarList);
        mCloudView.setAdapter(mCloudAdapter);
        mCloudView.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View view, int position) {
                startActivity(new Intent(getActivity(), UserInfoActivity.class)
                        .putExtra("objectId", mStarList.get(position).getUserId()));
            }
        });

        //绑定接口
        PairFriendUtil.getInstance().setOnPairResultListener(new PairFriendUtil.OnPairResultListener() {
            @Override
            public void OnPairListener(String userId) {
                Intent intent = new Intent();
                intent.setClass(getContext(), UserInfoActivity.class);
                intent.putExtra("objectId", userId);
                startActivity(intent);
            }

            @Override
            public void OnPairFailListener() {
                Toast.makeText(getContext(), "暂无匹配对象", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 读取用户信息
     */
    private void loadUser() {
        BmobManager.getInstance().queryAllUser(new FindListener<MeetUser>() {
            @Override
            public void done(List<MeetUser> list, BmobException e) {
                if (e == null) {
                    if (list.size() > 0) {
                        if (mAllUserList.size() > 0) {
                            mAllUserList.clear();
                        }
                        mAllUserList = list;
                        if (mStarList.size() > 0) {
                            mStarList.clear();
                        }

                        int index = list.size() >= 100 ? 100 : list.size();

                        //直接填充
                        for (int i = 0; i < index; i++) {
                            MeetUser meetUser = list.get(i);
                            saveStarUser(meetUser.getObjectId(),
                                    meetUser.getNickName(),
                                    meetUser.getPhoto());
                        }
                        mCloudAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    /**
     * 填充数据
     *
     * @param objectId
     * @param nickName
     * @param photo
     */
    private void saveStarUser(String objectId, String nickName, String photo) {
        StarUserModel starUserModel = new StarUserModel();
        starUserModel.setUserId(objectId);
        starUserModel.setNickName(nickName);
        starUserModel.setPhotoUrl(photo);
        mStarList.add(starUserModel);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_add:
                startActivity(new Intent(getContext(), AddFriendActivity.class));
                break;
            case R.id.iv_camera:
                Intent intent = new Intent(getActivity(), QrCodeActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.ll_love:
                matchUser(0);
                break;
            case R.id.ll_fate:
                matchUser(1);
                break;
            case R.id.ll_soul:
                matchUser(2);
                break;
            case R.id.ll_random:
                matchUser(3);
                break;
        }
    }

    /**
     * 匹配好友
     *
     * @param index
     */
    private void matchUser(int index) {
        LogUtils.i("matchUser");
        PairFriendUtil.getInstance().pairUser(index, mAllUserList);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 处理二维码扫描结果
         */
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    if (!TextUtils.isEmpty(result)) {
                        String[] split = result.split("#");
                        if (split != null && split.length >= 2) {
                            startActivity(new Intent(getActivity(), UserInfoActivity.class)
                                    .putExtra("objectId", split[1]));
                        }
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {

                }
            }
        }
    }
}
