package com.example.meet.fragment;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.TimeUtils;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.meet.R;
import com.example.meet.adapter.CommonAdapter;
import com.example.meet.adapter.CommonViewHolder;
import com.example.meet.base.BaseFragment;
import com.example.meet.bmob.BmobManager;
import com.example.meet.bmob.MeetUser;
import com.example.meet.bmob.SquareSet;
import com.example.meet.manager.MediaPlayerManager;
import com.example.meet.manager.WindowHelper;
import com.example.meet.ui.ImagePreviewActivity;
import com.example.meet.ui.PushSquareActivity;
import com.example.meet.ui.UserInfoActivity;
import com.example.meet.utils.AnimUtils;
import com.example.meet.utils.FileUtil;
import com.example.meet.utils.LogUtils;
import com.example.meet.utils.TimeUtil;
import com.example.meet.view.VideoJzvdStd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Square界面
 */
public class SquareFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final int UPDATE_POS = 1500;
    public static int REQUEST_CODE = 2000;
    private SimpleDateFormat simpleDateFormat;

    private ImageView iv_push;
    private SwipeRefreshLayout mSquareRefreshLayout;
    private View item_empty_view;

    private RecyclerView mSquareRecyclerView;
    private CommonAdapter<SquareSet> mCommonAdapter;
    private List<SquareSet> mList = new ArrayList<>();

    private FloatingActionButton fb_square_top;

    //播放
    private MediaPlayerManager mMusicManager;
    //音乐是否在播放
    private boolean isMusicPlay = false;


    //音乐悬浮窗
    private WindowManager.LayoutParams lpMusicParams;
    private View musicWindowView;
    private ImageView iv_music_photo;
    private ProgressBar pb_music_pos;
    private TextView tv_music_cur;
    private TextView tv_music_all;

    //属性动画
    private ObjectAnimator objAnimMusic;
    //是否移动
    private boolean isMove = false;
    //是否拖拽
    private boolean isDrag = false;
    private int mLastX;
    private int mLastY;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_POS:
                    int pos = msg.arg1;
                    tv_music_cur.setText(TimeUtil.formatDuring(pos));
                    pb_music_pos.setProgress(pos);
                    break;
            }
            return false;
        }
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_square, null);
        initView(view);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return view;
    }

    /**
     * 初始化view
     *
     * @param view
     */
    private void initView(View view) {
        initMusicWindow();
        mMusicManager = new MediaPlayerManager();
        //监听音乐是否播放完成
        mMusicManager.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isMusicPlay = false;
            }
        });
        mMusicManager.setMusicProgressListener(new MediaPlayerManager.OnMusicProgressListener() {
            @Override
            public void OnProgress(int progress, int percent) {
                Message message = new Message();
                message.what = UPDATE_POS;
                message.arg1 = progress;
                mHandler.sendMessage(message);
            }
        });
        iv_push = view.findViewById(R.id.iv_push);
        mSquareRefreshLayout = view.findViewById(R.id.mSquareRefreshLayout);
        item_empty_view = view.findViewById(R.id.item_empty_view);
        fb_square_top = view.findViewById(R.id.fb_square_top);
        mSquareRefreshLayout.setOnRefreshListener(this);
        iv_push.setOnClickListener(this);
        fb_square_top.setOnClickListener(this);
        mSquareRecyclerView = view.findViewById(R.id.mSquareRecyclerView);
        mSquareRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSquareRecyclerView.addItemDecoration(
                new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        ((SimpleItemAnimator) mSquareRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        mCommonAdapter = new CommonAdapter<SquareSet>(mList, new CommonAdapter.OnMoreBindDataListener<SquareSet>() {
            @Override
            public int getItemType(int position) {
                return position;
            }

            @Override
            public void onBindViewHolder(SquareSet model, CommonViewHolder holder, int type, int position) {
                BmobManager.getInstance().queryByObjectId(model.getObjectId(), new FindListener<MeetUser>() {
                    @Override
                    public void done(List<MeetUser> list, BmobException e) {
                        if (e == null) {
                            if (list.size() > 0) {
                                MeetUser meetUser = list.get(0);
                                if (!TextUtils.isEmpty(meetUser.getPhoto())) {
                                    holder.setImageUrl(getContext(), R.id.iv_photo, meetUser.getPhoto(), 50, 50);
                                }
                                holder.setText(R.id.tv_nickname, meetUser.getNickName());
                                holder.setText(R.id.tv_age, meetUser.getAge() + "岁");
                                //不存在的属性设置为不可见
                                if (!TextUtils.isEmpty(meetUser.getConstellation())) {
                                    holder.setText(R.id.tv_square_constellation, meetUser.getConstellation());
                                    holder.setVisibility(R.id.tv_square_constellation, View.VISIBLE);
                                }
                                if (!TextUtils.isEmpty(meetUser.getHobby())) {
                                    holder.setText(R.id.tv_square_hobby, meetUser.getHobby());
                                    holder.setVisibility(R.id.tv_square_hobby, View.VISIBLE);
                                }
                                if (!TextUtils.isEmpty(meetUser.getStatus())) {
                                    holder.setText(R.id.tv_square_status, meetUser.getStatus());
                                    holder.setVisibility(R.id.tv_square_status, View.VISIBLE);
                                }
                            }
                        } else {
                            LogUtils.e(e.toString());
                        }
                    }
                });

                //设置时间
                holder.setText(R.id.tv_time, simpleDateFormat.format(model.getPushTime()));
                //设置头像的点击事件
                holder.getView(R.id.iv_photo).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                        intent.putExtra("objectId", model.getObjectId());
                        startActivity(intent);
                    }
                });

                if (!TextUtils.isEmpty(model.getText())) {
                    holder.setText(R.id.tv_text, model.getText());
                } else {
                    holder.setVisibility(R.id.tv_text, View.GONE);
                }

                //多媒体（图片，音频，视频）
                switch (model.getPushType()) {
                    case SquareSet.PUSH_TEXT:
                        goneItemView(holder, false, false, false);
                        break;
                    case SquareSet.PUSH_IMAGE:
                        goneItemView(holder, true, false, false);
                        holder.setImageUrl(getContext(), R.id.iv_img, model.getMediaUrl());
                        holder.getView(R.id.iv_img).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //点击图片跳转到图片预览界面
                                ImagePreviewActivity.startActivity(getContext(), true, model.getMediaUrl());
                            }
                        });
                        break;
                    case SquareSet.PUSH_MUSIC:
                        goneItemView(holder, false, true, false);
                        holder.getView(R.id.ll_music).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestWindowPermission(getActivity());
                                //播放音乐
                                if (mMusicManager.isPlaying()) {
                                    hideMusicWindow();
                                } else {
                                    if (isMusicPlay) {
                                        mMusicManager.continuePlay();
                                    } else {
                                        mMusicManager.startPlay(model.getMediaUrl());
                                        isMusicPlay = true;
                                    }
                                    showMusicWindow();
                                }
                            }
                        });
                        break;
                    case SquareSet.PUSH_VIDEO:
                        goneItemView(holder, false, false, true);
                        holder.getView(R.id.tv_text).setVisibility(View.GONE);
                        VideoJzvdStd jzvdStd = holder.getView(R.id.jz_video);
                        //实现视频
                        jzvdStd.setUp(model.getMediaUrl(), model.getText());
                        Observable.create(new ObservableOnSubscribe<Bitmap>() {
                            @Override
                            public void subscribe(@NonNull ObservableEmitter<Bitmap> emitter) throws Exception {
                                Bitmap videoBitmap
                                        = FileUtil.getInstance().getNetVideoBitmap(model.getMediaUrl());
                                if (videoBitmap != null) {
                                    emitter.onNext(videoBitmap);
                                    emitter.onComplete();
                                }
                            }
                        }).subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<Bitmap>() {
                                    @Override
                                    public void accept(Bitmap bitmap) throws Exception {
                                        if (bitmap != null) {
                                            jzvdStd.thumbImageView.setImageBitmap(bitmap);
                                        }
                                    }
                                });
                        break;
                }
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layou_square_item;

            }
        });
        //监听列表滑动
        mSquareRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                        int position = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                        //square中item数量大于5时，显示floatActionButton，点击跳转到最上方
                        if(position > 5 ){
                            fb_square_top.setVisibility(View.VISIBLE);
                        }else {
                            fb_square_top.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
        mSquareRecyclerView.setAdapter(mCommonAdapter);
    }

    /**
     * 初始化音乐悬浮窗
     */
    private void initMusicWindow() {
        lpMusicParams = WindowHelper.getInstance().createLayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.START);
        musicWindowView = WindowHelper.getInstance().getView(R.layout.layout_square_music_item);
        //初始化窗口中的view
        iv_music_photo = musicWindowView.findViewById(R.id.iv_music_photo);
        pb_music_pos = musicWindowView.findViewById(R.id.pb_music_pos);
        tv_music_cur = musicWindowView.findViewById(R.id.tv_music_cur);
        tv_music_all = musicWindowView.findViewById(R.id.tv_music_all);
        objAnimMusic = AnimUtils.rotation(iv_music_photo);
        musicWindowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMusicWindow();
            }
        });
        musicWindowView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        musicWindowView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                /**
                 * OnTouch 和 OnClick 点击冲突
                 * 如何判断是点击 还是 移动
                 * 通过点击下的坐标 - 落地的坐标 如果移动则说明是移动 如果 = 0 ，那说明没有移动则是点击
                 */
                int mStartX = (int) event.getRawX();
                int mStartY = (int) event.getRawY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isMove = false;
                        isDrag = false;
                        mLastX = (int) event.getRawX();
                        mLastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:

                        //偏移量
                        int dx = mStartX - mLastX;
                        int dy = mStartY - mLastY;

                        if (isMove) {
                            isDrag = true;
                        } else {
                            if (dx == 0 && dy == 0) {
                                isMove = false;
                            } else {
                                isMove = true;
                                isDrag = true;
                            }
                        }

                        //移动
                        lpMusicParams.x += dx;
                        lpMusicParams.y += dy;

                        //重置坐标
                        mLastX = mStartX;
                        mLastY = mStartY;

                        //WindowManager addView removeView updateView
                        WindowHelper.getInstance().updateView(musicWindowView, lpMusicParams);

                        break;
                }
                return isDrag;
            }
        });
    }


    /**
     * 显示窗口
     */
    private void showMusicWindow() {
        pb_music_pos.setMax(mMusicManager.getDuration());
        tv_music_all.setText(TimeUtil.formatDuring(mMusicManager.getDuration()));
        objAnimMusic.start();
        WindowHelper.getInstance().showView(musicWindowView, lpMusicParams);
    }

    /**
     * 隐藏播放音乐窗口
     */
    private void hideMusicWindow() {
        mMusicManager.pausePlay();
        objAnimMusic.pause();
        WindowHelper.getInstance().hideView(musicWindowView);
    }

    /**
     * 控制view的显示
     *
     * @param viewHolder
     * @param img        图片
     * @param audio      音频
     * @param video      视频
     */
    private void goneItemView(CommonViewHolder viewHolder,
                              boolean img, boolean audio, boolean video) {
        viewHolder.getView(R.id.tv_text).setVisibility(View.VISIBLE);
        viewHolder.getView(R.id.iv_img).setVisibility(img ? View.VISIBLE : View.GONE);
        viewHolder.getView(R.id.ll_music).setVisibility(audio ? View.VISIBLE : View.GONE);
        viewHolder.getView(R.id.ll_video).setVisibility(video ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_push:
                startActivityForResult(new Intent(getActivity(), PushSquareActivity.class), REQUEST_CODE);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
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
                mSquareRefreshLayout.setRefreshing(false);
                if (e == null) {
                    if (list.size() > 0) {
                        Collections.reverse(list);
                        mSquareRecyclerView.setVisibility(View.VISIBLE);
                        item_empty_view.setVisibility(View.GONE);
                        if (mList.size() > 0) {
                            mList.clear();
                        }
                        mList.addAll(list);
                        mCommonAdapter.notifyDataSetChanged();
                    } else {
                        mSquareRecyclerView.setVisibility(View.GONE);
                        item_empty_view.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }
}
