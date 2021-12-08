package com.example.meet.utils;

import com.example.meet.bmob.BmobManager;
import com.example.meet.bmob.FateSet;
import com.example.meet.bmob.MeetUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 匹配好友工具类
 */
public class PairFriendUtil {

    private static volatile PairFriendUtil instance;

    private static final int DELAY_TIME = 3;//延时时间
    private Random random;

    private String myObjectId;

    private MeetUser mine;

    private Disposable mDisposable;

    private int queryNumber=0;//查询次数

    //接口
    private OnPairResultListener onPairResultListener;

    public void setOnPairResultListener(OnPairResultListener onPairResultListener) {
        this.onPairResultListener = onPairResultListener;
    }

    private PairFriendUtil() {
        random = new Random();
        myObjectId=BmobManager.getInstance().getCurrentUser().getObjectId();
        mine=BmobManager.getInstance().getCurrentUser();
    }

    public static PairFriendUtil getInstance() {
        if (instance == null) {
            synchronized (PairFriendUtil.class) {
                if (instance == null) {
                    instance = new PairFriendUtil();
                }
            }
        }
        return instance;
    }

    public void pairUser(int index, List<MeetUser> list) {
        switch (index) {
            case 0:
                loveMatchUser(list);
                break;
            case 1:
                fateMatchUser();
                break;
            case 2:
                //灵魂匹配
                soulMatchUser(list);
                break;
            case 3:
                //随机匹配
                randomMatchUser(list);
                break;
        }
    }

    private void loveMatchUser(List<MeetUser> list) {
        List<MeetUser>love_user=new ArrayList<>();
        List<String>love_userId=new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            MeetUser meetUser = list.get(i);
            if (meetUser.getObjectId().equals(myObjectId)){
                list.remove(i);
                continue;
            }
            //只匹配异性
            if(meetUser.isSex()!=mine.isSex()){
                love_user.add(meetUser);
            }
        }
        if(love_user.size() > 0){
            for (int i = 0; i < love_user.size(); i++) {
                MeetUser meetUser=love_user.get(i);
                if(Math.abs(meetUser.getAge() - mine.getAge())<=3){
                    love_userId.add(meetUser.getObjectId());
                }
            }
            if(love_userId.size() > 0){
                rxJavaParingResult(new OnRxJavaResultListener() {
                    @Override
                    public void rxJavaResult() {
                        int r=random.nextInt(love_userId.size());
                        onPairResultListener.OnPairListener(love_userId.get(r));
                    }
                });
            }else{
                onPairResultListener.OnPairFailListener();
            }
        }else{
            onPairResultListener.OnPairFailListener();
        }
    }

    /**
     * 缘分匹配好友
     */
    private void fateMatchUser() {
        //创建fateSet库，将自身添加进去
        BmobManager.getInstance().addFateSet(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if(e == null){
                    mDisposable=Observable.interval(1,TimeUnit.SECONDS)
                            .subscribe(new Consumer<Long>() {
                                @Override
                                public void accept(Long aLong) throws Exception {
                                    queryFateSet(s);
                                }
                            });
                }
            }
        });
    }

    /**
     * 查询fateSet库
     */
    private void queryFateSet(String id) {
        BmobManager.getInstance().queryFateSet(new FindListener<FateSet>() {
            @Override
            public void done(List<FateSet> list, BmobException e) {
                if(e == null){
                    queryNumber++;
                    if(list.size() > 1){
                        //listsize大于1时才能代表库中>=2
                        disposable();
                        //过滤掉自身
                        for (int i = 0; i < list.size(); i++) {
                            FateSet fateSet=list.get(i);
                            if(fateSet.getUserId().equals(myObjectId)){
                                list.remove(i);
                                break;
                            }
                        }
                        int r=random.nextInt(list.size());
                        onPairResultListener.OnPairListener(list.get(r).getUserId());
                        //匹配完成之后在fateSet库中删除
                        queryNumber=0;
                        deleteFateSet(id);
                    }else{
                        LogUtils.i("queryNumber"+queryNumber);
                        //超过十五次之后，显示超时
                        if(queryNumber>=15){
                            disposable();
                            deleteFateSet(id);
                            onPairResultListener.OnPairFailListener();
                            queryNumber=0;
                        }
                    }
                }
            }
        });
    }

    /**
     * 根据id删除fateSet库中的数据
     * @param id
     */
    private void deleteFateSet(String id) {
        BmobManager.getInstance().delFateSet(id, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                LogUtils.i("deleteFateSet");
            }
        });
    }

    /**
     * 灵魂匹配好友
     * @param list
     */
    private void soulMatchUser(List<MeetUser> list) {
        LogUtils.i("soulMatchUser");
        Map<String,String>map=new HashMap<>();
        List<String>list_objectId=new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            MeetUser meetUser = list.get(i);
            LogUtils.e(meetUser.getObjectId());
            if(myObjectId.equals(meetUser.getObjectId())){
                list.remove(i);
                continue;
            }
            //map中key不能重复
            if(mine.getConstellation().equals(meetUser.getConstellation())){
                map.put(meetUser.getObjectId(),meetUser.getObjectId());
            }
            if(mine.getAge() == meetUser.getAge()){
                map.put(meetUser.getObjectId(),meetUser.getObjectId());
            }
            if(mine.getStatus().equals(meetUser.getStatus())){
                map.put(meetUser.getObjectId(),meetUser.getObjectId());
            }
            if(mine.getHobby().equals(meetUser.getHobby())){
                map.put(meetUser.getObjectId(),meetUser.getObjectId());
            }
        }
        rxJavaParingResult(new OnRxJavaResultListener() {
            @Override
            public void rxJavaResult() {
                Set<String> strings = map.keySet();
                for(String objectId:strings){
                    list_objectId.add(objectId);
                }
                if(list_objectId.size()>0){
                    int r=random.nextInt(list_objectId.size());
                    MeetUser meetUser = list.get(r);
                    if(meetUser != null){
                        onPairResultListener.OnPairListener(meetUser.getObjectId());
                    }
                }
            }
        });
    }


    /**
     * 随机匹配
     */
    private void randomMatchUser(List<MeetUser> list) {
        // 获取到全部用户(通过传值)
        for (int i = 0; i < list.size(); i++) {
            //过滤掉自身
            if (list.get(i).getObjectId().equals(BmobManager.getInstance().getCurrentUser().getObjectId())) {
                list.remove(i);
            }
        }
        //处理结果
        rxJavaParingResult(new OnRxJavaResultListener() {
            @Override
            public void rxJavaResult() {
                int r = random.nextInt(list.size());
                MeetUser meetUser = list.get(r);
                if (meetUser != null) {
                    onPairResultListener.OnPairListener(meetUser.getObjectId());
                }
            }
        });
    }

    /**
     * 销毁disposable
     */
    public void disposable() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    private void rxJavaParingResult(OnRxJavaResultListener listener) {
        mDisposable = Observable.timer(DELAY_TIME, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        listener.rxJavaResult();
                    }
                });
    }

    //匹配结果的回调
    public interface OnPairResultListener {

        //匹配成功
        void OnPairListener(String userId);

        //匹配失败
        void OnPairFailListener();
    }

    public interface OnRxJavaResultListener {

        void rxJavaResult();
    }
}
