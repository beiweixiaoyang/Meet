package com.example.meet.bmob;

import android.content.Context;

import com.example.meet.model.Friend;
import com.example.meet.utils.LogUtils;

import java.io.File;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

/**
 * 对bmob的一些函数进行封装
 */
public class BmobManager {

    private static BmobManager instance;

    private static final String BMOB_ID = "c78dc48c4a7dc77d74d061b2f8bc662e";

    private BmobManager() {
    }

    public static BmobManager getInstance() {
        if (instance == null) {
            synchronized (BmobManager.class) {
                if (instance == null) {
                    instance = new BmobManager();
                }
            }
        }
        return instance;
    }

    public void initBmob(Context context) {
        LogUtils.i("init-->Bmob");
        Bmob.initialize(context, BMOB_ID);
    }

    /**
     * 请求发送短信验证码
     *
     * @param phone    电话号码
     * @param listener 回调函数
     */
    public void requestSMSCode(String phone, QueryListener<Integer> listener) {
        BmobSMS.requestSMSCode(phone, "", listener);
    }

    /**
     * 验证短信验证码
     *
     * @param phone    电话号码
     * @param SMSCode  短信验证码
     * @param listener 回调函数
     */
    public void signOrLoginByMobilePhone(String phone, String SMSCode, LogInListener<MeetUser> listener) {
        BmobUser.signOrLoginByMobilePhone(phone, SMSCode, listener);
    }

    /**
     * 判断当前是否有用户登录
     *
     * @return
     */
    public boolean isLogin() {
        return BmobUser.isLogin();
    }

    /**
     * 获取本地用户
     *
     * @return
     */
    public MeetUser getCurrentUser() {
        return BmobUser.getCurrentUser(MeetUser.class);
    }

    /**
     * 查询所有用户
     *
     * @param listener
     */
    public void queryAllUser(FindListener<MeetUser> listener) {
        BmobQuery<MeetUser> query = new BmobQuery<>();
        query.findObjects(listener);
    }

    /**
     * 根据objectId查询用户
     */
    public void queryByObjectId(String objectId, FindListener<MeetUser> listener) {
        baseQuery("objectId", objectId, listener);
    }

    /**
     * 根据手机号查询用户
     */
    public void queryByPhone(String phone, FindListener<MeetUser> listener) {
        baseQuery("mobilePhoneNumber", phone, listener);
    }

    /**
     * 查询基本方法
     *
     * @param key
     * @param values
     * @param listener
     */
    public void baseQuery(String key, String values, FindListener<MeetUser> listener) {
        BmobQuery<MeetUser> query = new BmobQuery<>();
        query.addWhereEqualTo(key, values);//通过key/values进行条件查询
        query.findObjects(listener);
    }

    /**
     * 查询我的好友
     */
    public void queryMyFriend(FindListener<Friend>listener){
        BmobQuery<Friend> query = new BmobQuery<>();
        query.addWhereEqualTo("user", getCurrentUser());
        query.findObjects(listener);
    }


    /**
     *
     * @param nickname 昵称
     * @param file 头像
     * @param listener 是否上传完成的监听
     */
    public void uploadFile(String nickname, File file, OnUploadListener listener){
        MeetUser meetUser=getCurrentUser();
        BmobFile bmobFile=new BmobFile(file);
        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if(e == null){
                    meetUser.setNickName(nickname);
                    meetUser.setPhoto(bmobFile.getFileUrl());
                    meetUser.setTokenNickName(nickname);
                    meetUser.setTokenPhoto(bmobFile.getFileUrl());
                    //更新用户信息
                    meetUser.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if(e == null){
                                listener.onUploadDone();
                            }else{
                                listener.onUploadFailed(e);
                                LogUtils.e("上传失败："+e.toString());
                            }
                        }
                    });
                }else{
                    listener.onUploadFailed(e);
                    LogUtils.e("上传失败："+e.toString());
                }
            }
        });
    }

    /**
     * 添加好友到自己的好友列表
     * @param meetUser
     */
    public void addFriend(MeetUser meetUser, SaveListener<String>listener){
        Friend friend=new Friend();
        friend.setMeetUser(getCurrentUser());
        friend.setFriendUser(meetUser);
        friend.save(listener);
    }

    /**
     * 通过objectId添加到好友列表中
     * @param objectId
     * @param listener
     */
    public void addFriend(String objectId,SaveListener<String> listener){
        queryByObjectId(objectId, new FindListener<MeetUser>() {
            @Override
            public void done(List<MeetUser> list, BmobException e) {
                if(e == null){
                    if(list.size() > 0){
                        MeetUser meetUser = list.get(0);
                        addFriend(meetUser,listener);
                    }
                }
            }
        });
    }
    public interface OnUploadListener{
        void onUploadDone();
        void onUploadFailed(BmobException e);
    }

}
