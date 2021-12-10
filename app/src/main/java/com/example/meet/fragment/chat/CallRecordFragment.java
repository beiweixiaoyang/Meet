package com.example.meet.fragment.chat;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.meet.litepal.CallRecord;
import com.example.meet.litepal.LitePalManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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
 * 通话记录tab
 */
public class CallRecordFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private Disposable disposable;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private View item_empty_view;
    private RecyclerView mCallRecordView;
    private SwipeRefreshLayout mCallRecordRefreshLayout;

    private List<CallRecord> mList = new ArrayList<>();
    private CommonAdapter<CallRecord> mCallRecordAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call_record, null);
        initView(view);
        return view;
    }

    private void initView(final View view) {
        item_empty_view = view.findViewById(R.id.item_empty_view);
        mCallRecordView = view.findViewById(R.id.mCallRecordView);
        mCallRecordRefreshLayout = view.findViewById(R.id.mCallRecordRefreshLayout);

        mCallRecordRefreshLayout.setOnRefreshListener(this);

        mCallRecordView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCallRecordView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));

        mCallRecordAdapter = new CommonAdapter<>(mList, new CommonAdapter.OnBindDataListener<CallRecord>() {
            @Override
            public void onBindViewHolder(final CallRecord model, final CommonViewHolder holder, int type, int position) {
                String mediaType = "";
                if (model.getMediaType() == CallRecord.MEDIA_TYPE_AUDIO) {
                    mediaType = "[音频]";
                } else if (model.getMediaType() == CallRecord.MEDIA_TYPE_VIDEO) {
                    mediaType = "[视频]";
                }
                String callStatus = "";
                if (model.getCallStatus() == CallRecord.CALL_STATUS_UN_ANSWER) {
                    callStatus ="[未接电话]";
                    holder.setImageResource(R.id.iv_status_icon, R.drawable.img_un_answer_icon);
                    holder.setTextColor(R.id.tv_nickname, Color.RED);
                    holder.setTextColor(R.id.tv_type, Color.RED);
                } else if (model.getCallStatus() == CallRecord.CALL_STATUS_DIAL) {
                    callStatus = "[已拨电话]";
                    holder.setImageResource(R.id.iv_status_icon, R.drawable.img_dial_icon);
                } else if (model.getCallStatus() == CallRecord.CALL_STATUS_ANSWER) {
                    callStatus = "[已接电话]";
                    holder.setImageResource(R.id.iv_status_icon, R.drawable.img_answer_icon);
                }

                holder.setText(R.id.tv_type, mediaType + " " + callStatus);
                holder.setText(R.id.tv_time, dateFormat.format(model.getCallTime()));

                BmobManager.getInstance().queryByObjectId(model.getUserId(), new FindListener<MeetUser>() {
                    @Override
                    public void done(List<MeetUser> list, BmobException e) {
                        if (e == null) {
                            if (list.size() > 0) {
                                MeetUser meetUser = list.get(0);
                                holder.setText(R.id.tv_nickname, meetUser.getNickName());
                            }
                        }
                    }
                });
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_call_record;
            }
        });
        mCallRecordView.setAdapter(mCallRecordAdapter);
    }

    @Override
    public void onRefresh() {
        if(mCallRecordRefreshLayout.isRefreshing()){
            queryCallRecord();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        queryCallRecord();
    }

    /**
     * 查询通话记录
     */
    private void queryCallRecord() {
        mCallRecordRefreshLayout.setRefreshing(true);
        disposable = Observable.create(new ObservableOnSubscribe<List<CallRecord>>() {
            @Override
            public void subscribe(ObservableEmitter<List<CallRecord>> emitter) throws Exception {
                emitter.onNext(LitePalManager.getInstance().queryCallRecord());
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<CallRecord>>() {
                    @Override
                    public void accept(List<CallRecord> callRecords) throws Exception {
                        mCallRecordRefreshLayout.setRefreshing(false);

                        if (callRecords.size() > 0) {
                            if (mList.size() > 0) {
                                mList.clear();
                            }
                            mList.addAll(callRecords);
                            mCallRecordAdapter.notifyDataSetChanged();

                            item_empty_view.setVisibility(View.GONE);
                            mCallRecordView.setVisibility(View.VISIBLE);

                        } else {
                            item_empty_view.setVisibility(View.VISIBLE);
                            mCallRecordView.setVisibility(View.GONE);
                        }

                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }
}
