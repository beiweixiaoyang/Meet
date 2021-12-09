package com.example.meet.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.meet.R;
import com.example.meet.adapter.CommonAdapter;
import com.example.meet.adapter.CommonViewHolder;
import com.example.meet.base.BaseFragment;
import com.example.meet.bmob.BmobManager;
import com.example.meet.bmob.MeetUser;
import com.example.meet.bmob.SquareSet;
import com.example.meet.ui.PushSquareActivity;
import com.example.meet.utils.LogUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Square界面
 */
public class SquareFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    public static int REQUEST_CODE=2000;

    private ImageView iv_push;
    private SwipeRefreshLayout mSquareRefreshLayout;
    private View item_empty_view;

    private RecyclerView mSquareRecyclerView;
    private CommonAdapter<SquareSet> mCommonAdapter;
    private List<SquareSet> mList=new ArrayList<>();

    private FloatingActionButton fb_square_top;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_square,null);
        initView(view);
        return view;
    }

    /**
     * 初始化view
     * @param view
     */
    private void initView(View view) {
        iv_push=view.findViewById(R.id.iv_push);
        mSquareRefreshLayout=view.findViewById(R.id.mSquareRefreshLayout);
        item_empty_view=view.findViewById(R.id.item_empty_view);
        fb_square_top=view.findViewById(R.id.fb_square_top);
        mSquareRefreshLayout.setOnRefreshListener(this);
        iv_push.setOnClickListener(this);

        mSquareRecyclerView=view.findViewById(R.id.mSquareRecyclerView);
        mSquareRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSquareRecyclerView.addItemDecoration(
                new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        mCommonAdapter=new CommonAdapter<SquareSet>(mList, new CommonAdapter.OnMoreBindDataListener<SquareSet>() {
            @Override
            public int getItemType(int position) {
                return position;
            }

            @Override
            public void onBindViewHolder(SquareSet model, CommonViewHolder holder, int type, int position) {
                BmobManager.getInstance().queryByObjectId(model.getObjectId(), new FindListener<MeetUser>() {
                    @Override
                    public void done(List<MeetUser> list, BmobException e) {
                        if(e == null){
                            if(list.size() > 0){
                            }
                        }else{
                            LogUtils.e(e.toString());
                        }
                    }
                });
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layou_square_item;

            }
        });
        mSquareRecyclerView.setAdapter(mCommonAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_push:
                startActivityForResult(new Intent(getActivity(), PushSquareActivity.class),REQUEST_CODE);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode ==REQUEST_CODE){
                loadSquare();
            }
        }
    }



    @Override
    public void onRefresh() {
        loadSquare();
    }

    /**
     * 读取广场数据
     */
    private void loadSquare() {
        mSquareRefreshLayout.setRefreshing(true);
        BmobManager.getInstance().queryAllSquare(new FindListener<SquareSet>() {
            @Override
            public void done(List<SquareSet> list, BmobException e) {
                if(e == null){
                    if(list.size() > 0){
                        mSquareRecyclerView.setVisibility(View.VISIBLE);
                        item_empty_view.setVisibility(View.GONE);
                        if(mList.size() > 0){
                            mList.clear();
                        }
                        mList.addAll(list);
                        mCommonAdapter.notifyDataSetChanged();
                    }else{
                        mSquareRecyclerView.setVisibility(View.GONE);
                        item_empty_view.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }
}
