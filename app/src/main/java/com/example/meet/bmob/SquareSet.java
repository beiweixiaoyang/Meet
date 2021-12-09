package com.example.meet.bmob;

import cn.bmob.v3.BmobObject;

public class SquareSet extends BmobObject {
    //文本
    public static final int PUSH_TEXT = 0;
    //图片
    public static final int PUSH_IMAGE = 1;
    //音乐
    public static final int PUSH_MUSIC = 2;
    //视频
    public static final int PUSH_VIDEO = 3;

    /**
     * 发送类型
     */
    private int pushType;

    //发布者的ID
    private String userId;

    //发布者的时间
    private long pushTime;

    //文字
    private String text;
    //图片
    //音乐
    //视频
    private String mediaUrl;

    public int getPushType() {
        return pushType;
    }

    public void setPushType(int pushType) {
        this.pushType = pushType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getPushTime() {
        return pushTime;
    }

    public void setPushTime(long pushTime) {
        this.pushTime = pushTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    @Override
    public String toString() {
        return "SquareSet{" +
                "pushType=" + pushType +'\n'+
                ", userId='" + userId + '\'' +'\n'+
                ", pushTime=" + pushTime +'\n'+
                ", text='" + text + '\'' +'\n'+
                ", mediaUrl='" + mediaUrl + '\'' +
                '}';
    }
}
