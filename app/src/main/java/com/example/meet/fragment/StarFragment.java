package com.example.meet.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.meet.R;
import com.example.meet.adapter.CloudTagAdapter;
import com.example.meet.ui.AddFriendActivity;
import com.moxun.tagcloudlib.view.TagCloudView;

import java.util.ArrayList;
import java.util.List;

public class StarFragment extends Fragment implements View.OnClickListener{

    private ImageView iv_add,iv_camera;
    private LinearLayout ll_random,ll_soul,ll_fate,ll_love;

    private TagCloudView mCloudView;
    private CloudTagAdapter mCloudAdapter;
    private List<String> mLists=new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_star,null);
        initView(view);
        return view;

    }

    /**
     * 初始化view
     * @param view 父容器
     */
    private void initView(View view) {
        iv_add=view.findViewById(R.id.iv_add);
        iv_camera=view.findViewById(R.id.iv_camera);
        mCloudView=view.findViewById(R.id.mCloudView);
        ll_random=view.findViewById(R.id.ll_random);
        ll_soul=view.findViewById(R.id.ll_soul);
        ll_fate=view.findViewById(R.id.ll_fate);
        ll_love=view.findViewById(R.id.ll_love);
        iv_add.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
        ll_love.setOnClickListener(this);
        ll_fate.setOnClickListener(this);
        ll_soul.setOnClickListener(this);
        ll_random.setOnClickListener(this);
        for (int i = 0; i < 200; i++) {
            mLists.add("soul"+i);
        }
        mCloudAdapter=new CloudTagAdapter(getContext(),mLists);
        mCloudView.setAdapter(mCloudAdapter);
        mCloudView.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View view, int position) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_add:
                startActivity(new Intent(getContext(), AddFriendActivity.class));
                break;
            case R.id.iv_camera:
                break;

        }
    }
}
