package com.example.meet.manager;


import com.example.meet.utils.LogUtils;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.util.List;

/**
 * LitePal（本地数据库）帮助类
 */
public class LitePalManager {

    private static volatile LitePalManager litePalManager;

    public LitePalManager() {
    }

    public static LitePalManager getInstance() {
        if (litePalManager == null) {
            synchronized (LitePalManager.class) {
                if (litePalManager == null) {
                    litePalManager = new LitePalManager();
                }
            }
        }
        return litePalManager;
    }

    /**
     * 保存信息
     * @param support
     */
    private void baseSave(LitePalSupport support){
        support.save();
    }

    /**
     * 保存新朋友相关信息到本地数据库
     */
    public void saveNewFriend(String msg,String id){
        LogUtils.i("saveNewFriend");
        NewFriend newFriend=new NewFriend();
        newFriend.setMsg(msg);
        newFriend.setSaveTime(System.currentTimeMillis());
        newFriend.setStatus(-1);
        newFriend.setUserId(id);
        baseSave(newFriend);
    }

    private List<? extends LitePalSupport> baseQuery(Class cls){
        return LitePal.findAll(cls);
    }

    /**
     * 从本地数据库中 查找所有好友
     * @return
     */
    public List<NewFriend>queryNewFriend(){
        return (List<NewFriend>) baseQuery(NewFriend.class);
    }
    /**
     * 更新好友信息
     */
    public void updateFriend(String userId,int status){
        NewFriend newFriend=new NewFriend();
        newFriend.setStatus(status);
        newFriend.updateAll("userId=?",userId);
    }

    /**
     * 新朋友 继承自LitePalSupport
     * 相当于数据库中一张表
     * 类中的属性为 表中的一列
     */
    public static class NewFriend extends LitePalSupport {

        //对方发送添加请求时的备注
        private String msg;
        //对方userId
        private String userId;
        //数据库保存的时间
        private long saveTime;
        //保存的状态  默认为-1（待确认请求） 0（确认） 1（拒绝）
        private int status=-1;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public long getSaveTime() {
            return saveTime;
        }

        public void setSaveTime(long saveTime) {
            this.saveTime = saveTime;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
