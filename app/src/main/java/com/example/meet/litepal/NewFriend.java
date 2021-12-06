package com.example.meet.litepal;

import org.litepal.crud.LitePalSupport;

public class NewFriend extends LitePalSupport {

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
